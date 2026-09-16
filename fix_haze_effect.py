import re

with open("app/src/main/java/com/example/ui/components/GlassSurface.kt", "r") as f:
    content = f.read()

content = content.replace("Modifier.hazeChild(state = hazeState)", "dev.chrisbanes.haze.hazeEffect(state = hazeState)")

with open("app/src/main/java/com/example/ui/components/GlassSurface.kt", "w") as f:
    f.write(content)
