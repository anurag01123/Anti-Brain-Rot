import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# For removeContact
old_remove = """                                    trailingContent = {
                                        IconButton(onClick = { viewModel.removeContact(contact.phoneNumber) }, modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))) {"""

new_remove = """                                    trailingContent = {
                                        val view = androidx.compose.ui.platform.LocalView.current
                                        IconButton(onClick = { 
                                            com.example.ui.utils.Haptics.playHeavyClick(view)
                                            viewModel.removeContact(contact.phoneNumber) 
                                        }, modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))) {"""

content = content.replace(old_remove, new_remove)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
