import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 10', content)
content = re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "2.0.5"', content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
