import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('versionCode = 4', 'versionCode = 5')
content = content.replace('versionName = "1.0.4"', 'versionName = "2.0.0"')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
