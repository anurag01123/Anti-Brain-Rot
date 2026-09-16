import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

pattern_emptystate = re.compile(r'fun EmptyState\(.*?\)\s*\{\s*Column\(\s*modifier = modifier,\s*horizontalAlignment = Alignment\.CenterHorizontally,\s*verticalArrangement = Arrangement\.Center\s*\)\s*\{\s*Icon\(.*?\)\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)', re.DOTALL)

new_emptystate = """fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Nature Vector Illustration
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
            // Tree
            val treePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.4f, size.height * 0.9f)
                lineTo(size.width * 0.45f, size.height * 0.5f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.5f, size.height * 0.1f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.55f, size.height * 0.5f)
                lineTo(size.width * 0.6f, size.height * 0.9f)
                close()
            }
            drawPath(path = treePath, color = primary.copy(alpha = 0.6f))
            // Cloud
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.25f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height * 0.2f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.6f, size.height * 0.25f))
        }
        Spacer(modifier = Modifier.height(16.dp))"""

new_content, count = pattern_emptystate.subn(new_emptystate, content)
print(f"Replaced {count} instances of EmptyState.")

if count > 0:
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(new_content)
