package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM tracked_apps")
    fun getAllTrackedApps(): Flow<List<TrackedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedApp(app: TrackedApp)

    @Query("DELETE FROM tracked_apps WHERE packageName = :packageName")
    suspend fun deleteTrackedApp(packageName: String)

    @Query("SELECT * FROM tracked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getTrackedApp(packageName: String): TrackedApp?

    @Query("UPDATE tracked_apps SET isActive = :isActive WHERE packageName = :packageName")
    suspend fun updateTrackedAppActiveState(packageName: String, isActive: Boolean)
    
    @Query("SELECT * FROM penalty_contacts")
    fun getAllContacts(): Flow<List<PenaltyContact>>
    
    @Query("SELECT * FROM penalty_contacts")
    suspend fun getAllContactsSync(): List<PenaltyContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: PenaltyContact)

    @Query("DELETE FROM penalty_contacts WHERE phoneNumber = :phoneNumber")
    suspend fun deleteContact(phoneNumber: String)
    
    @Query("UPDATE penalty_contacts SET lastCalledTimestamp = :timestamp WHERE phoneNumber = :phoneNumber")
    suspend fun updateContactCallTime(phoneNumber: String, timestamp: Long)

    @Query("SELECT * FROM daily_stats ORDER BY dateEpochDay DESC")
    fun getAllDailyStats(): Flow<List<DailyStat>>

    @Query("SELECT * FROM daily_stats WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getDailyStatSync(dateEpochDay: Long): DailyStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStat(stat: DailyStat)
}
