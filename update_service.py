import re

with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "r") as f:
    content = f.read()

import_context = "import android.content.Context\n"
if "import android.content.Context" not in content:
    content = content.replace("import android.content.Intent", import_context + "import android.content.Intent")

old_launch_block = """        scope.launch {
            val trackedApp = repository.getTrackedApp(packageName) ?: return@launch
            
            if (!trackedApp.isActive) return@launch
            
            val limitMillis = trackedApp.dailyLimitMinutes * 60 * 1000L
            val currentUsage = UsageUtils.getUsageTimeForApp(applicationContext, packageName)
            
            if (currentUsage > limitMillis) {
                // Check if unblock condition is met
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
                    // Block the app!
                    lastBlockedPackage = packageName
                    lastBlockTime = now
                    
                    repository.incrementBlockOccurrence()
                    
                    val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("BLOCKED_APP", trackedApp.appName)
                    }
                    startActivity(intent)
                }
            }
        }"""

new_launch_block = """        scope.launch {
            val trackedApp = repository.getTrackedApp(packageName) ?: return@launch
            if (!trackedApp.isActive) return@launch
            
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
            val isOvernight = currentHour >= 22 || currentHour < 7
            if (isOvernightBlock && isOvernight) {
                shouldBlock = true
                blockReason = "Overnight block is active."
            }
            
            if (isAntiDoom) {
                val continuousUsage = UsageUtils.getContinuousUsageTimeForApp(applicationContext, packageName)
                if (continuousUsage > 5 * 60 * 1000L) { // 5 minutes continuous
                    shouldBlock = true
                    blockReason = "Anti-Doom Scrolling active (5 min limit)."
                }
            }
            
            val limitMillis = trackedApp.dailyLimitMinutes * 60 * 1000L
            val currentUsage = UsageUtils.getUsageTimeForApp(applicationContext, packageName)
            
            if (currentUsage > limitMillis) {
                shouldBlock = true
            }
            
            if (shouldBlock) {
                // Check if unblock condition is met only for daily limit, 
                // but global blocks might override this. Let's say contacts unblock only daily limit.
                // For simplicity, penalty calls override EVERYTHING so the user can escape.
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
                    // Block the app!
                    lastBlockedPackage = packageName
                    lastBlockTime = now
                    
                    repository.incrementBlockOccurrence()
                    
                    val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("BLOCKED_APP", trackedApp.appName)
                        if (blockReason.isNotEmpty()) {
                            putExtra("BLOCK_REASON", blockReason)
                        }
                    }
                    startActivity(intent)
                }
            }
        }"""

if old_launch_block in content:
    content = content.replace(old_launch_block, new_launch_block)
    with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Failed to replace launch block")
