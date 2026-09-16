import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace('Text("${totalUsageMins / 60}h${totalUsageMins % 60}"', 'Text("${totalUsageMins / 60}h ${totalUsageMins % 60}m"')

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
