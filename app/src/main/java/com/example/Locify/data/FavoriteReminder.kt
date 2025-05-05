package com.example.Locify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.Locify.data.Converters
import java.time.LocalDateTime

@Entity(tableName = "favorite_reminders")
@TypeConverters(Converters::class)
data class FavoriteReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isLocationBased: Boolean = true,
    val isTimeBased: Boolean = false,
    val isUnlockBased: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatInterval: Int = 0,
    val repeatDays: List<Int> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
    // Note: Doesn't store actual location or time as these are template reminders
)