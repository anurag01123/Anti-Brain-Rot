package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_events")
data class UnlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val bonusMinutesGranted: Int,
    val newEffectiveLimitMinutes: Int
)
