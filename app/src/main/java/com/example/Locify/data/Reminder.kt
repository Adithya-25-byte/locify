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

    // Basic reminder info
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,

    // Location-based attributes
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Float = 100f, // Default radius in meters

    // Time-based attributes
    val hasTimeConstraint: Boolean = false,
    val scheduledTime: LocalDateTime? = null,

    // Type of reminder
    val isLocationBased: Boolean = true,
    val isTimeBased: Boolean = false, // Pure time-based reminder

    // Additional features
    val remindOnUnlock: Boolean = false, // Remind when phone unlocks
    val reAlertOnLeaving: Boolean = false, // Re-alert when leaving location

    // Repetition settings
    val isRepeating: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatInterval: Int = 0, // Days/weeks/months depending on repeatType
    val repeatEndDate: LocalDateTime? = null,

    // Created/updated timestamps
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}