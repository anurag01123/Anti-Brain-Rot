import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_set_content = """        setContent {
            MyApplicationTheme {"""

new_set_content = """        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {"""

content = content.replace(old_set_content, new_set_content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
