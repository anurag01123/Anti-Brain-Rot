import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

# Change appUsages to be initialized in init
old_appUsages = """    val appUsages: StateFlow<Map<String, Long>> = kotlinx.coroutines.flow.combine(
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

if old_appUsages in content:
    content = content.replace(old_appUsages, "    val appUsages: StateFlow<Map<String, Long>>")

old_init_end = """        // Polling removed from init.
        // It will be handled via a Lifecycle-aware flow when the screen is visible.
    }"""

new_init_end = """        
        appUsages = kotlinx.coroutines.flow.combine(
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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    }"""

if old_init_end in content:
    content = content.replace(old_init_end, new_init_end)
else:
    # Just append it
    pass

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
