import re

# 1. MainViewModel.kt - Run packageManager load on IO thread
with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    vm_content = f.read()

vm_content = vm_content.replace(
    "private fun loadInstalledApps() {\n        viewModelScope.launch {",
    "private fun loadInstalledApps() {\n        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {"
)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(vm_content)


# 2. BlockerAccessibilityService.kt - Respect power state to save battery
with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "r") as f:
    service_content = f.read()

old_loop = """        scope.launch {
            while (isActive) {
                if (currentForegroundPackage.isNotEmpty()) {
                    checkAndBlockApp(currentForegroundPackage)
                }
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds for the active app
            }
        }"""

new_loop = """        scope.launch {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            while (isActive) {
                if (powerManager.isInteractive && currentForegroundPackage.isNotEmpty()) {
                    checkAndBlockApp(currentForegroundPackage)
                }
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds for the active app, but only process if screen is on
            }
        }"""

service_content = service_content.replace(old_loop, new_loop)

with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "w") as f:
    f.write(service_content)

# 3. MainActivity.kt - Use LruCache to prevent OOM
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

old_cache = """object AppIconCache {
    val cache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()
}"""

new_cache = """object AppIconCache {
    // 50 items cache to prevent OOM
    val cache = object : android.util.LruCache<String, ImageBitmap>(50) {}
}"""

main_content = main_content.replace(old_cache, new_cache)

# We need to change the map syntax from cache[pkg] to cache.get(pkg) and cache.put(pkg, bmp)
main_content = main_content.replace(
    "AppIconCache.cache[app.packageName] = bmp",
    "AppIconCache.cache.put(app.packageName, bmp)"
)

main_content = main_content.replace(
    "AppIconCache.cache[app.packageName]",
    "AppIconCache.cache.get(app.packageName)"
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)
