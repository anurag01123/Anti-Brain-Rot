import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

imports = """
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
"""

content = content.replace("import androidx.compose.material.icons.filled.Add", imports + "import androidx.compose.material.icons.filled.Add")

content = content.replace("androidx.compose.material.icons.filled.Star", "Icons.Default.Star")
content = content.replace("androidx.compose.material.icons.filled.ThumbUp", "Icons.Default.ThumbUp")
content = content.replace("androidx.compose.material.icons.filled.CheckCircle", "Icons.Default.CheckCircle")
content = content.replace("androidx.compose.material.icons.filled.Favorite", "Icons.Default.Favorite")
content = content.replace("androidx.compose.material.icons.filled.Warning", "Icons.Default.Warning")
content = content.replace("androidx.compose.material.icons.filled.Info", "Icons.Default.Info")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
