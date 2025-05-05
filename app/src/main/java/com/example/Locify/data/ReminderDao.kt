package com.example.Locify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    fun getReminderByIdFlow(reminderId: Long): Flow<Reminder?>

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isActive = 0 ORDER BY completedAt DESC")
    fun getCompletedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isLocationBased = 1 AND isActive = 1")
    fun getActiveLocationReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isTimeBased = 1 AND isActive = 1 AND triggerDateTime >= :now")
    fun getUpcomingTimeReminders(now: LocalDateTime): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isUnlockBased = 1 AND isActive = 1")
    fun getActiveUnlockReminders(): Flow<List<Reminder>>

    @Query("UPDATE reminders SET isActive = 0, completedAt = :completionTime WHERE id = :reminderId")
    suspend fun markReminderAsCompleted(reminderId: Long, completionTime: LocalDateTime)

    @Query("UPDATE reminders SET isActive = 1, completedAt = NULL WHERE id = :reminderId")
    suspend fun markReminderAsActive(reminderId: Long)

    @Query("UPDATE reminders SET isFavorite = :isFavorite WHERE id = :reminderId")
    suspend fun updateReminderFavoriteStatus(reminderId: Long, isFavorite: Boolean)
}