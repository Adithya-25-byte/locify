package com.example.Locify.repository

import com.example.Locify.data.Reminder
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.Task
import com.example.Locify.data.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling all reminder-related operations
 */
@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao
) {
    // Basic Reminder Operations
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    fun getActiveReminders(): Flow<List<Reminder>> = reminderDao.getActiveReminders()

    fun getCompletedReminders(): Flow<List<Reminder>> = reminderDao.getCompletedReminders()

    fun getFavoriteReminders(): Flow<List<Reminder>> = reminderDao.getFavoriteReminders()

    suspend fun getReminderById(reminderId: Long): Reminder? = reminderDao.getReminderById(reminderId)

    suspend fun getLocationBasedReminders(): List<Reminder> = reminderDao.getLocationBasedReminders()

    suspend fun getTimeBasedReminders(): List<Reminder> = reminderDao.getTimeBasedReminders()

    suspend fun getDueReminders(): List<Reminder> =
        reminderDao.getDueReminders(LocalDateTime.now())

    suspend fun getRemindOnUnlockReminders(): List<Reminder> =
        reminderDao.getRemindOnUnlockReminders()

    // Task Operations
    fun getTasksForReminder(reminderId: Long): Flow<List<Task>> =
        taskDao.getTasksForReminder(reminderId)

    suspend fun getTasksForReminderImmediate(reminderId: Long): List<Task> =
        taskDao.getTasksForReminderImmediate(reminderId)

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    // Combined Operations
    suspend fun saveReminderWithTasks(reminder: Reminder, tasks: List<Task>): Long {
        val reminderId = reminderDao.insertReminder(reminder)

        // Insert tasks with the new reminderId
        tasks.forEach { task ->
            taskDao.insertTask(task.copy(reminderId = reminderId))
        }

        return reminderId
    }

    suspend fun updateReminderWithTasks(reminder: Reminder, tasks: List<Task>) {
        reminderDao.updateReminderWithTasks(reminder, tasks, taskDao)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        // The cascade delete will handle removing associated tasks
        reminderDao.deleteReminder(reminder)
    }

    // Status Updates
    suspend fun updateReminderCompletionStatus(reminderId: Long, isCompleted: Boolean) {
        reminderDao.updateReminderCompletionStatus(
            reminderId,
            isCompleted,
            LocalDateTime.now()
        )
    }

    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) LocalDateTime.now() else null
        taskDao.updateTaskCompletionStatus(
            taskId,
            isCompleted,
            completedAt?.toEpochSecond(java.time.ZoneOffset.UTC)
        )

        // Check if all tasks are completed to update the reminder status
        val task = taskDao.getTaskById(taskId) ?: return
        val allTasks = taskDao.getTasksForReminderImmediate(task.reminderId)
        val allCompleted = allTasks.all { it.isCompleted }

        if (allCompleted) {
            updateReminderCompletionStatus(task.reminderId, true)
        } else {
            // If any task is not completed, ensure reminder is marked as not completed
            updateReminderCompletionStatus(task.reminderId, false)
        }
    }

    suspend fun toggleFavoriteStatus(reminderId: Long) {
        val reminder = reminderDao.getReminderById(reminderId) ?: return
        reminderDao.updateFavoriteStatus(reminderId, !reminder.isFavorite)
    }
}