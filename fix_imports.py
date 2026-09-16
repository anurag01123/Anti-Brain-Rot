import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add the missing imports for ContextCompat and PackageManager
imports_to_add = """
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
"""

# Insert imports at the top (after package declaration)
content = content.replace("package com.example", f"package com.example{imports_to_add}")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
