import re

with open("app/src/main/java/com/example/BlockActivity.kt", "r") as f:
    content = f.read()

# Make the white bottom sheet look like glass too!
old_sheet = """                    // White bottom sheet
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {"""

new_sheet = """                    // Glass bottom sheet
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {"""

content = content.replace(old_sheet, new_sheet)

with open("app/src/main/java/com/example/BlockActivity.kt", "w") as f:
    f.write(content)
