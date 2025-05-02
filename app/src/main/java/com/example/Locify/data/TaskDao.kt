package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Insert
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId ORDER BY createdAt ASC")
    fun getTasksForReminder(reminderId: Long): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE reminderId = :reminderId AND isCompleted = 0")
    suspend fun getIncompleteTasksForReminder(reminderId: Long): List<Task>

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean)

    @Query("DELETE FROM tasks WHERE reminderId = :reminderId")
    suspend fun deleteTasksForReminder(reminderId: Long)
}