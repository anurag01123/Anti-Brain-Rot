package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * System Alert Window Service that reliably displays an overlay activity (BlockActivity)
 * and an immediate system overlay shield over restricted apps when their usage limit is exceeded.
 */
class SystemAlertWindowService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var windowManager: WindowManager? = null
    private var overlayRootView: View? = null
    private var currentBlockedPackage: String? = null

    companion object {
        private const val TAG = "SystemAlertWindow"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "anti_brain_rot_overlay_channel"

        const val ACTION_SHOW_OVERLAY = "com.example.action.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.example.action.HIDE_OVERLAY"
        const val ACTION_ACTIVITY_DISPLAYED = "com.example.action.ACTIVITY_DISPLAYED"

        const val EXTRA_BLOCKED_APP = "BLOCKED_APP"
        const val EXTRA_PACKAGE_NAME = "PACKAGE_NAME"
        const val EXTRA_LIMIT_MINUTES = "LIMIT_MINUTES"
        const val EXTRA_IS_UNCONDITIONAL = "IS_UNCONDITIONAL"
        const val EXTRA_BLOCK_REASON = "BLOCK_REASON"

        /**
         * Reliably displays the overlay activity and System Alert Window over restricted apps.
         */
        fun showBlockOverlay(
            context: Context,
            appName: String,
            packageName: String,
            limitMinutes: Int,
            isUnconditional: Boolean = false,
            blockReason: String? = null
        ) {
            val intent = Intent(context, SystemAlertWindowService::class.java).apply {
                action = ACTION_SHOW_OVERLAY
                putExtra(EXTRA_BLOCKED_APP, appName)
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_LIMIT_MINUTES, limitMinutes)
                putExtra(EXTRA_IS_UNCONDITIONAL, isUnconditional)
                if (blockReason != null) {
                    putExtra(EXTRA_BLOCK_REASON, blockReason)
                }
            }

            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "startService encountered issue, falling back to direct activity launch", e)
            }

            // Always guarantee overlay activity launch directly as well
            launchOverlayActivity(
                context = context,
                appName = appName,
                packageName = packageName,
                limitMinutes = limitMinutes,
                isUnconditional = isUnconditional,
                blockReason = blockReason
            )
        }

        /**
         * Hides any active overlay window when the app is unlocked or dismissed.
         */
        fun hideBlockOverlay(context: Context) {
            val intent = Intent(context, SystemAlertWindowService::class.java).apply {
                action = ACTION_HIDE_OVERLAY
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "hideBlockOverlay failed to send intent", e)
            }
        }

        /**
         * Called when BlockActivity has safely taken the foreground.
         */
        fun onActivityDisplayed(context: Context) {
            val intent = Intent(context, SystemAlertWindowService::class.java).apply {
                action = ACTION_ACTIVITY_DISPLAYED
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "onActivityDisplayed failed", e)
            }
        }

        /**
         * Directly starts BlockActivity with all necessary flags to appear over other apps.
         */
        fun launchOverlayActivity(
            context: Context,
            appName: String,
            packageName: String,
            limitMinutes: Int,
            isUnconditional: Boolean,
            blockReason: String?
        ) {
            val activityIntent = Intent(context, BlockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(EXTRA_BLOCKED_APP, appName)
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_LIMIT_MINUTES, limitMinutes)
                putExtra(EXTRA_IS_UNCONDITIONAL, isUnconditional)
                if (blockReason != null) {
                    putExtra(EXTRA_BLOCK_REASON, blockReason)
                }
            }

            try {
                context.startActivity(activityIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch BlockActivity via startActivity, trying pending intent", e)
                try {
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        101,
                        activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    pendingIntent.send()
                } catch (pe: Exception) {
                    Log.e(TAG, "Failed to launch BlockActivity via PendingIntent", pe)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        createNotificationChannelIfNeeded()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_SHOW_OVERLAY -> {
                val appName = intent.getStringExtra(EXTRA_BLOCKED_APP) ?: "Restricted App"
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
                val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 0)
                val isUnconditional = intent.getBooleanExtra(EXTRA_IS_UNCONDITIONAL, false)
                val blockReason = intent.getStringExtra(EXTRA_BLOCK_REASON)

                currentBlockedPackage = packageName

                // 1. Immediately launch the overlay activity over the restricted app
                launchOverlayActivity(
                    context = this,
                    appName = appName,
                    packageName = packageName,
                    limitMinutes = limitMinutes,
                    isUnconditional = isUnconditional,
                    blockReason = blockReason
                )

                // 2. If System Alert Window (Overlay) permission is granted, display the instant overlay view
                if (Settings.canDrawOverlays(this)) {
                    mainHandler.post {
                        showSystemAlertWindow(appName, packageName, limitMinutes, isUnconditional, blockReason)
                    }
                }
            }

            ACTION_HIDE_OVERLAY -> {
                currentBlockedPackage = null
                mainHandler.post {
                    removeOverlayView()
                }
            }

            ACTION_ACTIVITY_DISPLAYED -> {
                // BlockActivity is now safely on screen; remove the window overlay view so it doesn't overlap
                mainHandler.post {
                    removeOverlayView()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun showSystemAlertWindow(
        appName: String,
        packageName: String,
        limitMinutes: Int,
        isUnconditional: Boolean,
        blockReason: String?
    ) {
        if (!Settings.canDrawOverlays(this)) return

        val wm = windowManager ?: return

        // If overlay already displayed for this package, do not re-add
        if (overlayRootView != null) {
            return
        }

        try {
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val view = createOverlayView(appName, packageName, limitMinutes, isUnconditional, blockReason)
            overlayRootView = view
            wm.addView(view, params)
            Log.d(TAG, "System Alert Window view successfully added for $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach System Alert Window view", e)
        }
    }

    private fun removeOverlayView() {
        val wm = windowManager ?: return
        val view = overlayRootView ?: return
        try {
            wm.removeView(view)
            Log.d(TAG, "System Alert Window view removed")
        } catch (e: Exception) {
            Log.w(TAG, "Error removing overlay view", e)
        } finally {
            overlayRootView = null
        }
    }

    private fun createOverlayView(
        appName: String,
        packageName: String,
        limitMinutes: Int,
        isUnconditional: Boolean,
        blockReason: String?
    ): View {
        val density = resources.displayMetrics.density
        val dpToPx = { dp: Int -> (dp * density).toInt() }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#EE0B0B14")) // 93% opaque dark purple-black scrim
            setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(24), dpToPx(28), dpToPx(24), dpToPx(28))

            val cardBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f * density
                setColor(Color.parseColor("#1C1929"))
                setStroke(dpToPx(2), Color.parseColor("#E53935")) // Red alert border
            }
            background = cardBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Material Block Icon
        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.ic_block)
            val iconSize = dpToPx(56)
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dpToPx(12)
            }
        }
        card.addView(iconView)

        // Subtitle Header
        val headerView = TextView(this).apply {
            text = "USAGE LIMIT EXCEEDED"
            setTextColor(Color.parseColor("#FF5252"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setPadding(0, 0, 0, dpToPx(6))
        }
        card.addView(headerView)

        // App Name
        val appNameView = TextView(this).apply {
            text = appName
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(8))
        }
        card.addView(appNameView)

        // Reason description
        val reasonText = blockReason ?: if (isUnconditional) {
            "This application is currently locked."
        } else {
            "Daily limit of ${limitMinutes}m reached."
        }
        val reasonView = TextView(this).apply {
            text = reasonText
            setTextColor(Color.parseColor("#B0BEC5"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(22))
        }
        card.addView(reasonView)

        // Primary action button: open Penalty Call unlock
        val unlockButton = Button(this).apply {
            text = if (isUnconditional) "View Lock Details" else "Complete Penalty Call to Unlock"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f * density
                setColor(Color.parseColor("#6750A4"))
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(50)
            ).apply {
                setMargins(0, 0, 0, dpToPx(12))
            }
            setOnClickListener {
                launchOverlayActivity(
                    context = this@SystemAlertWindowService,
                    appName = appName,
                    packageName = packageName,
                    limitMinutes = limitMinutes,
                    isUnconditional = isUnconditional,
                    blockReason = blockReason
                )
                removeOverlayView()
            }
        }
        card.addView(unlockButton)

        // Secondary button: Exit to Home Screen
        val homeButton = Button(this).apply {
            text = "Return to Home Screen"
            setTextColor(Color.parseColor("#CFD8DC"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            val homeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f * density
                setColor(Color.parseColor("#292639"))
            }
            background = homeBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(46)
            )
            setOnClickListener {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                this@SystemAlertWindowService.startActivity(homeIntent)
                removeOverlayView()
            }
        }
        card.addView(homeButton)

        root.addView(card)
        return root
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Lock Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows protection status when apps reach their usage limits"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
        serviceJob.cancel()
    }
}
