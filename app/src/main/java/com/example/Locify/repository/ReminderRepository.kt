package com.example.Locify.repository

import com.example.Locify.data.Reminder
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.Task
import com.example.Locify.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao
) {
    // Reminder operations
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    fun getActiveReminders(): Flow<List<Reminder>> = reminderDao.getActiveReminders()

    fun getCompletedReminders(): Flow<List<Reminder>> = reminderDao.getCompletedReminders()

    fun getActiveLocationBasedReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveLocationBasedReminders()

    fun getActiveTimeBasedReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveTimeBasedReminders()

    fun getFavoriteReminders(): Flow<List<Reminder>> = reminderDao.getFavoriteReminders()

    suspend fun getReminderById(id: Long): Reminder? = reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) {
        // Delete associated tasks first
        taskDao.deleteTasksForReminder(reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    suspend fun updateReminderCompletionStatus(reminderId: Long, isCompleted: Boolean) {
        reminderDao.updateReminderCompletionStatus(
            reminderId = reminderId,
            isCompleted = isCompleted,
            updatedAt = LocalDateTime.now()
        )
    }

    suspend fun toggleReminderFavoriteStatus(reminderId: Long, isFavorite: Boolean) {
        reminderDao.updateReminderFavoriteStatus(reminderId, isFavorite)
    }

    suspend fun getRemindersToTrigger(): List<Reminder> =
        reminderDao.getRemindersToTrigger(LocalDateTime.now())

    suspend fun getRemindersNearLocation(
        latitude: Double,
        longitude: Double,
        maxDistance: Float
    ): List<Reminder> = reminderDao.getRemindersNearLocation(latitude, longitude, maxDistance)

    suspend fun getCompletedRepeatingReminders(): List<Reminder> =
        reminderDao.getCompletedRepeatingReminders()

    // Task operations
    fun getTasksForReminder(reminderId: Long): Flow<List<Task>> =
        taskDao.getTasksForReminder(reminderId)

    fun getIncompleteTasksForReminder(reminderId: Long): Flow<List<Task>> =
        taskDao.getIncompleteTasksForReminder(reminderId)

    fun getIncompleteTaskCountForReminder(reminderId: Long): Flow<Int> =
        taskDao.getIncompleteTaskCountForReminder(reminderId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<Task>): List<Long> = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) LocalDateTime.now().toString() else null
        taskDao.updateTaskCompletionStatus(taskId, isCompleted, completedAt)
    }

    // Combined operations
    suspend fun insertReminderWithTasks(
        reminder: Reminder,
        taskDescriptions: List<String>
    ): Long = withContext(Dispatchers.IO) {
        val reminderId = reminderDao.insertReminder(reminder)

        val tasks = taskDescriptions.mapIndexed { index, description ->
            Task(
                reminderId = reminderId,
                description = description,
                order = index,
                isCompleted = false
            )
        }

        taskDao.insertTasks(tasks)
        reminderId
    }

    suspend fun isReminderCompleted(reminderId: Long): Boolean {
        val incompleteCount = taskDao.getIncompleteTaskCountForReminder(reminderId).hashCode()
        return incompleteCount == 0
    }
}