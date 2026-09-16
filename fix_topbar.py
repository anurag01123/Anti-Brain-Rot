import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I will replace the Box containing the Canvas with a call to CloudsAndBirds()
# Let's find the exact block for the top bar canvas
old_topbar = """                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                            // Draw cloud 1
                            drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 1.1f))
                            drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 1.2f))
                            drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.25f, center = androidx.compose.ui.geometry.Offset(size.width * 1.05f, size.height * 0.9f))
                            
                            // Draw cloud 2
                            drawCircle(color = secondaryColor.copy(alpha = 0.15f), radius = size.width * 0.1f, center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * -0.1f))
                            drawCircle(color = secondaryColor.copy(alpha = 0.15f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * -0.2f))
                            
                            // Draw bird 1
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.75f, size.height * 0.2f)
                                quadraticTo(size.width * 0.78f, size.height * 0.15f, size.width * 0.81f, size.height * 0.2f)
                                quadraticTo(size.width * 0.84f, size.height * 0.15f, size.width * 0.87f, size.height * 0.2f)
                            }
                            drawPath(path = path, color = primaryColor.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            
                            // Draw bird 2
                            val path2 = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.65f, size.height * 0.35f)
                                quadraticTo(size.width * 0.67f, size.height * 0.32f, size.width * 0.69f, size.height * 0.35f)
                                quadraticTo(size.width * 0.71f, size.height * 0.32f, size.width * 0.73f, size.height * 0.35f)
                            }
                            drawPath(path = path2, color = primaryColor.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            
                            // Draw some trees
                            val treePath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.1f, size.height)
                                lineTo(size.width * 0.12f, size.height * 0.6f)
                                lineTo(size.width * 0.14f, size.height)
                                close()
                            }
                            drawPath(path = treePath, color = secondaryColor.copy(alpha = 0.8f))
                            
                            val treePath2 = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.2f, size.height)
                                lineTo(size.width * 0.23f, size.height * 0.5f)
                                lineTo(size.width * 0.26f, size.height)
                                close()
                            }
                            drawPath(path = treePath2, color = secondaryColor.copy(alpha = 0.8f))
                        }"""

new_topbar = """                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        com.example.ui.components.CloudsAndBirds(modifier = Modifier.matchParentSize())"""

content = content.replace(old_topbar, new_topbar)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
