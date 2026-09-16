import re

with open("app/src/main/java/com/example/BlockActivity.kt", "r") as f:
    content = f.read()

old_floating = """                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 60.dp)
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = alpha),
                            modifier = Modifier.size(56.dp)
                        )
                    }"""

new_floating = """                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 60.dp)
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
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
                        }
                    }"""

if old_floating in content:
    content = content.replace(old_floating, new_floating)
    print("Replaced floating icon with plant.")
else:
    print("Floating icon not found.")

with open("app/src/main/java/com/example/BlockActivity.kt", "w") as f:
    f.write(content)
