package com.example

import dev.chrisbanes.haze.haze
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextOverflow
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import com.example.utils.UsageUtils

import android.Manifest
import com.example.data.TrackedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.time.LocalDate


object AppIconCache {
    // 50 items cache to prevent OOM
    val cache = object : android.util.LruCache<String, ImageBitmap>(50) {}
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            
            LaunchedEffect(isDarkMode) {
                this@MainActivity.enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkMode) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
            }
            
            MyApplicationTheme(darkTheme = isDarkMode) {
                MainScreen(
                    viewModel = viewModel,
                    onCheckUsagePermission = { checkUsageStatsPermission() },
                    onCheckAccessibility = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
    }

    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCheckUsagePermission: () -> Unit,
    onCheckAccessibility: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Apps to Block", "Penalty Contacts", "Insights")
    val icons = listOf(Icons.Rounded.Lock, Icons.Rounded.Call, Icons.Rounded.DateRange)
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }

    Scaffold(
        modifier = Modifier.haze(state = hazeState),
        topBar = {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            
            AnimatedVisibility(visible = selectedTab == 0) {
                // Glass effect container
                com.example.ui.components.GlassSurface(
                    hazeState = hazeState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                            // Draw cloud 1
                            drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 1.1f))
                            drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 1.2f))
                            drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.25f, center = androidx.compose.ui.geometry.Offset(size.width * 1.05f, size.height * 0.9f))
                            
                            // Draw cloud 2
                            drawCircle(color = secondaryColor.copy(alpha = 0.15f), radius = size.width * 0.1f, center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * -0.1f))
                            drawCircle(color = secondaryColor.copy(alpha = 0.15f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * -0.2f))
                            
                            // Draw bird 1
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.75f, size.height * 0.2f)
                                quadraticTo(size.width * 0.78f, size.height * 0.15f, size.width * 0.81f, size.height * 0.2f)
                                quadraticTo(size.width * 0.84f, size.height * 0.15f, size.width * 0.87f, size.height * 0.2f)
                            }
                            drawPath(path = path, color = primaryColor.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            
                            // Draw bird 2
                            val path2 = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.65f, size.height * 0.35f)
                                quadraticTo(size.width * 0.67f, size.height * 0.32f, size.width * 0.69f, size.height * 0.35f)
                                quadraticTo(size.width * 0.71f, size.height * 0.32f, size.width * 0.73f, size.height * 0.35f)
                            }
                            drawPath(path = path2, color = primaryColor.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            
                            // Draw some trees
                            val treePath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.1f, size.height)
                                lineTo(size.width * 0.12f, size.height * 0.6f)
                                lineTo(size.width * 0.14f, size.height)
                                close()
                            }
                            drawPath(path = treePath, color = secondaryColor.copy(alpha = 0.3f))
                        }
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Star, contentDescription = "Goal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Your Goal", 
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    """Stay focused
and productive""", 
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                modifier = Modifier.clickable { viewModel.setDarkMode(!isDarkMode) }.clip(RoundedCornerShape(16.dp)),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        if (isDarkMode) "Light Theme" else "Dark Theme", 
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> TrackedAppsScreen(viewModel)
                1 -> ContactsScreen(viewModel)
                2 -> AnalyticsScreen(viewModel)
            }
        }
    }
}

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val unlockEvents by viewModel.todayUnlockEvents.collectAsStateWithLifecycle()
    val stats by viewModel.allDailyStats.collectAsStateWithLifecycle()
    val today = LocalDate.now().toEpochDay()
    val todayStat = stats.find { it.dateEpochDay == today }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Friction Scorecard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Urges Interrupted", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = "${todayStat?.urgesInterrupted ?: 0}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Limit Blocks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            text = "${todayStat?.blockOccurrences ?: 0}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Apps Unlocked & Time Extended", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(
                            text = "${todayStat?.unlockOccurrences ?: 0}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        if (unlockEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Unlock Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            items(unlockEvents) { event ->
                val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                val timeStr = timeFormat.format(java.util.Date(event.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.appName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("+${event.bonusMinutesGranted} min • $timeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Weekly Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            
            com.example.ui.components.GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (stats.isEmpty() || stats.all { it.urgesInterrupted == 0 && it.blockOccurrences == 0 }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        com.example.ui.components.GrowingPlant(modifier = Modifier.size(80.dp), progress = 0.5f)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No friction history yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxVal = maxOf(1, stats.take(7).maxOfOrNull { it.urgesInterrupted + it.blockOccurrences } ?: 1)
                        
                        for (i in 6 downTo 0) {
                            val dayStat = stats.find { it.dateEpochDay == today - i }
                            val valSum = (dayStat?.urgesInterrupted ?: 0) + (dayStat?.blockOccurrences ?: 0)
                            val targetHeight = valSum.toFloat() / maxVal.toFloat()
                            val heightFraction by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = targetHeight.coerceAtLeast(0.05f),
                                animationSpec = com.example.ui.utils.MotionTokens.standard()
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .fillMaxHeight(heightFraction)
                                        .background(if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = LocalDate.ofEpochDay(today - i).dayOfWeek.name.take(1),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Created by Anurag • v1.0.4", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedAppsScreen(viewModel: MainViewModel) {
    val trackedApps by viewModel.allTrackedApps.collectAsStateWithLifecycle()
    val appUsages = viewModel.appUsages
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold() { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            // Warning if Usage Stats permission is not granted
            var hasPermission by remember { mutableStateOf(UsageUtils.hasUsageStatsPermission(context)) }
            
            // Recheck permission when returning to this screen
            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        hasPermission = UsageUtils.hasUsageStatsPermission(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                            Icon(androidx.compose.material.icons.Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
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
                        // MODULE 8: TIME DETOXED
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text("Your Time Detoxed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text("${totalUsageMins / 60}h ${totalUsageMins % 60}m", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
                                            Text(" today", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 6.dp, start = 8.dp))
                                        }
                                    }

                                }
                                
                                // Simple Wave chart representation
                                val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                    val path = androidx.compose.ui.graphics.Path()
                                    path.moveTo(0f, size.height)
                                    path.lineTo(0f, size.height * 0.7f)
                                    path.cubicTo(size.width * 0.25f, size.height * 0.9f, size.width * 0.4f, size.height * 0.2f, size.width * 0.6f, size.height * 0.5f)
                                    path.cubicTo(size.width * 0.8f, size.height * 0.8f, size.width * 0.9f, size.height * 0.3f, size.width, size.height * 0.4f)
                                    path.lineTo(size.width, size.height)
                                    path.close()
                                    
                                    val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            primaryContainerColor.copy(alpha = 0.8f),
                                            primaryContainerColor.copy(alpha = 0.2f)
                                        )
                                    )
                                    drawPath(path, brush)
                                }
                            }
                        }

                        // MODULE 7: APP MONITORING HEADER
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text("App Monitoring", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape).size(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }

                        // QUICK ACTIONS GRID
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val isAntiDoom by viewModel.isAntiDoom.collectAsStateWithLifecycle()
                            Card(
                                modifier = Modifier.weight(1f).height(160.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isAntiDoom) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondaryContainer),
                                onClick = { viewModel.setAntiDoom(!isAntiDoom) }
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Set Anti-Doom\nScrolling", fontWeight = FontWeight.Bold, color = if (isAntiDoom) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                    Text(if (isAntiDoom) "🛡️" else "🌸", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart))
                                    Surface(
                                        color = (if (isAntiDoom) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text(if (isAntiDoom) "Active" else "5m Limit", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (isAntiDoom) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                            val isGlobalBlock by viewModel.isGlobalBlock.collectAsStateWithLifecycle()
                            Card(
                                modifier = Modifier.weight(1f).height(160.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isGlobalBlock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                                onClick = { viewModel.setGlobalBlock(!isGlobalBlock) }
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Block All Apps\nNow", fontWeight = FontWeight.Bold, color = if (isGlobalBlock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                                    Text(if (isGlobalBlock) "🛑" else "🌟", fontSize = 48.sp, modifier = Modifier.align(Alignment.BottomStart))
                                    IconButton(
                                        onClick = { viewModel.setGlobalBlock(!isGlobalBlock) },
                                        modifier = Modifier.align(Alignment.BottomEnd).background(if (isGlobalBlock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(if (isGlobalBlock) androidx.compose.material.icons.Icons.Filled.Warning else Icons.Default.ArrowForward, contentDescription = null, tint = if (isGlobalBlock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        
                        // OVERNIGHT BLOCK
                        val isOvernightBlock by viewModel.isOvernightBlock.collectAsStateWithLifecycle()
                        val overnightStartHour by viewModel.overnightStartHour.collectAsStateWithLifecycle()
                        val overnightEndHour by viewModel.overnightEndHour.collectAsStateWithLifecycle()
                        var showOvernightDialog by remember { mutableStateOf(false) }
                        
                        val startHourDisplay = if (overnightStartHour > 12) "${overnightStartHour - 12} PM" else if (overnightStartHour == 12) "12 PM" else if (overnightStartHour == 0) "12 AM" else "${overnightStartHour} AM"
                        val endHourDisplay = if (overnightEndHour > 12) "${overnightEndHour - 12} PM" else if (overnightEndHour == 12) "12 PM" else if (overnightEndHour == 0) "12 AM" else "${overnightEndHour} AM"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(100.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiaryContainer),
                            onClick = { viewModel.setOvernightBlock(!isOvernightBlock) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.CenterStart) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Overnight Block", fontWeight = FontWeight.Bold, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (isOvernightBlock) "Active ($startHourDisplay - $endHourDisplay)" else "Inactive", style = MaterialTheme.typography.labelLarge, color = if (isOvernightBlock) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    IconButton(
                                        onClick = { showOvernightDialog = true },
                                        modifier = Modifier.background(Color.Transparent).size(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.setOvernightBlock(!isOvernightBlock) },
                                        modifier = Modifier.background(if (isOvernightBlock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape).size(36.dp)
                                    ) {
                                        Icon(if (isOvernightBlock) androidx.compose.material.icons.Icons.Filled.Warning else androidx.compose.material.icons.Icons.Filled.ArrowForward, contentDescription = null, tint = if (isOvernightBlock) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        if (showOvernightDialog) {
                            AlertDialog(
                                onDismissRequest = { showOvernightDialog = false },
                                title = { Text("Overnight Block Time") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Start Hour:")
                                        var sHour by remember { mutableFloatStateOf(overnightStartHour.toFloat()) }
                                        Slider(value = sHour, onValueChange = { sHour = it }, valueRange = 0f..23f, steps = 22)
                                        val sHourInt = kotlin.math.round(sHour).toInt()
                                        val sDisplayStr = if (sHourInt > 12) "${sHourInt - 12} PM" else if (sHourInt == 12) "12 PM" else if (sHourInt == 0) "12 AM" else "${sHourInt} AM"
                                        Text(sDisplayStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        
                                        Text("End Hour:")
                                        var eHour by remember { mutableFloatStateOf(overnightEndHour.toFloat()) }
                                        Slider(value = eHour, onValueChange = { eHour = it }, valueRange = 0f..23f, steps = 22)
                                        val eHourInt = kotlin.math.round(eHour).toInt()
                                        val eDisplayStr = if (eHourInt > 12) "${eHourInt - 12} PM" else if (eHourInt == 12) "12 PM" else if (eHourInt == 0) "12 AM" else "${eHourInt} AM"
                                        Text(eDisplayStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        
                                        LaunchedEffect(sHour, eHour) {
                                            viewModel.setOvernightStartHour(kotlin.math.round(sHour).toInt())
                                            viewModel.setOvernightEndHour(kotlin.math.round(eHour).toInt())
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showOvernightDialog = false }) { Text("Done") }
                                }
                            )
                        }
                    }

                    if (trackedApps.isEmpty()) {
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
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            androidx.activity.compose.BackHandler(enabled = step == 2) { step = 1 }
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut())
                    } else {
                        (androidx.compose.animation.slideInHorizontally { width -> -width } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutHorizontally { width -> width } + androidx.compose.animation.fadeOut())
                    }.using(
                        androidx.compose.animation.SizeTransform(clip = false)
                    )
                },
                label = "step_transition"
            ) { targetStep ->
                if (targetStep == 1) {
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredApps = installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Select App to Block", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
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
                            items(filteredApps, key = { it.packageName }) { app ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { 
                                            selectedApp = app
                                            step = 2 
                                        }
                                        .padding(8.dp)
                                ) {
                    var iconBitmap by remember(app.packageName) { mutableStateOf<ImageBitmap?>(AppIconCache.cache.get(app.packageName)) }
                    LaunchedEffect(app.packageName) {
                        if (iconBitmap == null) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                    val bmp = drawableToImageBitmap(drawable)
                                    AppIconCache.cache.put(app.packageName, bmp)
                                    iconBitmap = bmp
                                } catch (e: Exception) {}
                            }
                        }
                    }
                                    val currentIcon = iconBitmap
                                    androidx.compose.animation.Crossfade(targetState = currentIcon != null, label = "icon_load") { isLoaded ->
                                        if (isLoaded && currentIcon != null) {
                                            Image(
                                                bitmap = currentIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                            )
                                        } else {
                                            com.example.ui.components.ShimmerPlaceholder(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        app.appName, 
                                        style = MaterialTheme.typography.labelSmall, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).height(450.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Set Daily Limit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedApp?.appName ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Text(
                            text = "${limitMinutes.toInt()} mins",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Slider(
                            value = limitMinutes,
                            onValueChange = { limitMinutes = it },
                            valueRange = 1f..180f,
                            steps = 179,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    selectedApp?.let {
                                        viewModel.addTrackedApp(it.packageName, it.appName, limitMinutes.toInt())
                                        showAddDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text("Save Lock")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TrackedAppCard(app: TrackedApp, usageMs: Long, onDelete: () -> Unit, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    val limitMs = app.dailyLimitMinutes * 60 * 1000L
    val targetProgress = if (limitMs > 0) (usageMs.toFloat() / limitMs).coerceIn(0f, 1f) else 0f
    val currentUsageMins = usageMs / (1000 * 60)
    
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "progress"
    )
    
    val targetColor = when {
        targetProgress >= 1f -> MaterialTheme.colorScheme.error
        targetProgress >= 0.8f -> Color(0xFFF59E0B) // Amber
        else -> MaterialTheme.colorScheme.primary
    }
    val color by androidx.compose.animation.animateColorAsState(
        targetValue = targetColor,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "color"
    )
    
    val statusText = when {
        targetProgress >= 1f -> "Locked"
        targetProgress >= 0.8f -> "Approaching"
        else -> "Accessible"
    }

    com.example.ui.components.GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    var iconBitmap by remember(app.packageName) { mutableStateOf<ImageBitmap?>(AppIconCache.cache.get(app.packageName)) }
                    LaunchedEffect(app.packageName) {
                        if (iconBitmap == null) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                    val bmp = drawableToImageBitmap(drawable)
                                    AppIconCache.cache.put(app.packageName, bmp)
                                    iconBitmap = bmp
                                } catch (e: Exception) {}
                            }
                        }
                    }
                    val currentIcon = iconBitmap
                    if (currentIcon != null) {
                        Image(
                            bitmap = currentIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(app.appName.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = app.appName, 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedContent(targetState = currentUsageMins, label = "usage_mins") { mins ->
                                Text(
                                    text = "$mins / ${app.dailyLimitMinutes}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            AnimatedContent(targetState = statusText, label = "status_text") { status ->
                                Surface(
                                    color = color.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = status,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                IconButton(
                    onClick = {
                        if (progress >= 1f) {
                            android.widget.Toast.makeText(context, "Cannot remove a blocked app. Complete penalty calls or wait until tomorrow.", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            onDelete()
                        }
                    },
                    modifier = Modifier.background(if (progress >= 1f) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp)).size(40.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = if (progress >= 1f) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            Button(
                onClick = { 
                    if (progress >= 1f && app.isActive) {
                        android.widget.Toast.makeText(context, "Cannot toggle a blocked app. Complete penalty calls.", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggle(!app.isActive)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (app.isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (app.isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (app.isActive) "Deactivate" else "Activate", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(viewModel: MainViewModel) {
    val contacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var hasCallLogPermission by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    
    val callLogPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCallLogPermission = isGranted
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, could launch picker but typically users click again
        }
    }

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
                onClick = { 
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        contactPickerLauncher.launch(null) 
                    } else {
                        contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                    }
                },
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Nature Vector Illustration
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "sway")
        val sway by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(3000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "tree_sway"
        )
        androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
            val treePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.4f, size.height * 0.9f)
                lineTo(size.width * 0.45f, size.height * 0.5f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.5f + sway, size.height * 0.1f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.55f, size.height * 0.5f)
                lineTo(size.width * 0.6f, size.height * 0.9f)
                close()
            }
            drawPath(path = treePath, color = primary.copy(alpha = 0.6f))
            
            val cloudOffset = sway * 0.5f
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f + cloudOffset, size.height * 0.25f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.4f + cloudOffset, size.height * 0.2f))
            drawCircle(color = secondary.copy(alpha = 0.4f), radius = size.width * 0.15f, center = androidx.compose.ui.geometry.Offset(size.width * 0.6f + cloudOffset, size.height * 0.25f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
    val bitmap = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1), 
            drawable.intrinsicHeight.coerceAtLeast(1), 
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp
    }
    return bitmap.asImageBitmap()
}

