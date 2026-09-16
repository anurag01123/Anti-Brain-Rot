import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_icon1 = """                                    if (currentIcon != null) {
                                        Image(
                                            bitmap = currentIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {
                                        Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                    }"""

new_icon1 = """                                    androidx.compose.animation.Crossfade(targetState = currentIcon != null, label = "icon_load") { isLoaded ->
                                        if (isLoaded && currentIcon != null) {
                                            Image(
                                                bitmap = currentIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                            )
                                        } else {
                                            com.example.ui.components.ShimmerPlaceholder(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
                                        }
                                    }"""

content = content.replace(old_icon1, new_icon1)

old_icon2 = """                    val currentIcon = iconBitmap
                    if (currentIcon != null) {
                        Image(
                            bitmap = currentIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }"""

new_icon2 = """                    val currentIcon = iconBitmap
                    androidx.compose.animation.Crossfade(targetState = currentIcon != null, label = "icon_load2") { isLoaded ->
                        if (isLoaded && currentIcon != null) {
                            Image(
                                bitmap = currentIcon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            com.example.ui.components.ShimmerPlaceholder(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)))
                        }
                    }"""

content = content.replace(old_icon2, new_icon2)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
