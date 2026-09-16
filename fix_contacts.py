import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

pattern_contacts = re.compile(r'@Composable\nfun ContactsScreen.*?\}\n\}\n', re.DOTALL)

new_contacts = """@Composable
fun ContactsScreen(viewModel: MainViewModel) {
    val contacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.addContactFromUri(context, uri)
            }
        }
    )
    
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { contactPickerLauncher.launch(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Contact") },
                text = { Text("Add Contact") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Vector Art (Mountains and Greenery)
            val primary = MaterialTheme.colorScheme.primary
            val tertiary = MaterialTheme.colorScheme.tertiary
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    quadraticTo(size.width * 0.25f, size.height * 0.5f, size.width * 0.5f, size.height * 0.65f)
                    quadraticTo(size.width * 0.75f, size.height * 0.8f, size.width, size.height * 0.55f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = tertiary.copy(alpha = 0.1f))
                
                val path2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    quadraticTo(size.width * 0.3f, size.height * 0.7f, size.width * 0.6f, size.height * 0.85f)
                    quadraticTo(size.width * 0.8f, size.height * 0.9f, size.width, size.height * 0.75f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path2, color = primary.copy(alpha = 0.15f))
            }
            
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = contacts.size < 3) {
                    Card(
                        modifier = Modifier.padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "You need at least 3 penalty contacts to unblock apps. Currently: ${contacts.size}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                if (contacts.isEmpty()) {
                    EmptyState(
                        icon = Icons.Rounded.Call,
                        title = "No penalty contacts",
                        subtitle = "Add family or friends you must call to unblock an app.",
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = if(contacts.size < 3) 0.dp else 64.dp, bottom = innerPadding.calculateBottomPadding() + 80.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(contacts, key = { it.phoneNumber }) { contact ->
                            Card(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                shape = RoundedCornerShape(28.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                            ) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(26.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Vector animal avatar (simple bear/dog silhouette)
                                            val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
                                            androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                                drawCircle(color = onPrimaryContainer, radius = size.width * 0.4f, center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f))
                                                drawCircle(color = onPrimaryContainer, radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.2f))
                                                drawCircle(color = onPrimaryContainer, radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f))
                                            }
                                        }
                                    },
                                    headlineContent = { Text(contact.contactName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) },
                                    supportingContent = { Text(contact.phoneNumber, color = MaterialTheme.colorScheme.outline) },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.removeContact(contact.phoneNumber) }, modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
"""

new_content, count = pattern_contacts.subn(new_contacts, content)
print(f"Replaced {count} instances of ContactsScreen.")

if count > 0:
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(new_content)
