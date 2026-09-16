import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

old_polling = """        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            while(true) {
                val apps = allTrackedApps.value
                if (apps.isNotEmpty()) {
                    val usages = apps.associate { app ->
                        app.packageName to com.example.utils.UsageUtils.getUsageTimeForApp(application, app.packageName)
                    }
                    _appUsages.value = usages
                } else {
                    _appUsages.value = emptyMap()
                }
                kotlinx.coroutines.delay(2000) // Poll every 2 seconds to reduce battery/CPU usage
            }
        }"""

new_polling = """        // Polling removed from init.
        // It will be handled via a Lifecycle-aware flow when the screen is visible."""

content = content.replace(old_polling, new_polling)

# Now define appUsages flow
old_appUsages = """    private val _appUsages = MutableStateFlow<Map<String, Long>>(emptyMap())
    val appUsages: StateFlow<Map<String, Long>> = _appUsages"""

new_appUsages = """    val appUsages: StateFlow<Map<String, Long>> = kotlinx.coroutines.flow.flow {
        while(true) {
            emit(Unit)
            kotlinx.coroutines.delay(2000)
        }
    }.kotlinx.coroutines.flow.combine(allTrackedApps) { _, apps ->
        if (apps.isNotEmpty()) {
            apps.associate { app ->
                app.packageName to com.example.utils.UsageUtils.getUsageTimeForApp(application, app.packageName)
            }
        } else emptyMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())"""

content = content.replace(old_appUsages, new_appUsages)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
