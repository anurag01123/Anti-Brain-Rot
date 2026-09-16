package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.utils.UsageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

import kotlinx.coroutines.isActive

class BlockerAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: AppRepository
    
    private var lastBlockedPackage = ""
    private var lastBlockTime = 0L
    private var currentForegroundPackage = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        val db = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(db.appDao())
        
        scope.launch {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            while (isActive) {
                if (powerManager.isInteractive && currentForegroundPackage.isNotEmpty()) {
                    checkAndBlockApp(currentForegroundPackage)
                }
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds for the active app, but only process if screen is on
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        
        val packageName = event.packageName?.toString() ?: return
        currentForegroundPackage = packageName
        
        scope.launch { checkAndBlockApp(packageName) }
    }

    private suspend fun checkAndBlockApp(packageName: String) {
        // Prevent infinite block loops
        if (packageName == applicationContext.packageName) return
        if (packageName == "com.android.systemui") return
        
        // Rate limit block checks to prevent excessive intents
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTime) < 300) return

        val trackedApp = repository.getTrackedApp(packageName) ?: return
        if (!trackedApp.isActive) return
        
        val prefs = applicationContext.getSharedPreferences("block_settings", Context.MODE_PRIVATE)
        val isGlobalBlock = prefs.getBoolean("block_all", false)
        val isOvernightBlock = prefs.getBoolean("overnight_block", false)
        val isAntiDoom = prefs.getBoolean("anti_doom", false)
        
        var shouldBlock = false
        var blockReason = ""
        
        if (isGlobalBlock) {
            shouldBlock = true
            blockReason = "Global block is active."
        }
        
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val startHour = prefs.getInt("overnight_start_hour", 22)
        val endHour = prefs.getInt("overnight_end_hour", 7)
        
        val isOvernight = if (startHour > endHour) {
            // e.g. 22 to 7 (crosses midnight)
            currentHour >= startHour || currentHour < endHour
        } else {
            // e.g. 1 to 5 (same day)
            currentHour in startHour until endHour
        }
        
        if (isOvernightBlock && isOvernight) {
            shouldBlock = true
            val sStr = if(startHour > 12) "${startHour-12} PM" else if(startHour==12) "12 PM" else if(startHour==0) "12 AM" else "${startHour} AM"
            val eStr = if(endHour > 12) "${endHour-12} PM" else if(endHour==12) "12 PM" else if(endHour==0) "12 AM" else "${endHour} AM"
            blockReason = "Overnight block is active ($sStr - $eStr)."
        }
        
        if (isAntiDoom) {
            val continuousUsage = UsageUtils.getContinuousUsageTimeForApp(applicationContext, packageName)
            if (continuousUsage > 5 * 60 * 1000L) { // 5 minutes continuous
                shouldBlock = true
                blockReason = "Anti-Doom Scrolling active (5 min limit)."
            }
        }
        
        
        val calBonus = Calendar.getInstance()
        calBonus.set(Calendar.HOUR_OF_DAY, 0)
        calBonus.set(Calendar.MINUTE, 0)
        calBonus.set(Calendar.SECOND, 0)
        calBonus.set(Calendar.MILLISECOND, 0)
        
        val bonusKey = "bonus_time_${packageName}_${calBonus.timeInMillis}"
        val bonusMillis = prefs.getLong(bonusKey, 0L)
        
        val limitMillis = trackedApp.dailyLimitMinutes * 60 * 1000L
        val currentUsage = UsageUtils.getUsageTimeForApp(applicationContext, packageName)
        
        if (currentUsage > (limitMillis + bonusMillis)) {
            shouldBlock = true
        }

        
        if (shouldBlock) {
            val contacts = repository.getAllContactsSync()
            val minContactsRequired = 3
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            val calledContactsCount = contacts.count { it.lastCalledTimestamp >= startOfDay }
            
            if (contacts.size < minContactsRequired || calledContactsCount < contacts.size) {
                lastBlockedPackage = packageName
                lastBlockTime = now
                
                repository.incrementBlockOccurrence()
                
                val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("BLOCKED_APP", trackedApp.appName)
                    putExtra("PACKAGE_NAME", trackedApp.packageName)
                    putExtra("LIMIT_MINUTES", trackedApp.dailyLimitMinutes)
                    if (blockReason.isNotEmpty()) {
                        putExtra("BLOCK_REASON", blockReason)
                    }
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
