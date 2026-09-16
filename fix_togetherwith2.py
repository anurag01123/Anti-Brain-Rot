import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix literal \n
content = content.replace("\\nimport androidx.compose.animation.togetherWith", "\nimport androidx.compose.animation.togetherWith")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
