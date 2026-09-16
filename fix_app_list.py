import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

old_load_apps = """    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .map { 
                    AppInfo(
                        packageName = it.packageName,
                        appName = pm.getApplicationLabel(it).toString()
                    )
                }
                .sortedBy { it.appName }
            _installedApps.value = apps
        }
    }"""

new_load_apps = """    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val apps = resolveInfos.map { 
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    appName = it.loadLabel(pm).toString()
                )
            }.distinctBy { it.packageName }.sortedBy { it.appName }
            _installedApps.value = apps
        }
    }"""
    
content = content.replace(old_load_apps, new_load_apps)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
