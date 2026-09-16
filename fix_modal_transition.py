import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_animated = """            AnimatedContent(targetState = step, label = "step_transition") { targetStep ->"""

new_animated = """            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn() androidx.compose.animation.togetherWith androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut()
                    } else {
                        androidx.compose.animation.slideInHorizontally { width -> -width } + androidx.compose.animation.fadeIn() androidx.compose.animation.togetherWith androidx.compose.animation.slideOutHorizontally { width -> width } + androidx.compose.animation.fadeOut()
                    }.using(
                        androidx.compose.animation.SizeTransform(clip = false)
                    )
                },
                label = "step_transition"
            ) { targetStep ->"""

content = content.replace(old_animated, new_animated)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
