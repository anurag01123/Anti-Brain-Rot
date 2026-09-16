import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.animation.AnimatedVisibility", "import androidx.compose.animation.AnimatedVisibility\\nimport androidx.compose.animation.togetherWith")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
