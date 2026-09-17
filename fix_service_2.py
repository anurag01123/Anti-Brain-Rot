with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "r") as f:
    content = f.read()

# find package and imports
header = content.split("class BlockerAccessibilityService")[0]

new_class = """class BlockerAccessibilityService : AccessibilityService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: AppRepository
    
    private var lastBlockedPackage = ""
    private var lastBlockTime = 0L
    private var currentForegroundPackage = ""
    private var foregroundStartTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val db = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(db.appDao())
        
        scope.launch {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            while (isActive) {
                if (powerManager.isInteractive && currentForegroundPackage.isNotEmpty()) {
                    checkAndBlockApp(currentForegroundPackage)
                } else if (!powerManager.isInteractive) {
                    // Screen is off, reset continuous usage tracking
                    currentForegroundPackage = ""
                    foregroundStartTime = 0L
                }
                kotlinx.coroutines.delay(2000) // Poll every 2 seconds for faster response
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        
        val packageName = event.packageName?.toString() ?: return
        if (packageName != currentForegroundPackage) {
            currentForegroundPackage = packageName
            foregroundStartTime = System.currentTimeMillis() // Reset continuous time on app switch
        }
        
        scope.launch { checkAndBlockApp(packageName) }
    }
    
    private suspend fun checkAndBlockApp(packageName: String) {
        if (packageName == applicationContext.packageName) return
        if (packageName == "com.android.systemui") return
        
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTime) < 1000) return

        val trackedApp = repository.getTrackedApp(packageName) ?: return
        if (!trackedApp.isActive) return

        val prefs = applicationContext.getSharedPreferences("block_settings", Context.MODE_PRIVATE)
        val isGlobalBlock = prefs.getBoolean("block_all", false)
        val isOvernightBlock = prefs.getBoolean("overnight_block", false)
        val isAntiDoom = prefs.getBoolean("anti_doom", false)
        
        var shouldBlock = false
        var blockReason = ""
        var isUnconditional = false
        
        if (isGlobalBlock) {
            shouldBlock = true
            isUnconditional = true
            blockReason = "Global block is active."
        }
        
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
            val sStr = if(startHour > 12) "${startHour-12} PM" else if(startHour==12) "12 PM" else if(startHour==0) "12 AM" else "${startHour} AM"
            val eStr = if(endHour > 12) "${endHour-12} PM" else if(endHour==12) "12 PM" else if(endHour==0) "12 AM" else "${endHour} AM"
            blockReason = "Overnight block is active ($sStr - $eStr)."
        }
        
        if (!shouldBlock && isAntiDoom) {
            // Track continuous usage manually via accessibility service events
            val continuousUsage = if (foregroundStartTime > 0 && packageName == currentForegroundPackage) {
                now - foregroundStartTime
            } else 0L
            
            if (continuousUsage > 5 * 60 * 1000L) { // 5 minutes continuous
                shouldBlock = true
                isUnconditional = true
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
        
        if (!shouldBlock && currentUsage > (limitMillis + bonusMillis)) {
            shouldBlock = true
            isUnconditional = false
            blockReason = "Daily limit reached."
        }
        
        if (shouldBlock) {
            lastBlockedPackage = packageName
            lastBlockTime = now
            
            repository.incrementBlockOccurrence()
            
            val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra("BLOCKED_APP", trackedApp.appName)
                putExtra("PACKAGE_NAME", packageName)
                putExtra("LIMIT_MINUTES", trackedApp.dailyLimitMinutes)
                putExtra("IS_UNCONDITIONAL", isUnconditional)
                if (blockReason.isNotEmpty()) {
                    putExtra("BLOCK_REASON", blockReason)
                }
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
"""

with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "w") as f:
    f.write(header + new_class)
