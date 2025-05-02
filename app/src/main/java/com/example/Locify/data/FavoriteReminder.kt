package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "favorite_reminders")
data class FavoriteReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val isLocationBased: Boolean = true,
    val isTimeBased: Boolean = false,
    val hasRepeat: Boolean = false,
    val repeatInterval: Int = 0, // 0: none, 1: daily, 2: weekly, 3: monthly
    val remindWhenUnlock: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)