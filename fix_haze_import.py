import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("Modifier.dev.chrisbanes.haze.haze(state = hazeState)", "Modifier.then(androidx.compose.ui.Modifier).let { dev.chrisbanes.haze.haze(it, state = hazeState) }")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
