import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_card = """    val limitMs = app.dailyLimitMinutes * 60 * 1000L
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
    ) {"""

new_card = """    val limitMs = app.dailyLimitMinutes * 60 * 1000L
    val targetProgress = if (limitMs > 0) (usageMs.toFloat() / limitMs).coerceIn(0f, 1f) else 0f
    val currentUsageMins = usageMs / (1000 * 60)
    
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = com.example.ui.utils.MotionTokens.standard()
    )
    
    val targetColor = when {
        targetProgress >= 1f -> MaterialTheme.colorScheme.error
        targetProgress >= 0.8f -> Color(0xFFF59E0B) // Amber
        else -> MaterialTheme.colorScheme.primary
    }
    val color by androidx.compose.animation.animateColorAsState(
        targetValue = targetColor,
        animationSpec = com.example.ui.utils.MotionTokens.standard()
    )
    
    val statusText = when {
        targetProgress >= 1f -> "Locked"
        targetProgress >= 0.8f -> "Approaching"
        else -> "Accessible"
    }

    com.example.ui.components.GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {"""

content = content.replace(old_card, new_card)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
