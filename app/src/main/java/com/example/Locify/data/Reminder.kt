package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime

/**
 * Entity representing a location-based reminder with enhanced features
 */
@Entity(tableName = "reminders")
@TypeConverters(Converters::class)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Basic reminder details
    val title: String,
    val description: String = "",

    // Location details
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val radius: Float = 100f, // Default radius in meters

    // Date and time details
    val hasTimeConstraint: Boolean = false,
    val scheduledDateTime: LocalDateTime? = null,

    // Reminder status
    val isCompleted: Boolean = false,

    // Repetition details
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NEVER,
    val repeatCustomDays: List<Int> = emptyList(), // Days of week (1-7) for custom repeats

    // Advanced features
    val remindOnUnlock: Boolean = false,
    val reAlertWhenLeaving: Boolean = false,
    val distanceToReAlert: Float = 200f, // Distance in meters to re-alert

    // Favorite status
    val isFavorite: Boolean = false,

    // Creation and modification timestamps
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Helper function to determine if this is a location-based reminder
     */
    fun isLocationBased(): Boolean {
        return latitude != null && longitude != null
    }

    /**
     * Helper function to determine if this is a time-based reminder
     */
    fun isTimeBased(): Boolean {
        return hasTimeConstraint && scheduledDateTime != null
    }

    /**
     * Helper function to determine if this is a combined (location + time) reminder
     */
    fun isCombinedReminder(): Boolean {
        return isLocationBased() && isTimeBased()
    }

    /**
     * Helper function to validate if a reminder is properly configured
     */
    fun isValidReminder(): Boolean {
        // Either location or time must be set
        return isLocationBased() || isTimeBased()
    }
}

/**
 * Enum representing repeat intervals for reminders
 */
enum class RepeatInterval {
    NEVER,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM // For custom day selection
}