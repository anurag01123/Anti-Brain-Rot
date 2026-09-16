import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

start_idx = content.find("fun TrackedAppCard(")
end_idx = content.find("@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun ContactsScreen")

if start_idx != -1 and end_idx != -1:
    new_card = """@Composable
fun TrackedAppCard(app: TrackedApp, usageMs: Long, onDelete: () -> Unit, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    val limitMs = app.dailyLimitMinutes * 60 * 1000L
    val progress = if (limitMs > 0) (usageMs.toFloat() / limitMs).coerceIn(0f, 1f) else 0f
    val currentUsageMins = usageMs / (1000 * 60)
    
    val color = when {
        progress >= 1f -> MaterialTheme.colorScheme.error
        progress >= 0.8f -> Color(0xFFF59E0B) // Amber
        else -> MaterialTheme.colorScheme.primary
    }
    
    val statusText = when {
        progress >= 1f -> "Locked 😤"
        progress >= 0.8f -> "Approaching 😅"
        else -> "Accessible 🤩"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val iconBitmap = remember(app.packageName) {
                        try {
                            val drawable = context.packageManager.getApplicationIcon(app.packageName)
                            drawableToImageBitmap(drawable)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(app.appName.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = app.appName, 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedContent(targetState = currentUsageMins, label = "usage_mins") { mins ->
                                Text(
                                    text = "$mins / ${app.dailyLimitMinutes}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            AnimatedContent(targetState = statusText, label = "status_text") { status ->
                                Surface(
                                    color = color.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = status,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
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
        }
    }
}
"""
    content = content[:start_idx] + new_card + content[end_idx:]
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Failed to find bounds")
