import re

with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "r") as f:
    content = f.read()

old_logic = """            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isOvernight = currentHour >= 22 || currentHour < 7
            if (isOvernightBlock && isOvernight) {
                shouldBlock = true
                blockReason = "Overnight block is active."
            }"""

new_logic = """            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
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
            }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/example/BlockerAccessibilityService.kt", "w") as f:
    f.write(content)
