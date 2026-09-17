import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

content = content.replace(
    'android:name=".BlockerAccessibilityService"\n            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"\n            android:exported="false"',
    'android:name=".BlockerAccessibilityService"\n            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"\n            android:exported="true"'
)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/utils/UsageUtils.kt", "r") as f:
    usage_content = f.read()

# Replace getUsageTimeForApp logic
new_usage_func = """    fun getUsageTimeForApp(context: Context, packageName: String): Long {
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
    }"""

usage_content = re.sub(r'    fun getUsageTimeForApp\(context: Context, packageName: String\): Long \{.*?(?=    fun getContinuousUsageTimeForApp)', new_usage_func + "\n\n", usage_content, flags=re.DOTALL)

with open("app/src/main/java/com/example/utils/UsageUtils.kt", "w") as f:
    f.write(usage_content)
