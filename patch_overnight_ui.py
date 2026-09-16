import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_overnight = """                        // OVERNIGHT BLOCK
                        val isOvernightBlock by viewModel.isOvernightBlock.collectAsStateWithLifecycle()
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(100.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiaryContainer),
                            onClick = { viewModel.setOvernightBlock(!isOvernightBlock) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.CenterStart) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("Overnight Block", fontWeight = FontWeight.Bold, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    Text(if (isOvernightBlock) "Active (10 PM - 7 AM)" else "Inactive", style = MaterialTheme.typography.labelLarge, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { viewModel.setOvernightBlock(!isOvernightBlock) },
                                        modifier = Modifier.background(if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(if (isOvernightBlock) androidx.compose.material.icons.Icons.Filled.Warning else Icons.Default.ArrowForward, contentDescription = null, tint = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }"""

new_overnight = """                        // OVERNIGHT BLOCK
                        val isOvernightBlock by viewModel.isOvernightBlock.collectAsStateWithLifecycle()
                        val overnightStartHour by viewModel.overnightStartHour.collectAsStateWithLifecycle()
                        val overnightEndHour by viewModel.overnightEndHour.collectAsStateWithLifecycle()
                        var showOvernightDialog by remember { mutableStateOf(false) }
                        val startHourDisplay = if (overnightStartHour > 12) "${overnightStartHour - 12} PM" else if (overnightStartHour == 12) "12 PM" else "${overnightStartHour} AM"
                        val endHourDisplay = if (overnightEndHour > 12) "${overnightEndHour - 12} PM" else if (overnightEndHour == 12) "12 PM" else "${overnightEndHour} AM"
                        
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (isOvernightBlock) "Active ($startHourDisplay - $endHourDisplay)" else "Inactive", style = MaterialTheme.typography.labelLarge, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        }
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
                                        Icon(if (isOvernightBlock) androidx.compose.material.icons.Icons.Filled.Warning else Icons.Default.ArrowForward, contentDescription = null, tint = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
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

content = content.replace(old_overnight, new_overnight)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
