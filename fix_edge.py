import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_oncreate = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissionLauncher.launch("""

new_oncreate = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.activity.enableEdgeToEdge()
        
        requestPermissionLauncher.launch("""

content = content.replace(old_oncreate, new_oncreate)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
