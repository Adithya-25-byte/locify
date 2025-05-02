package com.example.Locify.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId ORDER BY `order` ASC")
    fun getTasksForReminder(reminderId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId AND isCompleted = 0 ORDER BY `order` ASC")
    fun getIncompleteTasksForReminder(reminderId: Long): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE reminderId = :reminderId AND isCompleted = 0")
    fun getIncompleteTaskCountForReminder(reminderId: Long): Flow<Int>

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean, completedAt: String?)

    @Query("DELETE FROM tasks WHERE reminderId = :reminderId")
    suspend fun deleteTasksForReminder(reminderId: Long)
}