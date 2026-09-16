import re

with open("app/src/main/java/com/example/ui/components/GlassSurface.kt", "r") as f:
    content = f.read()

content = content.replace("import dev.chrisbanes.haze.hazeChild", "import dev.chrisbanes.haze.hazeEffect")
content = content.replace("dev.chrisbanes.haze.hazeEffect(state = hazeState)", "hazeEffect(state = hazeState)")

with open("app/src/main/java/com/example/ui/components/GlassSurface.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("Modifier.then(androidx.compose.ui.Modifier).let { dev.chrisbanes.haze.haze(it, state = hazeState) }", "dev.chrisbanes.haze.haze(androidx.compose.ui.Modifier, state = hazeState)")

# Add missing imports to MainActivity.kt
import_block = "import androidx.compose.animation.core.animateFloat\\nimport dev.chrisbanes.haze.haze\\nimport androidx.compose.animation.core.animateFloatAsState"
if "animateFloat" not in content:
    content = content.replace("package com.example", f"package com.example\\n\\n{import_block}")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
