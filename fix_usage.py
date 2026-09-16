import re

with open("app/src/main/java/com/example/utils/UsageUtils.kt", "r") as f:
    content = f.read()

old_func = """    fun getUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()
        if (startTime >= endTime) return 0L

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        return stats.filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
            .coerceIn(0L, endTime - startTime)
    }"""

new_func = """    fun getUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()
        if (startTime >= endTime) return 0L

        // queryUsageStats is notoriously inaccurate for precise daily tracking 
        // because Android's internal daily buckets don't align with local timezone midnight.
        // We use queryEvents to get exact foreground time from midnight to now.
        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var totalTime = 0L
        var lastResumed = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == 1) {
                    if (lastResumed == 0L) {
                        lastResumed = event.timeStamp
                    }
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || 
                           event.eventType == 2 || 
                           event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                    if (lastResumed > 0L) {
                        totalTime += (event.timeStamp - lastResumed)
                        lastResumed = 0L
                    }
                }
            }
        }
        // If the app is currently open and running, add the time since it was last resumed
        if (lastResumed > 0L) {
            totalTime += (endTime - lastResumed)
        }

        return totalTime.coerceIn(0L, endTime - startTime)
    }"""

content = content.replace(old_func, new_func)

with open("app/src/main/java/com/example/utils/UsageUtils.kt", "w") as f:
    f.write(content)
