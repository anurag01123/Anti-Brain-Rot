import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix 1: Remove ArrowForward from Time Detoxed
old_detox = """                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }"""

content = content.replace(old_detox, "")

# Fix 2: Rewrite Overnight Block
# Need to use regex because of potential whitespace or unseen characters.
pattern_overnight = re.compile(r'// OVERNIGHT BLOCK.*?val isOvernightBlock by viewModel\.isOvernightBlock\.collectAsStateWithLifecycle\(\)\s+Card\(.*?\n\s+onClick = \{ viewModel\.setOvernightBlock\(!isOvernightBlock\) \}\n\s+\) \{\n\s+Box\(.*?\) \{\n\s+Row\(.*?\) \{\n.*?\n.*?\n.*?\n\s+IconButton\(\n.*?\n.*?\n\s+\) \{\n\s+Icon\(.*?\n\s+\}\n\s+\}\n\s+Text\("☁️".*?\n\s+\}\n\s+\}', re.DOTALL)

new_overnight = """// OVERNIGHT BLOCK
                        val isOvernightBlock by viewModel.isOvernightBlock.collectAsStateWithLifecycle()
                        val overnightStartHour by viewModel.overnightStartHour.collectAsStateWithLifecycle()
                        val overnightEndHour by viewModel.overnightEndHour.collectAsStateWithLifecycle()
                        var showOvernightDialog by remember { mutableStateOf(false) }
                        
                        val startHourDisplay = if (overnightStartHour > 12) "${overnightStartHour - 12} PM" else if (overnightStartHour == 12) "12 PM" else if (overnightStartHour == 0) "12 AM" else "${overnightStartHour} AM"
                        val endHourDisplay = if (overnightEndHour > 12) "${overnightEndHour - 12} PM" else if (overnightEndHour == 12) "12 PM" else if (overnightEndHour == 0) "12 AM" else "${overnightEndHour} AM"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(100.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiaryContainer),
                            onClick = { viewModel.setOvernightBlock(!isOvernightBlock) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.CenterStart) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Overnight Block", fontWeight = FontWeight.Bold, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (isOvernightBlock) "Active ($startHourDisplay - $endHourDisplay)" else "Inactive", style = MaterialTheme.typography.labelLarge, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    IconButton(
                                        onClick = { showOvernightDialog = true },
                                        modifier = Modifier.background(Color.Transparent).size(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.setOvernightBlock(!isOvernightBlock) },
                                        modifier = Modifier.background(if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(if (isOvernightBlock) androidx.compose.material.icons.Icons.Filled.Warning else androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        if (showOvernightDialog) {
                            AlertDialog(
                                onDismissRequest = { showOvernightDialog = false },
                                title = { Text("Overnight Block Time") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Start Hour (24h format):")
                                        var sHour by remember { mutableFloatStateOf(overnightStartHour.toFloat()) }
                                        Slider(value = sHour, onValueChange = { sHour = it }, valueRange = 0f..23f, steps = 22)
                                        Text("${sHour.toInt()}:00")
                                        
                                        Text("End Hour (24h format):")
                                        var eHour by remember { mutableFloatStateOf(overnightEndHour.toFloat()) }
                                        Slider(value = eHour, onValueChange = { eHour = it }, valueRange = 0f..23f, steps = 22)
                                        Text("${eHour.toInt()}:00")
                                        
                                        LaunchedEffect(sHour, eHour) {
                                            viewModel.setOvernightStartHour(sHour.toInt())
                                            viewModel.setOvernightEndHour(eHour.toInt())
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showOvernightDialog = false }) { Text("Done") }
                                }
                            )
                        }"""

new_content, count = pattern_overnight.subn(new_overnight, content)
print(f"Replaced {count} instances of overnight block.")

if count > 0:
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(new_content)
