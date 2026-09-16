import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Anti-Doom Scrolling Card Update
anti_doom_old = """                            Card(
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
                            }"""

anti_doom_new = """                            val isAntiDoom by viewModel.isAntiDoom.collectAsStateWithLifecycle()
                            Card(
                                modifier = Modifier.weight(1f).height(160.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isAntiDoom) MaterialTheme.colorScheme.onSurface else com.example.ui.theme.md_theme_light_secondary),
                                onClick = { viewModel.setAntiDoom(!isAntiDoom) }
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Set Anti-Doom\\nScrolling", fontWeight = FontWeight.Bold, color = if (isAntiDoom) MaterialTheme.colorScheme.surface else com.example.ui.theme.md_theme_light_onSurface)
                                    Text(if (isAntiDoom) "🛡️" else "🌸", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart))
                                    Surface(
                                        color = (if (isAntiDoom) MaterialTheme.colorScheme.surface else com.example.ui.theme.md_theme_light_onSurface).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text(if (isAntiDoom) "Active" else "5m Limit", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (isAntiDoom) MaterialTheme.colorScheme.surface else com.example.ui.theme.md_theme_light_onSurface)
                                    }
                                }
                            }"""

# Block All Apps Card Update
block_all_old = """                            Card(
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
                            }"""

block_all_new = """                            val isGlobalBlock by viewModel.isGlobalBlock.collectAsStateWithLifecycle()
                            Card(
                                modifier = Modifier.weight(1f).height(160.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isGlobalBlock) MaterialTheme.colorScheme.errorContainer else com.example.ui.theme.md_theme_light_primary),
                                onClick = { viewModel.setGlobalBlock(!isGlobalBlock) }
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Block All Apps\\nNow", fontWeight = FontWeight.Bold, color = if (isGlobalBlock) MaterialTheme.colorScheme.error else com.example.ui.theme.md_theme_light_onSurface)
                                    Text(if (isGlobalBlock) "🛑" else "🌟", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart))
                                    IconButton(
                                        onClick = { viewModel.setGlobalBlock(!isGlobalBlock) },
                                        modifier = Modifier.align(Alignment.BottomEnd).background(if (isGlobalBlock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(if (isGlobalBlock) androidx.compose.material.icons.Icons.Default.Warning else Icons.Default.ArrowForward, contentDescription = null, tint = if (isGlobalBlock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }"""

# Overnight Block Card Update
overnight_old = """                        // OVERNIGHT BLOCK
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
                        }"""

overnight_new = """                        // OVERNIGHT BLOCK
                        val isOvernightBlock by viewModel.isOvernightBlock.collectAsStateWithLifecycle()
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(100.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else com.example.ui.theme.md_theme_light_tertiary),
                            onClick = { viewModel.setOvernightBlock(!isOvernightBlock) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.CenterStart) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("Overnight Block", fontWeight = FontWeight.Bold, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface else com.example.ui.theme.md_theme_light_onSurface, modifier = Modifier.weight(1f))
                                    Text(if (isOvernightBlock) "Active (10 PM - 7 AM)" else "Inactive", style = MaterialTheme.typography.labelLarge, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface else com.example.ui.theme.md_theme_light_onSurface)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { viewModel.setOvernightBlock(!isOvernightBlock) },
                                        modifier = Modifier.background(if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(if (isOvernightBlock) androidx.compose.material.icons.Icons.Default.Warning else Icons.Default.ArrowForward, contentDescription = null, tint = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text("☁️", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart).offset(y = 20.dp))
                            }
                        }"""

content = content.replace(anti_doom_old, anti_doom_new)
content = content.replace(block_all_old, block_all_new)
content = content.replace(overnight_old, overnight_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
