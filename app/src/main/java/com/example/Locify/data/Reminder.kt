package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.Locify.data.Converters
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isLocationBased: Boolean = true,
    val isTimeBased: Boolean = false,
    val triggerDateTime: Long? = null,
    val remindWhenUnlock: Boolean = false,
    val distanceThreshold: Float = 100f // Distance in meters
)