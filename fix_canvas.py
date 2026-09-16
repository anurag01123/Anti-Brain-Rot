import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_canvas = """                                // Simple Wave chart representation
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                    val path = androidx.compose.ui.graphics.Path()
                                    path.moveTo(0f, size.height)
                                    path.lineTo(0f, size.height * 0.7f)
                                    path.cubicTo(size.width * 0.25f, size.height * 0.9f, size.width * 0.4f, size.height * 0.2f, size.width * 0.6f, size.height * 0.5f)
                                    path.cubicTo(size.width * 0.8f, size.height * 0.8f, size.width * 0.9f, size.height * 0.3f, size.width, size.height * 0.4f)
                                    path.lineTo(size.width, size.height)
                                    path.close()
                                    
                                    val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                        )
                                    )
                                    drawPath(path, brush)
                                }"""

new_canvas = """                                // Simple Wave chart representation
                                val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                    val path = androidx.compose.ui.graphics.Path()
                                    path.moveTo(0f, size.height)
                                    path.lineTo(0f, size.height * 0.7f)
                                    path.cubicTo(size.width * 0.25f, size.height * 0.9f, size.width * 0.4f, size.height * 0.2f, size.width * 0.6f, size.height * 0.5f)
                                    path.cubicTo(size.width * 0.8f, size.height * 0.8f, size.width * 0.9f, size.height * 0.3f, size.width, size.height * 0.4f)
                                    path.lineTo(size.width, size.height)
                                    path.close()
                                    
                                    val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            primaryContainerColor.copy(alpha = 0.8f),
                                            primaryContainerColor.copy(alpha = 0.2f)
                                        )
                                    )
                                    drawPath(path, brush)
                                }"""

content = content.replace(old_canvas, new_canvas)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
