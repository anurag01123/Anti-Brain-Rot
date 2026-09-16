import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_theme = """                    IconButton(
                        onClick = { viewModel.setDarkMode(!isDarkMode) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) androidx.compose.material.icons.Icons.Filled.Warning else androidx.compose.material.icons.Icons.Filled.Info,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }"""

new_theme = """                    Surface(
                        modifier = Modifier.clickable { viewModel.setDarkMode(!isDarkMode) }.clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                if (isDarkMode) "Light Theme" else "Dark Theme", 
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }"""

content = content.replace(old_theme, new_theme)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
