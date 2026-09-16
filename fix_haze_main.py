import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Add HazeState to MainScreen
old_mainscreen_start = """fun MainScreen(
    viewModel: MainViewModel,
    onCheckUsagePermission: () -> Unit,
    onCheckAccessibility: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Apps to Block", "Penalty Contacts", "Insights")
    val icons = listOf(Icons.Rounded.Lock, Icons.Rounded.Call, Icons.Rounded.DateRange)

    Scaffold("""

new_mainscreen_start = """fun MainScreen(
    viewModel: MainViewModel,
    onCheckUsagePermission: () -> Unit,
    onCheckAccessibility: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Apps to Block", "Penalty Contacts", "Insights")
    val icons = listOf(Icons.Rounded.Lock, Icons.Rounded.Call, Icons.Rounded.DateRange)
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }

    Scaffold(
        modifier = Modifier.dev.chrisbanes.haze.haze(state = hazeState),"""

content = content.replace(old_mainscreen_start, new_mainscreen_start)

# 2. Update bottomBar
old_bottombar = """        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.surface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }"""

new_bottombar = """        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                com.example.ui.components.GlassSurface(
                    hazeState = hazeState,
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            val backgroundColor by androidx.compose.animation.animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                animationSpec = com.example.ui.utils.MotionTokens.standard()
                            )
                            val contentColor by androidx.compose.animation.animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = com.example.ui.utils.MotionTokens.standard()
                            )
                            val view = androidx.compose.ui.platform.LocalView.current
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(backgroundColor)
                                    .clickable { 
                                        com.example.ui.utils.Haptics.playLightTick(view)
                                        selectedTab = index 
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icons[index],
                                        contentDescription = title,
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    androidx.compose.animation.AnimatedVisibility(visible = isSelected) {
                                        Text(
                                            text = title,
                                            color = contentColor,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }"""

content = content.replace(old_bottombar, new_bottombar)

# 3. Add haze to topBar GlassSurface
old_topbar_glass = """                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {"""

new_topbar_glass = """                com.example.ui.components.GlassSurface(
                    hazeState = hazeState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {"""

content = content.replace(old_topbar_glass, new_topbar_glass)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
