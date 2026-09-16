import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix Icons
content = content.replace("Icons.Rounded.Star", "androidx.compose.material.icons.Icons.Default.Star")
content = content.replace("Icons.Rounded.ThumbUp", "androidx.compose.material.icons.Icons.Default.ThumbUp")
content = content.replace("Icons.Rounded.CheckCircle", "androidx.compose.material.icons.Icons.Default.CheckCircle")
content = content.replace("Icons.Rounded.Favorite", "androidx.compose.material.icons.Icons.Default.Favorite")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.WbSunny", "androidx.compose.material.icons.Icons.Default.Warning")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.NightlightRound", "androidx.compose.material.icons.Icons.Default.Info")

# Fix primaryContainerContainer
content = content.replace("MaterialTheme.colorScheme.primaryContainerContainer", "MaterialTheme.colorScheme.primaryContainer")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
