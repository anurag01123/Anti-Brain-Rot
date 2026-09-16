import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_setcontent = """        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {"""

new_setcontent = """        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            
            LaunchedEffect(isDarkMode) {
                androidx.activity.enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkMode) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
            }
            
            MyApplicationTheme(darkTheme = isDarkMode) {"""

content = content.replace(old_setcontent, new_setcontent)

# Remove the old enableEdgeToEdge if it's in onCreate
content = content.replace("androidx.activity.enableEdgeToEdge()\n        \n        requestPermissionLauncher", "requestPermissionLauncher")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
