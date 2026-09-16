import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

start_idx = content.find("                Row(verticalAlignment = Alignment.CenterVertically) {\\n                    val haptic")
if start_idx == -1:
    start_idx = content.find("                Row(verticalAlignment = Alignment.CenterVertically) {\n                    val haptic")

end_idx = content.find("        }\n    }\n}")
if end_idx == -1:
    end_idx = content.find("        }\n    }\n")

if start_idx != -1 and end_idx != -1:
    new_block = """                }
                IconButton(
                    onClick = {
                        if (progress >= 1f) {
                            android.widget.Toast.makeText(context, "Cannot remove a blocked app. Complete penalty calls or wait until tomorrow.", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            onDelete()
                        }
                    },
                    modifier = Modifier.background(if (progress >= 1f) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp)).size(40.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = if (progress >= 1f) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // BUTTON MODULE 4: ACTIVATE
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            Button(
                onClick = { 
                    if (progress >= 1f && app.isActive) {
                        android.widget.Toast.makeText(context, "Cannot toggle a blocked app. Complete penalty calls.", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggle(!app.isActive)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (app.isActive) MaterialTheme.colorScheme.surfaceVariant else com.example.ui.theme.md_theme_light_secondary,
                    contentColor = if (app.isActive) MaterialTheme.colorScheme.onSurfaceVariant else com.example.ui.theme.md_theme_light_onSurface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (app.isActive) "Deactivate" else "Activate", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
"""
    # Replace from start_idx to end_idx
    content = content[:start_idx] + new_block + content[end_idx:]
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Could not find boundaries")
