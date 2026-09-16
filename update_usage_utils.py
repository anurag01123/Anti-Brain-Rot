import re

with open("app/src/main/java/com/example/utils/UsageUtils.kt", "r") as f:
    content = f.read()

new_method = """
    fun getContinuousUsageTimeForApp(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        // Look back last 1 hour
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (60 * 60 * 1000L)
        
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        
        var lastResumed = 0L
        var isForeground = False
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == 1) {
                    if (lastResumed == 0L) {
                        lastResumed = event.timeStamp
                    }
                    isForeground = True
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || 
                           event.eventType == 2 || 
                           event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                    lastResumed = 0L
                    isForeground = False
                }
            }
        }
        
        if (lastResumed > 0 && isForeground) {
            return endTime - lastResumed
        }
        return 0L
    }
}
"""

content = content.replace("    }\n}", "    }\n" + new_method.replace("True", "true").replace("False", "false"))

with open("app/src/main/java/com/example/utils/UsageUtils.kt", "w") as f:
    f.write(content)
