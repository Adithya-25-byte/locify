package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE isLocationBased = 1 AND isCompleted = 0")
    suspend fun getLocationBasedActiveReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE isTimeBased = 1 AND isCompleted = 0")
    suspend fun getTimeBasedActiveReminders(): List<Reminder>

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :reminderId")
    suspend fun markReminderAsCompleted(reminderId: Long)
}