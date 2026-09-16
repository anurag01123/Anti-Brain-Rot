import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add IconCache
icon_cache = """
object AppIconCache {
    val cache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()
}

class MainActivity : ComponentActivity() {"""
content = content.replace("class MainActivity : ComponentActivity() {", icon_cache)

# Replace icon load in TrackedAppCard
old_icon_load = """                    val iconBitmap = remember(app.packageName) {
                        try {
                            val drawable = context.packageManager.getApplicationIcon(app.packageName)
                            drawableToImageBitmap(drawable)
                        } catch (e: Exception) {
                            null
                        }
                    }"""

new_icon_load = """                    var iconBitmap by remember(app.packageName) { mutableStateOf<ImageBitmap?>(AppIconCache.cache[app.packageName]) }
                    LaunchedEffect(app.packageName) {
                        if (iconBitmap == null) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                    val bmp = drawableToImageBitmap(drawable)
                                    AppIconCache.cache[app.packageName] = bmp
                                    iconBitmap = bmp
                                } catch (e: Exception) {}
                            }
                        }
                    }"""

content = content.replace(old_icon_load, new_icon_load)

# Also fix it in the add dialog
old_dialog_load = """                                    val iconBitmap = remember(app.packageName) {
                                        try {
                                            val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                            drawableToImageBitmap(drawable)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }"""
content = content.replace(old_dialog_load, new_icon_load)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
