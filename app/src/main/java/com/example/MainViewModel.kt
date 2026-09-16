package com.example

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import android.content.Context
import android.content.SharedPreferences

import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.PenaltyContact
import com.example.data.DailyStat
import com.example.data.UnlockEvent
import java.util.Calendar
import com.example.data.TrackedApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("block_settings", Context.MODE_PRIVATE)

    private val _isGlobalBlock = MutableStateFlow(prefs.getBoolean("block_all", false))
    val isGlobalBlock: StateFlow<Boolean> = _isGlobalBlock

    private val _isOvernightBlock = MutableStateFlow(prefs.getBoolean("overnight_block", false))
    val isOvernightBlock: StateFlow<Boolean> = _isOvernightBlock

    private val _overnightStartHour = MutableStateFlow(prefs.getInt("overnight_start_hour", 22))
    val overnightStartHour: StateFlow<Int> = _overnightStartHour

    private val _overnightEndHour = MutableStateFlow(prefs.getInt("overnight_end_hour", 7))
    val overnightEndHour: StateFlow<Int> = _overnightEndHour

    private val _isAntiDoom = MutableStateFlow(prefs.getBoolean("anti_doom", false))
    val isAntiDoom: StateFlow<Boolean> = _isAntiDoom

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode


    fun setGlobalBlock(active: Boolean) {
        prefs.edit().putBoolean("block_all", active).apply()
        _isGlobalBlock.value = active
    }

    fun setOvernightBlock(active: Boolean) {
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

    fun setAntiDoom(active: Boolean) {
        prefs.edit().putBoolean("anti_doom", active).apply()
        _isAntiDoom.value = active
    }

    fun setDarkMode(active: Boolean) {
        prefs.edit().putBoolean("dark_mode", active).apply()
        _isDarkMode.value = active
    }



    private val repository: AppRepository
    
    val allTrackedApps: StateFlow<List<TrackedApp>>
    val allContacts: StateFlow<List<PenaltyContact>>
    val allDailyStats: StateFlow<List<com.example.data.DailyStat>>
    val todayUnlockEvents: StateFlow<List<UnlockEvent>>
    
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps
    
    val appUsages: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Long> = androidx.compose.runtime.mutableStateMapOf()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db.appDao())
        
        allTrackedApps = repository.allTrackedApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        
        allContacts = repository.allContacts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allDailyStats = repository.allDailyStats.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        todayUnlockEvents = repository.getRecentUnlockEvents(cal.timeInMillis).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        
        loadInstalledApps()

        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            allTrackedApps.collect { apps ->
                while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                    if (apps.isEmpty()) break
                    apps.forEach { app ->
                        val usage = com.example.utils.UsageUtils.getUsageTimeForApp(application, app.packageName)
                        if (appUsages[app.packageName] != usage) {
                            appUsages[app.packageName] = usage
                        }
                    }
                    kotlinx.coroutines.delay(2000)
                }
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val apps = resolveInfos.map { 
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    appName = it.loadLabel(pm).toString()
                )
            }.distinctBy { it.packageName }.sortedBy { it.appName }
            _installedApps.value = apps
        }
    }

    fun addTrackedApp(packageName: String, appName: String, limitMinutes: Int) {
        viewModelScope.launch {
            repository.insertTrackedApp(TrackedApp(packageName, appName, limitMinutes))
        }
    }

    fun removeTrackedApp(packageName: String) {
        viewModelScope.launch {
            repository.deleteTrackedApp(packageName)
        }
    }

    fun toggleAppActive(packageName: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateTrackedAppActiveState(packageName, isActive)
        }
    }

    fun addContact(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertContact(PenaltyContact(phoneNumber = phone, contactName = name))
        }
    }

    fun addContactFromUri(context: Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val idIndex = c.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                    val nameIndex = c.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneIndex = c.getColumnIndex(android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    
                    if (idIndex >= 0 && nameIndex >= 0 && hasPhoneIndex >= 0) {
                        val id = c.getString(idIndex)
                        val name = c.getString(nameIndex)
                        val hasPhone = c.getString(hasPhoneIndex)
                        
                        if (hasPhone == "1") {
                            val phoneCursor = context.contentResolver.query(
                                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(id),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val numberIndex = pc.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numberIndex >= 0) {
                                        val number = pc.getString(numberIndex)
                                        addContact(name, number)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun removeContact(phone: String) {
        viewModelScope.launch {
            repository.deleteContact(phone)
        }
    }
}

data class AppInfo(val packageName: String, val appName: String)
