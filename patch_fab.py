import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old = """    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add App") },
                text = { Text("Block App") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->"""
    
new = """    Scaffold() { innerPadding ->"""

content = content.replace(old, new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
