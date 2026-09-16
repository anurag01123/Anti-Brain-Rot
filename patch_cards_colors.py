import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("com.example.ui.theme.md_theme_light_secondary", "MaterialTheme.colorScheme.secondaryContainer")
content = content.replace("com.example.ui.theme.md_theme_light_onSurface", "MaterialTheme.colorScheme.onSurface")
content = content.replace("com.example.ui.theme.md_theme_light_primary", "MaterialTheme.colorScheme.primaryContainer")
content = content.replace("com.example.ui.theme.md_theme_light_tertiary", "MaterialTheme.colorScheme.tertiaryContainer")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
