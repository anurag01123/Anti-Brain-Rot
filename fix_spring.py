import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_progress = """    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = com.example.ui.utils.MotionTokens.standard()
    )"""

new_progress = """    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "progress"
    )"""

content = content.replace(old_progress, new_progress)

old_color = """    val color by androidx.compose.animation.animateColorAsState(
        targetValue = targetColor,
        animationSpec = com.example.ui.utils.MotionTokens.standard()
    )"""

new_color = """    val color by androidx.compose.animation.animateColorAsState(
        targetValue = targetColor,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "color"
    )"""

content = content.replace(old_color, new_color)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
