package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey val dateEpochDay: Long,
    val urgesInterrupted: Int = 0,
    val blockOccurrences: Int = 0,
    val unlockOccurrences: Int = 0
)
