import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_analytics = """        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Weekly Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxVal = maxOf(1, stats.take(7).maxOfOrNull { it.urgesInterrupted + it.blockOccurrences } ?: 1)
                    
                    for (i in 6 downTo 0) {
                        val dayStat = stats.find { it.dateEpochDay == today - i }
                        val valSum = (dayStat?.urgesInterrupted ?: 0) + (dayStat?.blockOccurrences ?: 0)
                        val heightFraction = valSum.toFloat() / maxVal.toFloat()
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight(heightFraction.coerceAtLeast(0.05f))
                                    .background(if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = LocalDate.ofEpochDay(today - i).dayOfWeek.name.take(1),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }"""

new_analytics = """        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Weekly Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            
            com.example.ui.components.GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (stats.isEmpty() || stats.all { it.urgesInterrupted == 0 && it.blockOccurrences == 0 }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        com.example.ui.components.GrowingPlant(modifier = Modifier.size(80.dp), progress = 0.5f)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No friction history yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxVal = maxOf(1, stats.take(7).maxOfOrNull { it.urgesInterrupted + it.blockOccurrences } ?: 1)
                        
                        for (i in 6 downTo 0) {
                            val dayStat = stats.find { it.dateEpochDay == today - i }
                            val valSum = (dayStat?.urgesInterrupted ?: 0) + (dayStat?.blockOccurrences ?: 0)
                            val targetHeight = valSum.toFloat() / maxVal.toFloat()
                            val heightFraction by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = targetHeight.coerceAtLeast(0.05f),
                                animationSpec = com.example.ui.utils.MotionTokens.standard()
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .fillMaxHeight(heightFraction)
                                        .background(if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = LocalDate.ofEpochDay(today - i).dayOfWeek.name.take(1),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }"""

content = content.replace(old_analytics, new_analytics)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
