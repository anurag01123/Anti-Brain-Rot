import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Replace duplicate warning
content = re.sub(r'import androidx.compose.material.icons.filled.Warning\n(import androidx.compose.material.icons.filled.Warning\n)+', r'import androidx.compose.material.icons.filled.Warning\n', content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
