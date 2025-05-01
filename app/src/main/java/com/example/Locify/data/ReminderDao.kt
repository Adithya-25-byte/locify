package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Reminder entities with enhanced queries
 */
@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Long): Reminder?

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY modifiedAt DESC")
    fun getCompletedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteReminders(): Flow<List<Reminder>>

    // Location-based queries
    @Query("SELECT * FROM reminders WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND isCompleted = 0")
    suspend fun getLocationBasedReminders(): List<Reminder>

    // Time-based queries
    @Query("SELECT * FROM reminders WHERE hasTimeConstraint = 1 AND scheduledDateTime IS NOT NULL AND isCompleted = 0")
    suspend fun getTimeBasedReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE hasTimeConstraint = 1 AND scheduledDateTime <= :currentTime AND isCompleted = 0")
    suspend fun getDueReminders(currentTime: LocalDateTime): List<Reminder>

    // Feature-specific queries
    @Query("SELECT * FROM reminders WHERE remindOnUnlock = 1 AND isCompleted = 0")
    suspend fun getRemindOnUnlockReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE isRepeating = 1")
    suspend fun getRepeatingReminders(): List<Reminder>

    @Query("UPDATE reminders SET isCompleted = :isCompleted, modifiedAt = :modifiedAt WHERE id = :reminderId")
    suspend fun updateReminderCompletionStatus(reminderId: Long, isCompleted: Boolean, modifiedAt: LocalDateTime)

    @Query("UPDATE reminders SET isFavorite = :isFavorite WHERE id = :reminderId")
    suspend fun updateFavoriteStatus(reminderId: Long, isFavorite: Boolean)

    // Combined reminder updates in a transaction
    @Transaction
    suspend fun updateReminderWithTasks(reminder: Reminder, tasks: List<Task>, taskDao: TaskDao) {
        updateReminder(reminder)
        taskDao.deleteTasksForReminder(reminder.id)
        tasks.forEach { task ->
            taskDao.insertTask(task.copy(reminderId = reminder.id))
        }
    }
}