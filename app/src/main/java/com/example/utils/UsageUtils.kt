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

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        var totalTime = 0L
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        
        var lastForegroundTime = 0L
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName) {
                // ACTIVITY_RESUMED is 1, ACTIVITY_PAUSED is 2, ACTIVITY_STOPPED is 23
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == 1) {
                    if (lastForegroundTime == 0L) {
                        lastForegroundTime = event.timeStamp
                    }
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || 
                           event.eventType == 2 || 
                           event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                    if (lastForegroundTime > 0) {
                        totalTime += (event.timeStamp - lastForegroundTime)
                        lastForegroundTime = 0L
                    }
                }
            }
        }
        
        // If it's currently in the foreground
        if (lastForegroundTime > 0) {
            totalTime += (endTime - lastForegroundTime)
        }
        
        return totalTime
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


