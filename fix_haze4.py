import re

with open("app/src/main/java/com/example/ui/components/GlassSurface.kt", "r") as f:
    content = f.read()

content = content.replace("import dev.chrisbanes.haze.hazeEffect", "import dev.chrisbanes.haze.hazeChild")
content = content.replace("hazeEffect(state = hazeState)", "Modifier.hazeChild(state = hazeState)")
content = content.replace("Modifier.Modifier.hazeChild", "Modifier.hazeChild")

with open("app/src/main/java/com/example/ui/components/GlassSurface.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add imports
imports = """
import dev.chrisbanes.haze.haze
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
"""
if "import dev.chrisbanes.haze.haze" not in content:
    content = content.replace("import android.app.AppOpsManager", imports.strip() + "\\nimport android.app.AppOpsManager")

# Fix haze call
content = content.replace("androidx.compose.ui.Modifier.haze(state = hazeState)", "Modifier.haze(state = hazeState)")
content = content.replace("Modifier.Modifier.haze", "Modifier.haze")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
