import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.activity.enableEdgeToEdge(", "this@MainActivity.enableEdgeToEdge(")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
