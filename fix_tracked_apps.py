import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# We need to replace the `if (trackedApps.isEmpty()) { ... } else { ... LazyColumn { ... } }` block.
# Let's find the start of `if (trackedApps.isEmpty())` up to `val totalUsageMs = appUsages.values.sum()`
old_if = """            if (trackedApps.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Lock,
                    title = "No apps blocked",
                    subtitle = "Add an app to start tracking its usage limit.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val totalUsageMs = appUsages.values.sum()
                val totalLimitMins = trackedApps.sumOf { it.dailyLimitMinutes }
                val totalUsageMins = totalUsageMs / (1000 * 60)
                val totalProgress = if (totalLimitMins > 0) (totalUsageMins.toFloat() / totalLimitMins).coerceIn(0f, 1f) else 0f

                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {"""

new_code = """            val totalUsageMs = appUsages.values.sum()
            val totalLimitMins = trackedApps.sumOf { it.dailyLimitMinutes }
            val totalUsageMins = totalUsageMs / (1000 * 60)
            val totalProgress = if (totalLimitMins > 0) (totalUsageMins.toFloat() / totalLimitMins).coerceIn(0f, 1f) else 0f

            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {"""

content = content.replace(old_if, new_code)

old_items = """                    items(trackedApps, key = { it.packageName }) { app ->
                        TrackedAppCard(
                            app = app,
                            usageMs = appUsages[app.packageName] ?: 0L,
                            onDelete = { viewModel.removeTrackedApp(app.packageName) },
                            onToggle = { isActive -> viewModel.toggleAppActive(app.packageName, isActive) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }"""

new_items = """                    if (trackedApps.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Rounded.Lock,
                                title = "No apps blocked",
                                subtitle = "Add an app to start tracking its usage limit.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                            )
                        }
                    } else {
                        items(trackedApps, key = { it.packageName }) { app ->
                            TrackedAppCard(
                                app = app,
                                usageMs = appUsages[app.packageName] ?: 0L,
                                onDelete = { viewModel.removeTrackedApp(app.packageName) },
                                onToggle = { isActive -> viewModel.toggleAppActive(app.packageName, isActive) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }"""
                
content = content.replace(old_items, new_items)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
