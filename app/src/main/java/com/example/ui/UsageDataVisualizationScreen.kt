package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.TrackedApp
import com.example.ui.components.GlassSurface
import com.example.ui.utils.Haptics
import com.example.ui.utils.MotionTokens
import dev.chrisbanes.haze.HazeState
import org.json.JSONArray
import org.json.JSONObject

enum class UsageFilter {
    ALL, APPROACHING, EXCEEDED, SAFE
}

data class AppUsageItem(
    val app: TrackedApp,
    val usedMillis: Long,
    val effectiveLimitMinutes: Int
) {
    val usedMinutes: Int = (usedMillis / (1000 * 60)).toInt()
    val percentage: Float = if (effectiveLimitMinutes > 0) {
        (usedMinutes.toFloat() / effectiveLimitMinutes.toFloat()) * 100f
    } else 0f

    val isExceeded: Boolean = usedMinutes >= effectiveLimitMinutes && effectiveLimitMinutes > 0
    val isApproaching: Boolean = percentage >= 75f && !isExceeded
    val isSafe: Boolean = percentage < 75f

    val remainingMinutes: Int = (effectiveLimitMinutes - usedMinutes).coerceAtLeast(0)
    val overtimeMinutes: Int = (usedMinutes - effectiveLimitMinutes).coerceAtLeast(0)
}

@Composable
fun UsageDataVisualizationScreen(
    viewModel: MainViewModel,
    hazeState: HazeState? = null
) {
    val context = LocalContext.current
    val trackedApps by viewModel.allTrackedApps.collectAsStateWithLifecycle(initialValue = emptyList())
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val appUsages = viewModel.appUsages

    var selectedFilter by remember { mutableStateOf(UsageFilter.ALL) }
    var selectedAppForEdit by remember { mutableStateOf<TrackedApp?>(null) }
    var newLimitInput by remember { mutableStateOf("") }

    // Prepare processed usage items
    val items = remember(trackedApps, appUsages.toMap()) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val prefs = context.getSharedPreferences("block_settings", Context.MODE_PRIVATE)

        trackedApps.map { app ->
            val bonusKey = "bonus_time_${app.packageName}_${startOfDay}"
            val bonusMillis = prefs.getLong(bonusKey, 0L)
            val bonusMins = (bonusMillis / (1000 * 60)).toInt()
            val effectiveLimit = app.dailyLimitMinutes + bonusMins
            val usedMillis = appUsages[app.packageName] ?: 0L
            AppUsageItem(app, usedMillis, effectiveLimit)
        }.sortedByDescending { it.percentage }
    }

    val approachingItems = remember(items) { items.filter { it.isApproaching } }
    val exceededItems = remember(items) { items.filter { it.isExceeded } }
    val safeItems = remember(items) { items.filter { it.isSafe } }

    val filteredItems = when (selectedFilter) {
        UsageFilter.ALL -> items
        UsageFilter.APPROACHING -> approachingItems
        UsageFilter.EXCEEDED -> exceededItems
        UsageFilter.SAFE -> safeItems
    }

    val totalUsedMinutes = items.sumOf { it.usedMinutes }
    val totalHours = totalUsedMinutes / 60
    val remainingMins = totalUsedMinutes % 60

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Title
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Daily App Usage",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Real-time usage visualization & limit proximity radar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 2. Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Screen Time",
                    value = if (totalHours > 0) "${totalHours}h ${remainingMins}m" else "${remainingMins}m",
                    subtitle = "${items.size} apps tracked",
                    icon = Icons.Rounded.Timer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Approaching Limit",
                    value = "${approachingItems.size}",
                    subtitle = "≥75% limit reached",
                    icon = Icons.Rounded.WarningAmber,
                    containerColor = if (approachingItems.isNotEmpty()) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = if (approachingItems.isNotEmpty()) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                    badgeColor = if (approachingItems.isNotEmpty()) Color(0xFFF59E0B) else null,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Limit Exceeded",
                    value = "${exceededItems.size}",
                    subtitle = "Blocked today",
                    icon = Icons.Rounded.Block,
                    containerColor = if (exceededItems.isNotEmpty()) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = if (exceededItems.isNotEmpty()) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    badgeColor = if (exceededItems.isNotEmpty()) Color(0xFFEF4444) else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Prominent "Approaching Limits" Banner Alert
        if (approachingItems.isNotEmpty()) {
            item {
                ApproachingLimitsAlertBanner(
                    approachingItems = approachingItems,
                    onAdjustLimit = { app ->
                        selectedAppForEdit = app
                        newLimitInput = app.dailyLimitMinutes.toString()
                    }
                )
            }
        }

        // 4. Recharts Interactive Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.BarChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Usage vs Limits Chart",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Tap any bar to inspect app threshold",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Live indicator badge
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Live",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Embedded Recharts WebView Component
                    RechartsWebViewComponent(
                        items = items,
                        isDarkMode = isDarkMode,
                        onAppSelected = { pkg ->
                            val match = trackedApps.find { it.packageName == pkg }
                            if (match != null) {
                                selectedAppForEdit = match
                                newLimitInput = match.dailyLimitMinutes.toString()
                            }
                        }
                    )
                }
            }
        }

        // 5. Filter Chips
        item {
            Column {
                Text(
                    "Tracked Apps Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == UsageFilter.ALL,
                            onClick = { selectedFilter = UsageFilter.ALL },
                            leadingIcon = {
                                Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("All (${items.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == UsageFilter.APPROACHING,
                            onClick = { selectedFilter = UsageFilter.APPROACHING },
                            leadingIcon = {
                                Icon(Icons.Rounded.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFB45309))
                            },
                            label = { Text("Approaching (${approachingItems.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFF92400E)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == UsageFilter.EXCEEDED,
                            onClick = { selectedFilter = UsageFilter.EXCEEDED },
                            leadingIcon = {
                                Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFB91C1C))
                            },
                            label = { Text("Exceeded (${exceededItems.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEE2E2),
                                selectedLabelColor = Color(0xFF991B1B)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == UsageFilter.SAFE,
                            onClick = { selectedFilter = UsageFilter.SAFE },
                            leadingIcon = {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF15803D))
                            },
                            label = { Text("Healthy (${safeItems.size})") }
                        )
                    }
                }
            }
        }

        // 6. List of App Usage Cards
        if (filteredItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No apps found in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.app.packageName }) { usageItem ->
                AppUsageDetailCard(
                    item = usageItem,
                    onEditLimit = {
                        selectedAppForEdit = usageItem.app
                        newLimitInput = usageItem.app.dailyLimitMinutes.toString()
                    }
                )
            }
        }
    }

    // Edit Limit Dialog
    if (selectedAppForEdit != null) {
        val app = selectedAppForEdit!!
        AlertDialog(
            onDismissRequest = { selectedAppForEdit = null },
            icon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Daily Limit: ${app.appName}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Set the maximum allowed usage time per day. Once reached, AntiBrainRot will block access to the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newLimitInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) newLimitInput = it },
                        label = { Text("Limit (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            AssistChip(
                                onClick = { newLimitInput = mins.toString() },
                                label = { Text("${mins}m") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = newLimitInput.toIntOrNull()
                        if (mins != null && mins > 0) {
                            viewModel.addTrackedApp(app.packageName, app.appName, mins)
                        }
                        selectedAppForEdit = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebViewComponent(
    items: List<AppUsageItem>,
    isDarkMode: Boolean,
    onAppSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Convert items into JSON array for Recharts
    val jsonData = remember(items) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("packageName", item.app.packageName)
                put("appName", item.app.appName)
                put("usedMinutes", item.usedMinutes)
                put("limitMinutes", item.effectiveLimitMinutes)
                put("percentage", Math.round(item.percentage))
            }
            array.put(obj)
        }
        array.toString()
    }

    // Push update to Recharts whenever data or theme changes
    LaunchedEffect(jsonData, isDarkMode) {
        webViewRef?.evaluateJavascript(
            "if (window.updateUsageData) { window.updateUsageData(${JSONObject.quote(jsonData)}, $isDarkMode); }",
            null
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .border(
                1.dp,
                if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            )
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    setBackgroundColor(0)

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onAppSelected(pkg: String) {
                            onAppSelected(pkg)
                        }

                        @JavascriptInterface
                        fun getInitialData(): String {
                            return jsonData
                        }

                        @JavascriptInterface
                        fun isDark(): Boolean {
                            return isDarkMode
                        }
                    }, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                "if (window.updateUsageData) { window.updateUsageData(${JSONObject.quote(jsonData)}, $isDarkMode); }",
                                null
                            )
                        }
                    }

                    loadUrl("file:///android_asset/recharts_chart.html")
                    webViewRef = this
                }
            },
            update = { wv ->
                wv.evaluateJavascript(
                    "if (window.updateUsageData) { window.updateUsageData(${JSONObject.quote(jsonData)}, $isDarkMode); }",
                    null
                )
            }
        )
    }
}

