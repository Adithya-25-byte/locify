package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task entities
 */
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>): List<Long>

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId ORDER BY orderPosition ASC")
    fun getTasksForReminder(reminderId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId ORDER BY orderPosition ASC")
    suspend fun getTasksForReminderImmediate(reminderId: Long): List<Task>

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM tasks WHERE reminderId = :reminderId")
    suspend fun getTaskCountForReminder(reminderId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE reminderId = :reminderId AND isCompleted = 1")
    suspend fun getCompletedTaskCountForReminder(reminderId: Long): Int

    @Query("DELETE FROM tasks WHERE reminderId = :reminderId")
    suspend fun deleteTasksForReminder(reminderId: Long)
}