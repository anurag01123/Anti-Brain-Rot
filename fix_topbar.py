import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

pattern_topbar = re.compile(r'topBar = \{\s+val isDarkMode.*?\},(\s+)bottomBar = \{', re.DOTALL)

new_topbar = """topBar = {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            
            AnimatedVisibility(visible = selectedTab == 0) {
                // Glass effect container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
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
                            drawPath(path = treePath, color = secondaryColor.copy(alpha = 0.3f))
                        }
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(androidx.compose.material.icons.Icons.Rounded.Star, contentDescription = "Goal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Your Goal", 
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Stay focused\\nand productive", 
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                modifier = Modifier.clickable { viewModel.setDarkMode(!isDarkMode) }.clip(RoundedCornerShape(16.dp)),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        if (isDarkMode) "Light Theme" else "Dark Theme", 
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },\\1bottomBar = {"""

new_content, count = pattern_topbar.subn(new_topbar, content)
print(f"Replaced {count} instances of topBar.")

if count > 0:
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(new_content)
