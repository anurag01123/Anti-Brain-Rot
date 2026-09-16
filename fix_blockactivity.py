import re

with open("app/src/main/java/com/example/BlockActivity.kt", "r") as f:
    content = f.read()

# Remove onBackPressed block
content = re.sub(r'\s*@Deprecated\("Deprecated in Java"\)\s*override fun onBackPressed\(\)\s*\{[^}]*\}', '', content, flags=re.MULTILINE)

with open("app/src/main/java/com/example/BlockActivity.kt", "w") as f:
    f.write(content)
