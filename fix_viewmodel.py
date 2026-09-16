import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

old_appUsages_decl = "val appUsages: StateFlow<Map<String, Long>>"
new_appUsages_decl = "val appUsages: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Long> = androidx.compose.runtime.mutableStateMapOf()"

content = content.replace(old_appUsages_decl, new_appUsages_decl)

old_appUsages_init = """        appUsages = kotlinx.coroutines.flow.combine(
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

new_appUsages_init = """        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            allTrackedApps.collect { apps ->
                while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                    if (apps.isEmpty()) break
                    apps.forEach { app ->
                        val usage = com.example.utils.UsageUtils.getUsageTimeForApp(application, app.packageName)
                        if (appUsages[app.packageName] != usage) {
                            appUsages[app.packageName] = usage
                        }
                    }
                    kotlinx.coroutines.delay(2000)
                }
            }
        }"""

content = content.replace(old_appUsages_init, new_appUsages_init)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

main_content = main_content.replace("val appUsages by viewModel.appUsages.collectAsStateWithLifecycle()", "val appUsages = viewModel.appUsages")
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)
