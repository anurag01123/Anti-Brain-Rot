import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_launched_effect = """            LaunchedEffect(Unit) {
                while (true) {
                    hasPermission = UsageUtils.hasUsageStatsPermission(context)
                    kotlinx.coroutines.delay(1000)
                }
            }"""

new_launched_effect = """            LaunchedEffect(Unit) {
                while (!hasPermission) {
                    hasPermission = UsageUtils.hasUsageStatsPermission(context)
                    if (hasPermission) break
                    kotlinx.coroutines.delay(1000)
                }
            }"""

content = content.replace(old_launched_effect, new_launched_effect)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
