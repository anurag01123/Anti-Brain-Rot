import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("\\nimport android.app.AppOpsManager", "\nimport android.app.AppOpsManager")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
