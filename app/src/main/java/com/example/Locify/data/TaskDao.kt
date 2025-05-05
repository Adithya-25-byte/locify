package com.example.Locify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("DELETE FROM tasks WHERE reminderId = :reminderId")
    suspend fun deleteTasksByReminderId(reminderId: Long)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId ORDER BY `order` ASC")
    fun getTasksForReminder(reminderId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId ORDER BY `order` ASC")
    suspend fun getTasksForReminderSync(reminderId: Long): List<Task>

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completionTime WHERE id = :taskId")
    suspend fun updateTaskCompletedStatus(taskId: Long, isCompleted: Boolean, completionTime: LocalDateTime?)

    @Query("SELECT COUNT(*) FROM tasks WHERE reminderId = :reminderId")
    suspend fun getTaskCountForReminder(reminderId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE reminderId = :reminderId AND isCompleted = 1")
    suspend fun getCompletedTaskCountForReminder(reminderId: Long): Int
}