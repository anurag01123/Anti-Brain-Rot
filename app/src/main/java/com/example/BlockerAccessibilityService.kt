package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.utils.UsageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class BlockerAccessibilityService : AccessibilityService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: AppRepository
    
    private var lastBlockedPackage = ""
    private var lastBlockTime = 0L
    private var currentForegroundPackage = ""
    private var foregroundStartTime = 0L

    private val launcherPackages = mutableSetOf<String>()

    companion object {
        private const val TAG = "BlockerService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "BlockerAccessibilityService connected")
        val db = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(db.appDao())
        loadLauncherPackages()

        scope.launch {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            while (isActive) {
                if (powerManager.isInteractive && currentForegroundPackage.isNotEmpty()) {
                    checkAndBlockApp(currentForegroundPackage)
                } else if (!powerManager.isInteractive) {
                    currentForegroundPackage = ""
                    foregroundStartTime = 0L
                }
                delay(1500)
            }
        }
    }

    private fun loadLauncherPackages() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            launcherPackages.clear()
            for (info in resolveInfos) {
                info.activityInfo?.packageName?.let { launcherPackages.add(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading launcher packages", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return
        
        val packageName = event.packageName?.toString() ?: return
        
        // Skip system packages, system UI, keyboards, launcher, and dialers
        if (isIgnoredPackage(packageName)) {
            return
        }

        if (packageName != currentForegroundPackage) {
            currentForegroundPackage = packageName
            foregroundStartTime = System.currentTimeMillis()
        }

        scope.launch { checkAndBlockApp(packageName) }
    }

    private fun isIgnoredPackage(packageName: String): Boolean {
        if (packageName == applicationContext.packageName) return true
        if (packageName == "com.android.systemui") return true
        if (packageName == "android") return true
        if (packageName.contains("inputmethod")) return true
        if (launcherPackages.contains(packageName) || packageName.contains("launcher")) return true
        if (isDialer(packageName)) return true
        return false
    }

    private fun isDialer(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower.contains("dialer") || 
               lower.contains("phone") || 
               lower.contains("telecom") ||
               lower.contains("incall")
    }

    private suspend fun checkAndBlockApp(packageName: String) {
        if (isIgnoredPackage(packageName)) return

        // Per user requirements: overnight block, block all, anti-doom scroll, and daily limit
        // should ONLY apply to the apps added to the list and marked active
        val trackedApp = repository.getTrackedApp(packageName) ?: return
        if (!trackedApp.isActive) return

        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTime) < 1500) return

        val prefs = applicationContext.getSharedPreferences("block_settings", Context.MODE_PRIVATE)
        val isGlobalBlock = prefs.getBoolean("block_all", false)
        val isOvernightBlock = prefs.getBoolean("overnight_block", false)
        val isAntiDoom = prefs.getBoolean("anti_doom", false)
        
        var shouldBlock = false
        var blockReason = ""
        var isUnconditional = false

        // 1. BLOCK ALL LISTED APPS
        if (isGlobalBlock) {
            shouldBlock = true
            isUnconditional = true
            blockReason = "Block All Listed Apps is active."
        }
        
        // 2. OVERNIGHT BLOCK
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val startHour = prefs.getInt("overnight_start_hour", 22)
        val endHour = prefs.getInt("overnight_end_hour", 7)
        
        val isOvernight = if (startHour > endHour) {
            currentHour >= startHour || currentHour < endHour
        } else {
            currentHour in startHour until endHour
        }
        
        if (!shouldBlock && isOvernightBlock && isOvernight) {
            shouldBlock = true
            isUnconditional = true
            val sStr = if (startHour > 12) "${startHour - 12} PM" else if (startHour == 12) "12 PM" else if (startHour == 0) "12 AM" else "$startHour AM"
            val eStr = if (endHour > 12) "${endHour - 12} PM" else if (endHour == 12) "12 PM" else if (endHour == 0) "12 AM" else "$endHour AM"
            blockReason = "Overnight block is active ($sStr - $eStr)."
        }
        
        // 3. ANTI-DOOM SCROLLING (5 minutes continuous usage)
        if (!shouldBlock && isAntiDoom) {
            val liveContinuous = if (foregroundStartTime > 0 && packageName == currentForegroundPackage) {
                now - foregroundStartTime
            } else 0L
            val statsContinuous = UsageUtils.getContinuousUsageTimeForApp(applicationContext, packageName)
            val continuousUsage = maxOf(liveContinuous, statsContinuous)
            
            if (continuousUsage >= 5 * 60 * 1000L) { // 5 minutes
                shouldBlock = true
                isUnconditional = true
                blockReason = "Anti-Doom Scrolling limit reached (5 min continuous)."
            }
        }
        
        // 4. DAILY LIMIT (Standard App Lock)
        val calBonus = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val bonusKey = "bonus_time_${packageName}_${calBonus.timeInMillis}"
        val bonusMillis = prefs.getLong(bonusKey, 0L)
        val limitMillis = trackedApp.dailyLimitMinutes * 60 * 1000L
        val currentUsage = UsageUtils.getUsageTimeForApp(applicationContext, packageName)
        
        if (!shouldBlock && currentUsage >= (limitMillis + bonusMillis)) {
            shouldBlock = true
            isUnconditional = false
            blockReason = "Daily limit of ${trackedApp.dailyLimitMinutes}m reached."
        }
        
        if (shouldBlock) {
            lastBlockedPackage = packageName
            lastBlockTime = now
            
            repository.incrementBlockOccurrence()
            
            try {
                // Use System Alert Window service to reliably display the overlay activity
                SystemAlertWindowService.showBlockOverlay(
                    context = applicationContext,
                    appName = trackedApp.appName,
                    packageName = packageName,
                    limitMinutes = trackedApp.dailyLimitMinutes,
                    isUnconditional = isUnconditional,
                    blockReason = blockReason.ifEmpty { null }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to display block overlay, falling back to home screen", e)
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "BlockerAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
