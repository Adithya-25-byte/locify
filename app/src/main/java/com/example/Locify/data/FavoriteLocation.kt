package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.Locify.data.Converters
import java.time.LocalDateTime

@Entity(tableName = "favorite_locations")
@TypeConverters(Converters::class)
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f, // Default radius in meters
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)