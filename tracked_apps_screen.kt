    val appUsages by viewModel.appUsages.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add App") },
                text = { Text("Block App") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            // Warning if Usage Stats permission is not granted
            var hasPermission by remember { mutableStateOf(UsageUtils.hasUsageStatsPermission(context)) }
            
            // Recheck permission when returning to this screen
            LaunchedEffect(Unit) {
                while (true) {
                    hasPermission = UsageUtils.hasUsageStatsPermission(context)
                    kotlinx.coroutines.delay(1000)
                }
            }

            AnimatedVisibility(visible = !hasPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Usage Access Required", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("The app cannot track time because it does not have Usage Access. Tap to grant permission.", color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            if (trackedApps.isEmpty()) {
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
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(36.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = totalProgress,
                                    animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
                                    label = "progress"
                                )
                                Text("Total Screen Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(24.dp))
                                Box(
                                    modifier = Modifier.size(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeWidth = 20.dp
                                    )
                                    CircularProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = if (animatedProgress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        strokeWidth = 20.dp,
                                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (animatedProgress >= 1f) "😤" else if (animatedProgress >= 0.8f) "😅" else "🤩",
                                            fontSize = 42.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AnimatedContent(targetState = totalUsageMins, label = "total_mins") { mins ->
                                            Text(
                                                text = "${mins}m",
                                                style = MaterialTheme.typography.displayMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "/ ${totalLimitMins}m limit",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
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
            }
        }
    }

    if (showAddDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var step by remember { mutableStateOf(1) }
        var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
        var limitMinutes by remember { mutableFloatStateOf(20f) }

        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AnimatedContent(targetState = step, label = "step_transition") { targetStep ->
                if (targetStep == 1) {
                    Column(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                        Text(
                            "Select App to Block", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
