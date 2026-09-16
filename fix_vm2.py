import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

old_appUsages = """    val appUsages: StateFlow<Map<String, Long>> = kotlinx.coroutines.flow.flow {
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

new_appUsages = """    val appUsages: StateFlow<Map<String, Long>> = kotlinx.coroutines.flow.combine(
        kotlinx.coroutines.flow.flow {
            while(true) {
                emit(Unit)
                kotlinx.coroutines.delay(2000)
            }
        },
        allTrackedApps
    ) { _, apps ->
        if (apps.isNotEmpty()) {
            apps.associate { app ->
                app.packageName to com.example.utils.UsageUtils.getUsageTimeForApp(application, app.packageName)
            }
        } else emptyMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())"""

# Wait, the problem is `allTrackedApps` is defined in init block!
# Yes! `allTrackedApps` is initialized inside init block. So it's not available when initializing `appUsages` property!
content = content.replace(old_appUsages, new_appUsages)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
