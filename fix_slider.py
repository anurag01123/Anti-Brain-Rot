import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_dialog = """                        if (showOvernightDialog) {
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

new_dialog = """                        if (showOvernightDialog) {
                            AlertDialog(
                                onDismissRequest = { showOvernightDialog = false },
                                title = { Text("Overnight Block Time") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Start Hour:")
                                        var sHour by remember { mutableFloatStateOf(overnightStartHour.toFloat()) }
                                        Slider(value = sHour, onValueChange = { sHour = it }, valueRange = 0f..23f, steps = 22)
                                        val sHourInt = kotlin.math.round(sHour).toInt()
                                        val sDisplayStr = if (sHourInt > 12) "${sHourInt - 12} PM" else if (sHourInt == 12) "12 PM" else if (sHourInt == 0) "12 AM" else "${sHourInt} AM"
                                        Text(sDisplayStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        
                                        Text("End Hour:")
                                        var eHour by remember { mutableFloatStateOf(overnightEndHour.toFloat()) }
                                        Slider(value = eHour, onValueChange = { eHour = it }, valueRange = 0f..23f, steps = 22)
                                        val eHourInt = kotlin.math.round(eHour).toInt()
                                        val eDisplayStr = if (eHourInt > 12) "${eHourInt - 12} PM" else if (eHourInt == 12) "12 PM" else if (eHourInt == 0) "12 AM" else "${eHourInt} AM"
                                        Text(eDisplayStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        
                                        LaunchedEffect(sHour, eHour) {
                                            viewModel.setOvernightStartHour(kotlin.math.round(sHour).toInt())
                                            viewModel.setOvernightEndHour(kotlin.math.round(eHour).toInt())
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showOvernightDialog = false }) { Text("Done") }
                                }
                            )
                        }"""

if old_dialog in content:
    content = content.replace(old_dialog, new_dialog)
    print("Replaced exact match.")
else:
    print("Exact match not found. Trying regex.")
    # In case of slight whitespace differences
    pattern = re.compile(r'if \(showOvernightDialog\).*?TextButton\(onClick = \{ showOvernightDialog = false \}\) \{ Text\("Done"\) \}\n\s+\}\n\s+\)\n\s+\}', re.DOTALL)
    content, count = pattern.subn(new_dialog, content)
    print(f"Replaced {count} using regex.")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
