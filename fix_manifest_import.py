import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

content = content.replace("<application\\n        android:enableOnBackInvokedCallback=\"true\"", '<application\n        android:enableOnBackInvokedCallback="true"')

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    vm_content = f.read()

if "import kotlinx.coroutines.isActive" not in vm_content:
    vm_content = vm_content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.isActive")

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(vm_content)
