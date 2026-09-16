import re

with open("app/src/main/java/com/example/BlockActivity.kt", "r") as f:
    content = f.read()

if 'val blockReason = intent.getStringExtra("BLOCK_REASON")' not in content:
    content = content.replace(
        'val blockedAppName = intent.getStringExtra("BLOCKED_APP") ?: "Target App"',
        'val blockedAppName = intent.getStringExtra("BLOCKED_APP") ?: "Target App"\n        val blockReason = intent.getStringExtra("BLOCK_REASON")'
    )
    
old_ui = """                            Text(
                                text = blockedAppName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )"""

new_ui = """                            Text(
                                text = blockedAppName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (blockReason != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = blockReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }"""
                            
content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/example/BlockActivity.kt", "w") as f:
    f.write(content)
