package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "favorite_reminders")
data class FavoriteReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val dateTime: LocalDateTime? = null,
    val enableUnlockReminder: Boolean = false,
    val repeatType: Int = 0, // 0: None, 1: Daily, 2: Weekly, 3: Monthly
    val creationTimestamp: Long = System.currentTimeMillis()
)