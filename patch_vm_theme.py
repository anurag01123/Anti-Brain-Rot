import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

props = """    private val _isAntiDoom = MutableStateFlow(prefs.getBoolean("anti_doom", false))
    val isAntiDoom: StateFlow<Boolean> = _isAntiDoom

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode
"""

if "_isDarkMode" not in content:
    content = content.replace("    private val _isAntiDoom = MutableStateFlow(prefs.getBoolean(\"anti_doom\", false))\n    val isAntiDoom: StateFlow<Boolean> = _isAntiDoom", props)

funcs = """    fun setAntiDoom(active: Boolean) {
        prefs.edit().putBoolean("anti_doom", active).apply()
        _isAntiDoom.value = active
    }

    fun setDarkMode(active: Boolean) {
        prefs.edit().putBoolean("dark_mode", active).apply()
        _isDarkMode.value = active
    }
"""

if "fun setDarkMode" not in content:
    content = content.replace("    fun setAntiDoom(active: Boolean) {\n        prefs.edit().putBoolean(\"anti_doom\", active).apply()\n        _isAntiDoom.value = active\n    }", funcs)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
