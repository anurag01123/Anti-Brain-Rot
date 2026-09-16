import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I will just write a small regex to wrap the left hand side in parens and the right hand side in parens
# Let's just find the `togetherWith` and make sure it has ( )
content = re.sub(
    r'androidx\.compose\.animation\.slideInHorizontally \{ width -> width \} \+ androidx\.compose\.animation\.fadeIn\(\)\)\.togetherWith\(androidx\.compose\.animation\.slideOutHorizontally \{ width -> -width \} \+ androidx\.compose\.animation\.fadeOut\(\)',
    r'(androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut())',
    content
)

content = re.sub(
    r'androidx\.compose\.animation\.slideInHorizontally \{ width -> -width \} \+ androidx\.compose\.animation\.fadeIn\(\)\)\.togetherWith\(androidx\.compose\.animation\.slideOutHorizontally \{ width -> width \} \+ androidx\.compose\.animation\.fadeOut\(\)',
    r'(androidx.compose.animation.slideInHorizontally { width -> -width } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally { width -> width } + androidx.compose.animation.fadeOut())',
    content
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
