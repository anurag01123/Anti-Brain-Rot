package com.example.utils

import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import com.example.BlockerAccessibilityService
import java.util.Calendar

object UsageUtils {

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val expectedComponentName = ComponentName(context, BlockerAccessibilityService::class.java)
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)
            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledService = ComponentName.unflattenFromString(componentNameString)
                if (enabledService != null && (enabledService == expectedComponentName || enabledService.packageName == context.packageName)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L
        
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val now = System.currentTimeMillis()
        if (startOfDay >= now) return 0L

        // Approach 1: Check system UsageStats
        var statsUsage = 0L
        try {
            val statsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
            if (!statsList.isNullOrEmpty()) {
                for (stat in statsList) {
                    if (stat.packageName == packageName) {
                        statsUsage = maxOf(statsUsage, stat.totalTimeInForeground)
                    }
                }
            }
            if (statsUsage == 0L) {
                val agg = usm.queryAndAggregateUsageStats(startOfDay, now)
                statsUsage = agg[packageName]?.totalTimeInForeground ?: 0L
            }
        } catch (e: Exception) {
            statsUsage = 0L
        }

        // Approach 2: Accurate UsageEvents timeline reconstruction
        var eventsUsage = 0L
        try {
            val events = usm.queryEvents(startOfDay, now)
            val event = UsageEvents.Event()
            var currentSessionStart = 0L
            var isAppForeground = false

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val eventPkg = event.packageName ?: continue
                val type = event.eventType
                val time = event.timeStamp

                if (eventPkg == packageName) {
                    if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == 1) {
                        if (!isAppForeground) {
                            currentSessionStart = time
                            isAppForeground = true
                        }
                    } else if (type == UsageEvents.Event.ACTIVITY_PAUSED ||
                               type == UsageEvents.Event.ACTIVITY_STOPPED ||
                               type == 2) {
                        // Check if another resumed event for this app is coming next
                        // If app was foreground, accumulate
                        if (isAppForeground && currentSessionStart > 0L) {
                            eventsUsage += (time - currentSessionStart).coerceAtLeast(0L)
                            isAppForeground = false
                            currentSessionStart = 0L
                        }
                    }
                } else {
                    // Different app resumed -> our target app is backgrounded
                    if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == 1) {
                        if (isAppForeground && currentSessionStart > 0L) {
                            eventsUsage += (time - currentSessionStart).coerceAtLeast(0L)
                            isAppForeground = false
                            currentSessionStart = 0L
                        }
                    }
                }
            }

            // If app is currently actively open right now
            if (isAppForeground && currentSessionStart > 0L) {
                eventsUsage += (now - currentSessionStart).coerceAtLeast(0L)
            }
        } catch (e: Exception) {
            eventsUsage = 0L
        }

        val total = maxOf(statsUsage, eventsUsage)
        return total.coerceIn(0L, now - startOfDay)
    }

    fun getContinuousUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L
        
        val now = System.currentTimeMillis()
        val startTime = now - (60 * 60 * 1000L) // Look back 1 hour
        
        try {
            val events = usageStatsManager.queryEvents(startTime, now)
            val event = UsageEvents.Event()
            
            var sessionStart = 0L
            var isForeground = false
            
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val type = event.eventType
                val timestamp = event.timeStamp

                if (pkg == packageName) {
                    if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == 1) {
                        if (!isForeground) {
                            sessionStart = timestamp
                            isForeground = true
                        }
                    } else if (type == UsageEvents.Event.ACTIVITY_PAUSED || 
                               type == UsageEvents.Event.ACTIVITY_STOPPED || 
                               type == 2) {
                        if (isForeground && sessionStart > 0L) {
                            isForeground = false
                            sessionStart = 0L
                        }
                    }
                } else if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == 1) {
                    // Another app took the foreground
                    if (isForeground) {
                        isForeground = false
                        sessionStart = 0L
                    }
                }
            }
            
            if (isForeground && sessionStart > 0L) {
                return (now - sessionStart).coerceAtLeast(0L)
            }
        } catch (e: Exception) {
            return 0L
        }
        
        return 0L
    }
}