@Composable
fun ApproachingLimitsAlertBanner(
    approachingItems: List<AppUsageItem>,
    onAdjustLimit: (TrackedApp) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBEB)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A)),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF59E0B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "${approachingItems.size} ${if (approachingItems.size == 1) "App Approaching Daily Limit" else "Apps Approaching Daily Limits"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        "Approaching or exceeded 75% of limit. Lock imminent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            approachingItems.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFFDE68A)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                item.app.appName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF78350F)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "${Math.round(item.percentage)}%",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                        Text(
                            "${item.usedMinutes}m used • ${item.remainingMinutes}m left before block",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB45309)
                        )
                    }

                    OutlinedButton(
                        onClick = { onAdjustLimit(item.app) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF92400E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Adjust", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AppUsageDetailCard(
    item: AppUsageItem,
    onEditLimit: () -> Unit
) {
    val progressFraction = (item.usedMinutes.toFloat() / item.effectiveLimitMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = MotionTokens.standard(),
        label = "progress"
    )

    val (statusColor, statusBg, statusText) = when {
        item.isExceeded -> Triple(Color(0xFFEF4444), Color(0xFFFEE2E2), "EXCEEDED")
        item.isApproaching -> Triple(Color(0xFFF59E0B), Color(0xFFFEF3C7), "APPROACHING")
        item.percentage >= 40f -> Triple(Color(0xFF3B82F6), Color(0xFFDBEAFE), "MODERATE")
        else -> Triple(Color(0xFF10B981), Color(0xFFDCFCE7), "HEALTHY")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.app.appName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            item.app.appName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (item.isExceeded) "Over limit by ${item.overtimeMinutes}m" else "${item.remainingMinutes}m remaining today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isExceeded) Color(0xFFDC2626) else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar with custom threshold styling
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.usedMinutes} mins used",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Limit: ${item.effectiveLimitMinutes} mins (${Math.round(item.percentage)}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditLimit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Limit", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    badgeColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                if (badgeColor != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(badgeColor, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
