import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_bs = """        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {"""

new_bs = """        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            androidx.activity.compose.BackHandler(enabled = step == 2) { step = 1 }"""

content = content.replace(old_bs, new_bs)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
