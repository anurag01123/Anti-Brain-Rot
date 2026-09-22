package com.example.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppIconCache
import com.example.MainViewModel
import com.example.data.TrackedApp
import com.example.drawableToImageBitmap
import com.example.ui.components.EmptyState
import com.example.ui.components.GlassSurface
import com.example.ui.components.ShimmerPlaceholder
import com.example.ui.utils.FluidSprings
import com.example.ui.utils.Haptics
import com.example.ui.utils.bounceClick
import com.example.utils.UsageUtils
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedAppsScreen(
    viewModel: MainViewModel,
    hazeState: HazeState? = null,
    onNavigateToCharts: (() -> Unit)? = null
) {
    val trackedApps by viewModel.allTrackedApps.collectAsStateWithLifecycle()
    val appUsages = viewModel.appUsages
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current

    var hasUsageAccess by remember { mutableStateOf(UsageUtils.hasUsageStatsPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(UsageUtils.isAccessibilityServiceEnabled(context)) }
    var hasOverlay by remember { mutableStateOf(UsageUtils.hasOverlayPermission(context)) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = UsageUtils.hasUsageStatsPermission(context)
                hasAccessibility = UsageUtils.isAccessibilityServiceEnabled(context)
                hasOverlay = UsageUtils.hasOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allEnginesActive = hasUsageAccess && hasAccessibility && hasOverlay

    val totalUsageMs = trackedApps.sumOf { appUsages[it.packageName] ?: 0L }
    val totalUsageMins = (totalUsageMs / (1000 * 60)).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Engine Permissions Banner (if needed)
        if (!allEnginesActive) {
            item {
                EnginePermissionsBanner(
                    hasAccessibility = hasAccessibility,
                    hasUsageAccess = hasUsageAccess,
                    hasOverlay = hasOverlay,
                    onOpenAccessibility = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onOpenUsageAccess = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                    onOpenOverlay = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }

        // 2. Hero Detox Status Card
        item {
            DetoxHeroCard(
                totalUsageMins = totalUsageMins,
                trackedCount = trackedApps.size,
                onNavigateToCharts = onNavigateToCharts,
                hazeState = hazeState
            )
        }

        // 3. Quick Action Toggles Grid
        item {
            QuickActionsSection(viewModel = viewModel)
        }

        // 4. Section Header: App Monitoring
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp)
            ) {
                Column {
                    Text(
                        text = "App Monitoring",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${trackedApps.size} active rules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .bounceClick(scaleDown = 0.9f) {
                            showAddDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Tracked App",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 5. Tracked Apps List or Empty State
        if (trackedApps.isEmpty()) {
            item {
                EmptyState(
                    vectorRes = com.example.R.drawable.ic_digital_detox,
                    title = "No Apps Blocked Yet",
                    subtitle = "Add social media, games, or video apps to impose daily healthy limits.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
        } else {
            items(trackedApps, key = { it.packageName }) { app ->
                val usage = appUsages[app.packageName] ?: 0L
                val onDelete = remember(app.packageName) {
                    { viewModel.removeTrackedApp(app.packageName) }
                }
                val onToggle = remember(app.packageName) {
                    { isActive: Boolean -> viewModel.toggleAppActive(app.packageName, isActive) }
                }

                TrackedAppCardItem(
                    app = app,
                    usageMs = usage,
                    onDelete = onDelete,
                    onToggle = onToggle,
                    hazeState = hazeState,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    if (showAddDialog) {
        AddTrackedAppDialog(
            installedApps = installedApps,
            onDismiss = { showAddDialog = false },
            onAddApp = { app, limitMins ->
                viewModel.addTrackedApp(app.packageName, app.appName, limitMins)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DetoxHeroCard(
    totalUsageMins: Int,
    trackedCount: Int,
    onNavigateToCharts: (() -> Unit)?,
    hazeState: HazeState?
) {
    val hours = totalUsageMins / 60
    val mins = totalUsageMins % 60

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(22.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Time Detoxed Today",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${hours}h ${mins}m",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " tracked",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 6.dp, start = 8.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_zen_mind),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).padding(end = 6.dp)
                    )
                    if (onNavigateToCharts != null) {
                        FilledTonalButton(
                            onClick = onNavigateToCharts,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Rounded.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Charts", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Focus Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("Protected Apps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("$trackedCount apps", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            if (totalUsageMins < 120) "Healthy Pace" else "Elevated Use",
                            fontWeight = FontWeight.Bold,
                            color = if (totalUsageMins < 120) Color(0xFF10B981) else Color(0xFFF59E0B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(viewModel: MainViewModel) {
    val isAntiDoom by viewModel.isAntiDoom.collectAsStateWithLifecycle()
    val isGlobalBlock by viewModel.isGlobalBlock.collectAsStateWithLifecycle()
    val isOvernightBlock by viewModel.isOvernightBlock.collectAsStateWithLifecycle()
    val overnightStartHour by viewModel.overnightStartHour.collectAsStateWithLifecycle()
    val overnightEndHour by viewModel.overnightEndHour.collectAsStateWithLifecycle()
    var showOvernightDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Anti-Doom Scrolling Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .bounceClick(scaleDown = 0.96f) {
                        viewModel.setAntiDoom(!isAntiDoom)
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAntiDoom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isAntiDoom) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Anti-Doom\nScrolling",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isAntiDoom) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )

                    Icon(
                        imageVector = if (isAntiDoom) Icons.Rounded.Security else Icons.Rounded.Spa,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomStart),
                        tint = if (isAntiDoom) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = (if (isAntiDoom) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = if (isAntiDoom) "Active" else "5m Limit",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAntiDoom) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Block Listed Apps Now Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .bounceClick(scaleDown = 0.96f) {
                        viewModel.setGlobalBlock(!isGlobalBlock)
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGlobalBlock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isGlobalBlock) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Block Listed\nApps Now",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isGlobalBlock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface
                    )

                    Icon(
                        imageVector = if (isGlobalBlock) Icons.Rounded.Block else Icons.Rounded.Star,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomStart),
                        tint = if (isGlobalBlock) MaterialTheme.colorScheme.onError.copy(alpha = 0.85f) else MaterialTheme.colorScheme.secondary
                    )

                    Surface(
                        color = (if (isGlobalBlock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = if (isGlobalBlock) "LOCKED" else "Instant",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGlobalBlock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Overnight Block Card
        val startHourDisplay = if (overnightStartHour > 12) "${overnightStartHour - 12} PM" else if (overnightStartHour == 12) "12 PM" else if (overnightStartHour == 0) "12 AM" else "${overnightStartHour} AM"
        val endHourDisplay = if (overnightEndHour > 12) "${overnightEndHour - 12} PM" else if (overnightEndHour == 12) "12 PM" else if (overnightEndHour == 0) "12 AM" else "${overnightEndHour} AM"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOvernightBlock) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = if (isOvernightBlock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Overnight Sleep Shield",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOvernightBlock) "$startHourDisplay - $endHourDisplay (Active)" else "Off (Tap to configure)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOvernightBlock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = { showOvernightDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Configure hours",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Switch(
                    checked = isOvernightBlock,
                    onCheckedChange = { viewModel.setOvernightBlock(it) }
                )
            }
        }
    }

    if (showOvernightDialog) {
        AlertDialog(
            onDismissRequest = { showOvernightDialog = false },
            title = { Text("Overnight Sleep Shield Time", fontWeight = FontWeight.Bold) },
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
                TextButton(onClick = { showOvernightDialog = false }) { Text("Done", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun TrackedAppCardItem(
    app: TrackedApp,
    usageMs: Long,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val limitMs = app.dailyLimitMinutes * 60 * 1000L
    val targetProgress = if (limitMs > 0) (usageMs.toFloat() / limitMs).coerceIn(0f, 1f) else 0f
    val currentUsageMins = (usageMs / (1000 * 60)).toInt()

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "app_progress"
    )

    val targetColor = when {
        targetProgress >= 1f -> MaterialTheme.colorScheme.error
        targetProgress >= 0.8f -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusText = when {
        targetProgress >= 1f -> "Locked"
        targetProgress >= 0.8f -> "Approaching"
        else -> "Accessible"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    var iconBitmap by remember(app.packageName) {
                        mutableStateOf<ImageBitmap?>(AppIconCache.cache.get(app.packageName))
                    }
                    LaunchedEffect(app.packageName) {
                        if (iconBitmap == null) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                    val bmp = drawableToImageBitmap(drawable)
                                    AppIconCache.cache.put(app.packageName, bmp)
                                    iconBitmap = bmp
                                } catch (_: Exception) {}
                            }
                        }
                    }

                    val currentIcon = iconBitmap
                    if (currentIcon != null) {
                        Image(
                            bitmap = currentIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.appName.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$currentUsageMins / ${app.dailyLimitMinutes}m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = targetColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = targetColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = targetColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (progress >= 1f) {
                            android.widget.Toast.makeText(
                                context,
                                "Cannot remove a blocked app. Complete penalty calls or wait until tomorrow.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onDelete()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (progress >= 1f) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (progress >= 1f) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smooth Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = targetColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle active rule button
            val haptic = LocalHapticFeedback.current
            Button(
                onClick = {
                    if (progress >= 1f && app.isActive) {
                        android.widget.Toast.makeText(
                            context,
                            "Cannot toggle a blocked app. Complete penalty calls.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggle(!app.isActive)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (app.isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (app.isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (app.isActive) "Active Protection (Tap to Pause)" else "Protection Paused (Tap to Activate)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun EnginePermissionsBanner(
    hasAccessibility: Boolean,
    hasUsageAccess: Boolean,
    hasOverlay: Boolean,
    onOpenAccessibility: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenOverlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Blocker Engine Needs Setup", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Enable the required Android permissions so the anti-doom shield can detect and lock apps:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (!hasAccessibility) {
                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("1. Enable Blocker Accessibility Service", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (!hasUsageAccess) {
                Button(
                    onClick = onOpenUsageAccess,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("2. Grant Usage Access Permission", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (!hasOverlay) {
                Button(
                    onClick = onOpenOverlay,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("3. Grant Display Over Other Apps", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrackedAppDialog(
    installedApps: List<com.example.AppInfo>,
    onDismiss: () -> Unit,
    onAddApp: (com.example.AppInfo, Int) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedApp by remember { mutableStateOf<com.example.AppInfo?>(null) }
    var limitMinutes by remember { mutableIntStateOf(30) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        content = {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "add_step"
                ) { currentStep ->
                    if (currentStep == 1) {
                        var searchQuery by remember { mutableStateOf("") }
                        val filteredApps = remember(searchQuery, installedApps) {
                            installedApps.filter {
                                it.appName.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(520.dp)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Select App to Block",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search installed apps...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Rounded.Search, contentDescription = null)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredApps, key = { it.packageName }) { app ->
                                    val appName = app.appName

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .bounceClick(scaleDown = 0.92f) {
                                                selectedApp = app
                                                step = 2
                                            }
                                            .padding(6.dp)
                                    ) {
                                        var iconBitmap by remember(app.packageName) {
                                            mutableStateOf<ImageBitmap?>(AppIconCache.cache.get(app.packageName))
                                        }
                                        LaunchedEffect(app.packageName) {
                                            if (iconBitmap == null) {
                                                withContext(Dispatchers.IO) {
                                                    try {
                                                        val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                                        val bmp = drawableToImageBitmap(drawable)
                                                        AppIconCache.cache.put(app.packageName, bmp)
                                                        iconBitmap = bmp
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                        }

                                        val currentIcon = iconBitmap
                                        if (currentIcon != null) {
                                            Image(
                                                bitmap = currentIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                            )
                                        } else {
                                            ShimmerPlaceholder(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = appName,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Step 2: Set Daily Limit
                        val app = selectedApp
                        val appName = app?.appName ?: ""

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Set Daily Limit",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Choose how many minutes per day you can use $appName before lockdown.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "$limitMinutes minutes / day",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Slider(
                                value = limitMinutes.toFloat(),
                                onValueChange = { limitMinutes = it.toInt() },
                                valueRange = 5f..180f,
                                steps = 34
                            )

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 30, 45, 60).forEach { preset ->
                                    FilterChip(
                                        selected = limitMinutes == preset,
                                        onClick = { limitMinutes = preset },
                                        label = { Text("${preset}m") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { step = 1 }) {
                                    Text("Back")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (app != null) {
                                            onAddApp(app, limitMinutes)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Lock App Rule", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
