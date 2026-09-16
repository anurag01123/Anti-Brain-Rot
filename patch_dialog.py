import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_dialog = """        ModalBottomSheet(
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(installedApps) { app ->"""

new_dialog = """        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            AnimatedContent(targetState = step, label = "step_transition") { targetStep ->
                if (targetStep == 1) {
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredApps = installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Select App to Block", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, top = 16.dp, bottom = 8.dp)
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search apps...") },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->"""

content = content.replace(old_dialog, new_dialog)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
