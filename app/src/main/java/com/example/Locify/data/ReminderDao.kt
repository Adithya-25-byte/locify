package com.example.Locify.data

import androidx.room.*
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

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE isLocationBased = 1 AND isCompleted = 0")
    fun getActiveLocationBasedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isTimeBased = 1 AND hasTimeConstraint = 1 AND isCompleted = 0")
    fun getActiveTimeBasedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteReminders(): Flow<List<Reminder>>

    @Query("UPDATE reminders SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :reminderId")
    suspend fun updateReminderCompletionStatus(reminderId: Long, isCompleted: Boolean, updatedAt: LocalDateTime)

    @Query("UPDATE reminders SET isFavorite = :isFavorite WHERE id = :reminderId")
    suspend fun updateReminderFavoriteStatus(reminderId: Long, isFavorite: Boolean)

    // For time-based reminders that need to be triggered
    @Query("SELECT * FROM reminders WHERE isTimeBased = 1 AND hasTimeConstraint = 1 AND isCompleted = 0 AND scheduledTime <= :currentTime")
    suspend fun getRemindersToTrigger(currentTime: LocalDateTime): List<Reminder>

    // For location-based reminders within a specific area
    @Query("SELECT * FROM reminders WHERE isLocationBased = 1 AND isCompleted = 0 AND " +
            "(:latitude BETWEEN latitude - (:maxDistance / 111111) AND latitude + (:maxDistance / 111111)) AND " +
            "(:longitude BETWEEN longitude - (:maxDistance / (111111 * COS(latitude * 3.14159 / 180))) AND longitude + (:maxDistance / (111111 * COS(latitude * 3.14159 / 180))))")
    suspend fun getRemindersNearLocation(latitude: Double, longitude: Double, maxDistance: Float): List<Reminder>

    // For handling repeated reminders
    @Query("SELECT * FROM reminders WHERE isRepeating = 1 AND isCompleted = 1")
    suspend fun getCompletedRepeatingReminders(): List<Reminder>
}