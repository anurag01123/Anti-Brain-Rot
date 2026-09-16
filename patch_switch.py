import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Replace the switch block with a button
start_idx = content.find("                Row(verticalAlignment = Alignment.CenterVertically) {")
end_idx = content.find("                        },\n                        colors = SwitchDefaults.colors(") 

if start_idx == -1:
    print("Could not find start")
else:
    # Let's just find the exact block using string replacement
    old_block = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    Switch(
                        checked = app.isActive,
                        onCheckedChange = { 
                            if (progress >= 1f) {
                                android.widget.Toast.makeText(context, "Cannot toggle a blocked app. Complete penalty calls or wait until tomorrow.", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onToggle(it) 
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
"""
    
    new_block = """                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(20.dp))
            
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (app.isActive) MaterialTheme.colorScheme.surfaceVariant else com.example.ui.theme.md_theme_light_secondary,
                    contentColor = if (app.isActive) MaterialTheme.colorScheme.onSurfaceVariant else com.example.ui.theme.md_theme_light_onSurface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (app.isActive) "Deactivate" else "Activate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
"""
    
    if old_block in content:
        content = content.replace(old_block, new_block)
        with open("app/src/main/java/com/example/MainActivity.kt", "w") as f_out:
            f_out.write(content)
        print("Success")
    else:
        print("Old block not found. Trying regex.")
        # fallback if exact string matching fails due to some spaces
