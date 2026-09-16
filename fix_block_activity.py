import re

with open("app/src/main/java/com/example/BlockActivity.kt", "r") as f:
    content = f.read()

old_canvas1 = """                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Night sky / dramatic background elements
                        drawCircle(color = errorColor.copy(alpha = 0.1f), radius = size.width * 0.8f, center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.1f))
                        
                        // Mountains
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.6f)
                            quadraticTo(size.width * 0.25f, size.height * 0.4f, size.width * 0.5f, size.height * 0.55f)
                            quadraticTo(size.width * 0.75f, size.height * 0.7f, size.width, size.height * 0.45f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = tertiary.copy(alpha = 0.2f))
                        
                        val path2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.7f)
                            quadraticTo(size.width * 0.3f, size.height * 0.6f, size.width * 0.6f, size.height * 0.75f)
                            quadraticTo(size.width * 0.8f, size.height * 0.8f, size.width, size.height * 0.65f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path2, color = primary.copy(alpha = 0.15f))
                    }"""

new_canvas1 = """                    com.example.ui.components.MountainScene(modifier = Modifier.fillMaxSize())"""

content = content.replace(old_canvas1, new_canvas1)

old_canvas2 = """                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Simple plant drawing
                            val plantPath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.5f, size.height)
                                quadraticTo(size.width * 0.5f, size.height * 0.6f, size.width * 0.2f, size.height * 0.3f)
                                quadraticTo(size.width * 0.5f, size.height * 0.4f, size.width * 0.5f, size.height * 0.7f)
                                
                                moveTo(size.width * 0.5f, size.height * 0.8f)
                                quadraticTo(size.width * 0.5f, size.height * 0.5f, size.width * 0.8f, size.height * 0.2f)
                                quadraticTo(size.width * 0.5f, size.height * 0.3f, size.width * 0.5f, size.height * 0.6f)
                                
                                moveTo(size.width * 0.5f, size.height)
                                lineTo(size.width * 0.5f, size.height * 0.2f)
                            }
                            drawPath(plantPath, color = primaryColor.copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        }"""

new_canvas2 = """                        com.example.ui.components.GrowingPlant(modifier = Modifier.fillMaxSize(), progress = 1f)"""

content = content.replace(old_canvas2, new_canvas2)

with open("app/src/main/java/com/example/BlockActivity.kt", "w") as f:
    f.write(content)
