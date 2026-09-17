package com.example.utils

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar

object UsageUtils {
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()
        if (startTime >= endTime) return 0L

        val stats = usm.queryAndAggregateUsageStats(startTime, endTime)
        return stats[packageName]?.totalTimeInForeground ?: 0L
    }

    fun getContinuousUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        // Look back last 1 hour
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (60 * 60 * 1000L)
        
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        
        var lastResumed = 0L
        var isForeground = false
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == 1) {
                    if (lastResumed == 0L) {
                        lastResumed = event.timeStamp
                    }
                    isForeground = true
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || 
                           event.eventType == 2 || 
                           event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                    lastResumed = 0L
                    isForeground = false
                }
            }
        }
        
        if (lastResumed > 0 && isForeground) {
            return endTime - lastResumed
        }
        
        return 0L
    }
}
