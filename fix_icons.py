import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Default.Star", "androidx.compose.material.icons.filled.Star")
content = content.replace("androidx.compose.material.icons.Icons.Default.ThumbUp", "androidx.compose.material.icons.filled.ThumbUp")
content = content.replace("androidx.compose.material.icons.Icons.Default.CheckCircle", "androidx.compose.material.icons.filled.CheckCircle")
content = content.replace("androidx.compose.material.icons.Icons.Default.Favorite", "androidx.compose.material.icons.filled.Favorite")
content = content.replace("androidx.compose.material.icons.Icons.Default.Warning", "androidx.compose.material.icons.filled.Warning")
content = content.replace("androidx.compose.material.icons.Icons.Default.Info", "androidx.compose.material.icons.filled.Info")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
