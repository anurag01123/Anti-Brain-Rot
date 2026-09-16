import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix 1b: tracked apps
old_tracked_apps_items = """                        items(trackedApps, key = { it.packageName }) { app ->
                            TrackedAppCard(
                                app = app,
                                usageMs = appUsages[app.packageName] ?: 0L,
                                onDelete = { viewModel.removeTrackedApp(app.packageName) },
                                onToggle = { isActive -> viewModel.toggleAppActive(app.packageName, isActive) },
                                modifier = Modifier.animateItem()
                            )
                        }"""

new_tracked_apps_items = """                        items(trackedApps, key = { it.packageName }) { app ->
                            val usage = appUsages[app.packageName] ?: 0L
                            val onDelete = remember(app.packageName) { { viewModel.removeTrackedApp(app.packageName) } }
                            val onToggle = remember(app.packageName) {
                                { isActive: Boolean -> viewModel.toggleAppActive(app.packageName, isActive) }
                            }
                            TrackedAppCard(
                                app = app,
                                usageMs = usage,
                                onDelete = onDelete,
                                onToggle = onToggle,
                                modifier = Modifier.animateItem()
                            )
                        }"""

content = content.replace(old_tracked_apps_items, new_tracked_apps_items)

# Fix 1b: contacts
old_contacts_items = """                        items(contacts, key = { it.phoneNumber }) { contact ->
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
                                        val view = androidx.compose.ui.platform.LocalView.current
                                        IconButton(onClick = { 
                                            com.example.ui.utils.Haptics.playWarning(view)
                                            viewModel.removeContact(contact.phoneNumber) 
                                        }, modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )
                            }
                        }"""

new_contacts_items = """                        items(contacts, key = { it.phoneNumber }) { contact ->
                            val view = androidx.compose.ui.platform.LocalView.current
                            val onRemove = remember(contact.phoneNumber) {
                                {
                                    com.example.ui.utils.Haptics.playWarning(view)
                                    viewModel.removeContact(contact.phoneNumber)
                                }
                            }
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
                                        IconButton(onClick = onRemove, modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )
                            }
                        }"""

content = content.replace(old_contacts_items, new_contacts_items)

# Fix 1c: permission polling
old_permission_poll = """            // Recheck permission when returning to this screen
            LaunchedEffect(Unit) {
                while (!hasPermission) {
                    hasPermission = UsageUtils.hasUsageStatsPermission(context)
                    if (hasPermission) break
                    kotlinx.coroutines.delay(1000)
                }
            }"""

new_permission_poll = """            // Recheck permission when returning to this screen
            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        hasPermission = UsageUtils.hasUsageStatsPermission(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }"""

content = content.replace(old_permission_poll, new_permission_poll)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
