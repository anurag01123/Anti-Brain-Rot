import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_social = """                            Spacer(modifier = Modifier.width(16.dp))
                            // A couple fake social icons to match the design visually
                            Text("📸", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text("👔", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text("📘", fontSize = 24.sp)
                        }"""

new_social = """                        }"""

content = content.replace(old_social, new_social)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
