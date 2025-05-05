package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.Locify.data.Converters
import java.time.LocalDateTime

@Entity(tableName = "reminders")
@TypeConverters(Converters::class)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val locationName: String = "",
    val radius: Float = 100f, // Default radius in meters
    val isActive: Boolean = true,
    val isLocationBased: Boolean = true,
    val isTimeBased: Boolean = false,
    val isUnlockBased: Boolean = false,
    val triggerDateTime: LocalDateTime? = null,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatInterval: Int = 0, // For custom repeat intervals
    val repeatDays: List<Int> = emptyList(), // For weekly repeats (1=Monday, 7=Sunday)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val isFavorite: Boolean = false
)

enum class RepeatType {
    NONE,
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}