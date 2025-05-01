package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime

/**
 * Entity representing a favorite location that can be quickly selected
 * when creating reminders
 */
@Entity(tableName = "favorite_locations")
@TypeConverters(Converters::class)
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Location details
    val name: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val address: String = "",

    // Optional custom radius for this location
    val defaultRadius: Float = 100f,

    // For UI organization (sorting/grouping)
    val category: String = "",

    // Usage statistics
    val usageCount: Int = 0,
    val lastUsed: LocalDateTime? = null,

    // Timestamps
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now()
)