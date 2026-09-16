import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

props = """    private val _isOvernightBlock = MutableStateFlow(prefs.getBoolean("overnight_block", false))
    val isOvernightBlock: StateFlow<Boolean> = _isOvernightBlock

    private val _overnightStartHour = MutableStateFlow(prefs.getInt("overnight_start_hour", 22))
    val overnightStartHour: StateFlow<Int> = _overnightStartHour

    private val _overnightEndHour = MutableStateFlow(prefs.getInt("overnight_end_hour", 7))
    val overnightEndHour: StateFlow<Int> = _overnightEndHour
"""

content = content.replace('    private val _isOvernightBlock = MutableStateFlow(prefs.getBoolean("overnight_block", false))\n    val isOvernightBlock: StateFlow<Boolean> = _isOvernightBlock\n', props)

funcs = """    fun setOvernightBlock(active: Boolean) {
        prefs.edit().putBoolean("overnight_block", active).apply()
        _isOvernightBlock.value = active
    }

    fun setOvernightStartHour(hour: Int) {
        prefs.edit().putInt("overnight_start_hour", hour).apply()
        _overnightStartHour.value = hour
    }

    fun setOvernightEndHour(hour: Int) {
        prefs.edit().putInt("overnight_end_hour", hour).apply()
        _overnightEndHour.value = hour
    }
"""

content = content.replace('    fun setOvernightBlock(active: Boolean) {\n        prefs.edit().putBoolean("overnight_block", active).apply()\n        _isOvernightBlock.value = active\n    }\n', funcs)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
