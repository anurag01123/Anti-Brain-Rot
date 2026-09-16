package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val appDao: AppDao) {
    val allTrackedApps: Flow<List<TrackedApp>> = appDao.getAllTrackedApps()
    val allContacts: Flow<List<PenaltyContact>> = appDao.getAllContacts()
    val allDailyStats: Flow<List<DailyStat>> = appDao.getAllDailyStats()

    suspend fun insertTrackedApp(app: TrackedApp) = appDao.insertTrackedApp(app)
    
    suspend fun deleteTrackedApp(packageName: String) = appDao.deleteTrackedApp(packageName)
    
    suspend fun getTrackedApp(packageName: String) = appDao.getTrackedApp(packageName)
    
    suspend fun updateTrackedAppActiveState(packageName: String, isActive: Boolean) {
        appDao.updateTrackedAppActiveState(packageName, isActive)
    }

    suspend fun insertContact(contact: PenaltyContact) = appDao.insertContact(contact)
    
    suspend fun deleteContact(phoneNumber: String) = appDao.deleteContact(phoneNumber)
    
    suspend fun getAllContactsSync() = appDao.getAllContactsSync()
    
    suspend fun markContactCalled(phoneNumber: String, timestamp: Long) {
        val allContacts = appDao.getAllContactsSync()
        // Phones formats can differ, so let's do a loose matching or clean the number
        val cleanTarget = phoneNumber.replace(Regex("[^0-9+]"), "")
        val contactToUpdate = allContacts.find { 
            val cleanDbNum = it.phoneNumber.replace(Regex("[^0-9+]"), "")
            cleanDbNum == cleanTarget || cleanDbNum.endsWith(cleanTarget) || cleanTarget.endsWith(cleanDbNum)
        }
        if (contactToUpdate != null) {
            appDao.updateContactCallTime(contactToUpdate.phoneNumber, timestamp)
        }
    }

    suspend fun incrementUrgeInterrupted() {
        val today = LocalDate.now().toEpochDay()
        val stat = appDao.getDailyStatSync(today) ?: DailyStat(dateEpochDay = today)
        appDao.insertDailyStat(stat.copy(urgesInterrupted = stat.urgesInterrupted + 1))
    }

    suspend fun incrementBlockOccurrence() {
        val today = LocalDate.now().toEpochDay()
        val stat = appDao.getDailyStatSync(today) ?: DailyStat(dateEpochDay = today)
        appDao.insertDailyStat(stat.copy(blockOccurrences = stat.blockOccurrences + 1))
    }
}
