import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Find the start of the item block for the card
start_idx = content.find("                    item {\n                        Card(\n                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),")

# Find the start of the items block
end_idx = content.find("                    items(trackedApps, key = { it.packageName }) { app ->")

if start_idx == -1 or end_idx == -1:
    print("Could not find blocks")
else:
    new_ui = """                    item {
                        // MODULE 8: TIME DETOXED
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text("Your Time Detoxed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text("${totalUsageMins / 60}h${totalUsageMins % 60}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
                                            Text(" today", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 6.dp, start = 8.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                                
                                // Simple Wave chart representation
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
                                            com.example.ui.theme.md_theme_light_primary.copy(alpha = 0.8f),
                                            com.example.ui.theme.md_theme_light_primaryContainer.copy(alpha = 0.2f)
                                        )
                                    )
                                    drawPath(path, brush)
                                }
                            }
                        }

                        // MODULE 7: APP MONITORING HEADER
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text("App Monitoring", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape).size(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // A couple fake social icons to match the design visually
                            Text("📸", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text("👔", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text("📘", fontSize = 24.sp)
                        }

                        // QUICK ACTIONS GRID
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).height(160.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.md_theme_light_secondary)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Set Anti-Doom\\nScrolling", fontWeight = FontWeight.Bold, color = com.example.ui.theme.md_theme_light_onSurface)
                                    Text("🌸", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart))
                                    Surface(
                                        color = com.example.ui.theme.md_theme_light_onSurface.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text("🕒 14m 53s", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f).height(160.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.md_theme_light_primary)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Block All Apps\\nNow", fontWeight = FontWeight.Bold, color = com.example.ui.theme.md_theme_light_onSurface)
                                    Text("🌟", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart))
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.align(Alignment.BottomEnd).background(MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        
                        // OVERNIGHT BLOCK
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(100.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.md_theme_light_tertiary)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.CenterStart) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("Overnight Block", fontWeight = FontWeight.Bold, color = com.example.ui.theme.md_theme_light_onSurface, modifier = Modifier.weight(1f))
                                    Text("0", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(" Night Streak", style = MaterialTheme.typography.labelLarge)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text("☁️", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart).offset(y = 20.dp))
                            }
                        }
                    }
\n"""

    new_content = content[:start_idx] + new_ui + content[end_idx:]
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f_out:
        f_out.write(new_content)
    print("Success")

