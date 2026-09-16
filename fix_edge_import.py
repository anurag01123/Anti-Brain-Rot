import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.activity.compose.setContent", "import androidx.activity.compose.setContent\nimport androidx.activity.enableEdgeToEdge")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
