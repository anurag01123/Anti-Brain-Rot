import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Remove the extra rounded warning import
content = content.replace("import androidx.compose.material.icons.rounded.Warning\n", "")

# I also need to ensure that when we refer to them, we use the fully qualified name or just Icons.Rounded... wait, we replaced it with androidx.compose.material.icons.filled.Warning! So we don't need Icons.Default.Warning anymore.
# Let's replace `androidx.compose.material.icons.filled.Warning` in the code with `androidx.compose.material.icons.filled.Warning`... wait, they are just properties.
# To use an icon, it's `Icons.Filled.Warning` or `Icons.Rounded.Warning`. 
# Wait, Jetpack Compose syntax is `Icons.Filled.Warning`.
# If I just used `androidx.compose.material.icons.filled.Warning` directly as a value, it won't work because it's an extension property on `Icons.Filled`.
# So `Icons.Filled.Star` works if `androidx.compose.material.icons.filled.Star` is imported.

# Let's rewrite the icons properly in code!
content = content.replace("androidx.compose.material.icons.filled.Star", "androidx.compose.material.icons.Icons.Filled.Star")
content = content.replace("androidx.compose.material.icons.filled.ThumbUp", "androidx.compose.material.icons.Icons.Filled.ThumbUp")
content = content.replace("androidx.compose.material.icons.filled.CheckCircle", "androidx.compose.material.icons.Icons.Filled.CheckCircle")
content = content.replace("androidx.compose.material.icons.filled.Favorite", "androidx.compose.material.icons.Icons.Filled.Favorite")
content = content.replace("androidx.compose.material.icons.filled.Warning", "androidx.compose.material.icons.Icons.Filled.Warning")
content = content.replace("androidx.compose.material.icons.filled.Info", "androidx.compose.material.icons.Icons.Filled.Info")

# But wait, that will also ruin the imports!
content = content.replace("import androidx.compose.material.icons.Icons.Filled.Star", "import androidx.compose.material.icons.filled.Star")
content = content.replace("import androidx.compose.material.icons.Icons.Filled.ThumbUp", "import androidx.compose.material.icons.filled.ThumbUp")
content = content.replace("import androidx.compose.material.icons.Icons.Filled.CheckCircle", "import androidx.compose.material.icons.filled.CheckCircle")
content = content.replace("import androidx.compose.material.icons.Icons.Filled.Favorite", "import androidx.compose.material.icons.filled.Favorite")
content = content.replace("import androidx.compose.material.icons.Icons.Filled.Warning", "import androidx.compose.material.icons.filled.Warning")
content = content.replace("import androidx.compose.material.icons.Icons.Filled.Info", "import androidx.compose.material.icons.filled.Info")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
