package com.example.Locify.repository

import androidx.lifecycle.LiveData
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

class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao
) {
    val allReminders = reminderDao.getAllReminders()
    val activeReminders = reminderDao.getActiveReminders()

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insert(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.update(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        taskDao.deleteTasksForReminder(reminder.id)
        reminderDao.delete(reminder)
    }

    suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)
    }

    fun getTasksForReminder(reminderId: Long): LiveData<List<Task>> {
        return taskDao.getTasksForReminder(reminderId)
    }

    suspend fun getIncompleteTasksForReminder(reminderId: Long): List<Task> {
        return taskDao.getIncompleteTasksForReminder(reminderId)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insert(task)
    }

    suspend fun insertAllTasks(tasks: List<Task>) {
        taskDao.insertAll(tasks)
    }

    suspend fun updateTask(task: Task) {
        taskDao.update(task)
    }

    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) {
        taskDao.updateTaskCompletionStatus(taskId, isCompleted)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    suspend fun markReminderAsCompleted(reminderId: Long) {
        reminderDao.markReminderAsCompleted(reminderId)
    }

    suspend fun getLocationBasedActiveReminders(): List<Reminder> {
        return reminderDao.getLocationBasedActiveReminders()
    }

    suspend fun getTimeBasedActiveReminders(): List<Reminder> {
        return reminderDao.getTimeBasedActiveReminders()
    }
}