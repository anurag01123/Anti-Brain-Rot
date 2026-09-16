import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_surface = """                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {"""

new_surface = """                color = Color.Transparent,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
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
                            quadraticBezierTo(size.width * 0.78f, size.height * 0.15f, size.width * 0.81f, size.height * 0.2f)
                            quadraticBezierTo(size.width * 0.84f, size.height * 0.15f, size.width * 0.87f, size.height * 0.2f)
                        }
                        drawPath(path = path, color = primaryColor.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        
                        // Draw bird 2
                        val path2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.65f, size.height * 0.35f)
                            quadraticBezierTo(size.width * 0.67f, size.height * 0.32f, size.width * 0.69f, size.height * 0.35f)
                            quadraticBezierTo(size.width * 0.71f, size.height * 0.32f, size.width * 0.73f, size.height * 0.35f)
                        }
                        drawPath(path = path2, color = primaryColor.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""

content = content.replace(old_surface, new_surface)

# Fix the closing brackets for the Box
old_closing = """                        }
                    }
                }
            }
        },
        bottomBar = {"""

new_closing = """                        }
                    }
                }
                }
            }
        },
        bottomBar = {"""

content = content.replace(old_closing, new_closing)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
