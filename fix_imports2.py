import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix the broken imports
content = content.replace("import Icons.Default.Star", "import androidx.compose.material.icons.filled.Star")
content = content.replace("import Icons.Default.ThumbUp", "import androidx.compose.material.icons.filled.ThumbUp")
content = content.replace("import Icons.Default.CheckCircle", "import androidx.compose.material.icons.filled.CheckCircle")
content = content.replace("import Icons.Default.Favorite", "import androidx.compose.material.icons.filled.Favorite")
content = content.replace("import Icons.Default.Info", "import androidx.compose.material.icons.filled.Info")
content = content.replace("import Icons.Default.Warning", "import androidx.compose.material.icons.filled.Warning")
content = content.replace("import Icons.Default.Add", "import androidx.compose.material.icons.filled.Add")

# Also, there's `Icons.Default.Warning` which might be conflicting with something else? 
# The error says "Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch: val Icons.Rounded.Warning"
# This is because I previously replaced some icons.
content = content.replace("Icons.Default.Warning", "androidx.compose.material.icons.filled.Warning")
content = content.replace("Icons.Default.Info", "androidx.compose.material.icons.filled.Info")
content = content.replace("Icons.Default.Star", "androidx.compose.material.icons.filled.Star")
content = content.replace("Icons.Default.ThumbUp", "androidx.compose.material.icons.filled.ThumbUp")
content = content.replace("Icons.Default.CheckCircle", "androidx.compose.material.icons.filled.CheckCircle")
content = content.replace("Icons.Default.Favorite", "androidx.compose.material.icons.filled.Favorite")

# Clean up broken "import androidx.compose.material.icons.filled.Warning" if it was double-replaced? Let's just sed it.
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
