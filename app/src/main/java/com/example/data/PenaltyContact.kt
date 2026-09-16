package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "penalty_contacts")
data class PenaltyContact(
    @PrimaryKey
    val phoneNumber: String,
    val contactName: String,
    val lastCalledTimestamp: Long = 0L
)
