import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix togetherWith
content = content.replace("androidx.compose.animation.fadeIn() androidx.compose.animation.togetherWith androidx.compose.animation.slideOutHorizontally", "androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally")
# Wait, let's just do a regex or replace the whole block
old_anim = """                    if (targetState > initialState) {
                        androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn() androidx.compose.animation.togetherWith androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut()
                    } else {
                        androidx.compose.animation.slideInHorizontally { width -> -width } + androidx.compose.animation.fadeIn() androidx.compose.animation.togetherWith androidx.compose.animation.slideOutHorizontally { width -> width } + androidx.compose.animation.fadeOut()
                    }"""

new_anim = """                    if (targetState > initialState) {
                        (androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut())
                    } else {
                        (androidx.compose.animation.slideInHorizontally { width -> -width } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally { width -> width } + androidx.compose.animation.fadeOut())
                    }"""

content = content.replace(old_anim, new_anim)

# Fix playHeavyClick
content = content.replace("playHeavyClick", "playWarning")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
