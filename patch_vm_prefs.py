import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

imports = """
import android.content.Context
import android.content.SharedPreferences
"""
if "import android.content.SharedPreferences" not in content:
    content = content.replace("import androidx.lifecycle.AndroidViewModel", "import androidx.lifecycle.AndroidViewModel" + imports)

# Add properties
props = """
    private val prefs: SharedPreferences = application.getSharedPreferences("block_settings", Context.MODE_PRIVATE)

    private val _isGlobalBlock = MutableStateFlow(prefs.getBoolean("block_all", false))
    val isGlobalBlock: StateFlow<Boolean> = _isGlobalBlock

    private val _isOvernightBlock = MutableStateFlow(prefs.getBoolean("overnight_block", false))
    val isOvernightBlock: StateFlow<Boolean> = _isOvernightBlock

    private val _isAntiDoom = MutableStateFlow(prefs.getBoolean("anti_doom", false))
    val isAntiDoom: StateFlow<Boolean> = _isAntiDoom

    fun setGlobalBlock(active: Boolean) {
        prefs.edit().putBoolean("block_all", active).apply()
        _isGlobalBlock.value = active
    }

    fun setOvernightBlock(active: Boolean) {
        prefs.edit().putBoolean("overnight_block", active).apply()
        _isOvernightBlock.value = active
    }

    fun setAntiDoom(active: Boolean) {
        prefs.edit().putBoolean("anti_doom", active).apply()
        _isAntiDoom.value = active
    }
"""

# Insert props inside class MainViewModel
content = content.replace("class MainViewModel(application: Application) : AndroidViewModel(application) {", "class MainViewModel(application: Application) : AndroidViewModel(application) {" + props)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
