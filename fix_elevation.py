import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("shadowElevation = 8.dp,", "shadowElevation = 0.dp,")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
