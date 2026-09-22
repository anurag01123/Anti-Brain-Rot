package com.example

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.UsageDataVisualizationScreen
import com.example.ui.components.ModernBackground
import com.example.ui.components.ModernBottomNavigation
import com.example.ui.components.ModernTopBar
import com.example.ui.components.NavigationTabItem
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.TrackedAppsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.utils.FluidSprings
import com.example.utils.UsageUtils

object AppIconCache {
    val cache = object : android.util.LruCache<String, ImageBitmap>(80) {}
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

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        SystemAlertWindowService.hideBlockOverlay(this)
        BlockerAccessibilityService.notifyAppExited(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val view = LocalView.current

            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? android.app.Activity)?.window
                    if (window != null) {
                        WindowCompat.getInsetsController(window, view).apply {
                            isAppearanceLightStatusBars = !isDarkMode
                            isAppearanceLightNavigationBars = !isDarkMode
                        }
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                ModernBackground(isDark = isDarkMode) {
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

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onCheckUsagePermission: () -> Unit,
    onCheckAccessibility: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var hasUsageAccess by remember { mutableStateOf(UsageUtils.hasUsageStatsPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(UsageUtils.isAccessibilityServiceEnabled(context)) }
    var hasOverlay by remember { mutableStateOf(UsageUtils.hasOverlayPermission(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = UsageUtils.hasUsageStatsPermission(context)
                hasAccessibility = UsageUtils.isAccessibilityServiceEnabled(context)
                hasOverlay = UsageUtils.hasOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allEnginesActive = hasUsageAccess && hasAccessibility && hasOverlay

    val navigationTabs = remember {
        listOf(
            NavigationTabItem(title = "Apps", icon = Icons.Rounded.Lock),
            NavigationTabItem(title = "Contacts", icon = Icons.Rounded.Call),
            NavigationTabItem(title = "Charts", icon = Icons.Rounded.BarChart),
            NavigationTabItem(title = "Insights", icon = Icons.Rounded.DateRange)
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            ModernTopBar(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.setDarkMode(!isDarkMode) },
                allEnginesActive = allEnginesActive
            )
        },
        bottomBar = {
            ModernBottomNavigation(
                tabs = navigationTabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // 120Hz Fluid Sliding & Fading Screen Transitions
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = FluidSprings.SmoothSlideOffset,
                            initialOffsetX = { fullWidth -> fullWidth / 4 }
                        ) + fadeIn(animationSpec = tween(240))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = FluidSprings.SmoothSlideOffset,
                                targetOffsetX = { fullWidth -> -fullWidth / 4 }
                            ) + fadeOut(animationSpec = tween(180))
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = FluidSprings.SmoothSlideOffset,
                            initialOffsetX = { fullWidth -> -fullWidth / 4 }
                        ) + fadeIn(animationSpec = tween(240))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = FluidSprings.SmoothSlideOffset,
                                targetOffsetX = { fullWidth -> fullWidth / 4 }
                            ) + fadeOut(animationSpec = tween(180))
                        )
                    }
                },
                label = "tab_fluid_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> TrackedAppsScreen(
                        viewModel = viewModel,
                        onNavigateToCharts = { selectedTab = 2 }
                    )
                    1 -> ContactsScreen(
                        viewModel = viewModel
                    )
                    2 -> UsageDataVisualizationScreen(
                        viewModel = viewModel
                    )
                    3 -> AnalyticsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
