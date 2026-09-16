import re

with open("app/src/main/java/com/example/BlockActivity.kt", "r") as f:
    content = f.read()

# Replace the background gradient
old_bg = """                val backgroundGradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundGradient)
                ) {"""

new_bg = """                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val primary = MaterialTheme.colorScheme.primary
                    val tertiary = MaterialTheme.colorScheme.tertiary
                    val errorColor = MaterialTheme.colorScheme.error
                    
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
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
                        drawPath(path2, color = primary.copy(alpha = 0.25f))
                        
                        // Small animal silhouette (e.g., bird or owl)
                        val animalPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.8f, size.height * 0.2f)
                            quadraticTo(size.width * 0.82f, size.height * 0.18f, size.width * 0.85f, size.height * 0.2f)
                            quadraticTo(size.width * 0.87f, size.height * 0.18f, size.width * 0.89f, size.height * 0.2f)
                        }
                        drawPath(animalPath, color = primary.copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }
"""
content = content.replace(old_bg, new_bg)

# Replace the contact card styling
old_card = """                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                val intent = Intent(Intent.ACTION_DIAL)
                                                intent.data = Uri.parse("tel:${contact.phoneNumber}")
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Call,
                                                        contentDescription = null,
                                                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                    )
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Column {
                                                        Text(contact.contactName, fontWeight = FontWeight.Bold)
                                                        Text(if (isCompleted) "Completed" else "Pending Call", style = MaterialTheme.typography.bodySmall, color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                                    }
                                                }
                                            }
                                        }"""

new_card = """                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                val intent = Intent(Intent.ACTION_DIAL)
                                                intent.data = Uri.parse("tel:${contact.phoneNumber}")
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha=0.3f) else Color.Transparent)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(48.dp).background(if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isCompleted) {
                                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                                        } else {
                                                            val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
                                                            androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                                                drawCircle(color = onSurfaceVar, radius = size.width * 0.4f, center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f))
                                                                drawCircle(color = onSurfaceVar, radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.2f))
                                                                drawCircle(color = onSurfaceVar, radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f))
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Column {
                                                        Text(contact.contactName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                        Text(if (isCompleted) "Completed" else "Pending Call", style = MaterialTheme.typography.bodySmall, color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                                    }
                                                }
                                            }
                                        }"""
content = content.replace(old_card, new_card)

with open("app/src/main/java/com/example/BlockActivity.kt", "w") as f:
    f.write(content)
