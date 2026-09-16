import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_canvas = """        androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
            // Tree
            val treePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.4f, size.height * 0.9f)
                lineTo(size.width * 0.45f, size.height * 0.5f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.5f, size.height * 0.1f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.55f, size.height * 0.5f)
                lineTo(size.width * 0.6f, size.height * 0.9f)
                close()
            }
            drawPath(path = treePath, color = primary.copy(alpha = 0.6f))
            // Cloud
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.25f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height * 0.2f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.6f, size.height * 0.25f))
        }"""

new_canvas = """        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "sway")
        val sway by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(3000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "tree_sway"
        )
        androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
            val treePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.4f, size.height * 0.9f)
                lineTo(size.width * 0.45f, size.height * 0.5f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.5f + sway, size.height * 0.1f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.55f, size.height * 0.5f)
                lineTo(size.width * 0.6f, size.height * 0.9f)
                close()
            }
            drawPath(path = treePath, color = primary.copy(alpha = 0.6f))
            
            val cloudOffset = sway * 0.5f
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f + cloudOffset, size.height * 0.25f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.4f + cloudOffset, size.height * 0.2f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.6f + cloudOffset, size.height * 0.25f))
        }"""

content = content.replace(old_canvas, new_canvas)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
