package com.example.Locify.service

import android.content.Context
import android.content.Intent
import android.location.Location
import com.example.Locify.data.Reminder
import com.example.Locify.location.LocationClient
import com.example.Locify.notification.AlarmManager
import com.example.Locify.notification.NotificationHelper
import com.example.Locify.repository.ReminderRepository
import com.example.Locify.utility.DateTimeUtils
import com.example.Locify.utility.LocationUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

class ReminderManager @Inject constructor(
    private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val locationClient: LocationClient,
    private val notificationHelper: NotificationHelper,
    private val alarmManager: AlarmManager,
    private val reminderScheduler: ReminderScheduler
) {
    private val triggeredReminderIds = mutableSetOf<Long>()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun checkLocationBasedReminders(location: Location) {
        val activeReminders = reminderRepository.getLocationBasedActiveReminders()

        activeReminders.forEach { reminder ->
            // Check if this is a location+time reminder and the time hasn't come yet
            if (reminder.isLocationBased && reminder.isTimeBased &&
                reminder.triggerDateTime != null &&
                reminder.triggerDateTime > System.currentTimeMillis()) {
                return@forEach // Skip this reminder until the time comes
            }

            val distance = locationClient.calculateDistance(
                location.latitude, location.longitude,
                reminder.latitude, reminder.longitude
            )

            if (distance <= reminder.distanceThreshold) {
                if (!triggeredReminderIds.contains(reminder.id)) {
                    // We've entered the reminder area
                    triggeredReminderIds.add(reminder.id)
                    triggerReminder(reminder)
                }
            } else if (distance > reminder.distanceThreshold * 2) {
                // We've moved away from the reminder area
                if (triggeredReminderIds.contains(reminder.id)) {
                    val tasks = reminderRepository.getIncompleteTasksForReminder(reminder.id)
                    if (tasks.isNotEmpty()) {
                        // User is leaving the area without completing the reminder
                        // Trigger a new alarm
                        val reminderTasks = reminderRepository.getTasksForReminder(reminder.id).value ?: emptyList()
                        notificationHelper.triggerFullScreenAlarm(reminder, reminderTasks)
                    }
                    triggeredReminderIds.remove(reminder.id)
                }
            }
        }
    }

    suspend fun triggerTimeBasedReminder(reminderId: Long) {
        val reminder = reminderRepository.getReminderById(reminderId) ?: return

        if (reminder.isCompleted) return

        if (reminder.isLocationBased && reminder.isTimeBased) {
            // For location+time reminders, check if we're in the location
            val currentLocation = locationClient.getCurrentLocation() ?: return
            val distance = locationClient.calculateDistance(
                currentLocation.latitude, currentLocation.longitude,
                reminder.latitude, reminder.longitude
            )

            if (distance <= reminder.distanceThreshold) {
                triggerReminder(reminder)
            } else {
                // We're not at the location yet, so just show a notification
                val tasks = reminderRepository.getIncompleteTasksForReminder(reminderId)
                notificationHelper.showReminderNotification(reminder, tasks)
            }
        } else {
            // Pure time-based reminder
            triggerReminder(reminder)
        }
    }

    private suspend fun triggerReminder(reminder: Reminder) {
        val tasks = reminderRepository.getIncompleteTasksForReminder(reminder.id)

        // Show full-screen alarm
        notificationHelper.triggerFullScreenAlarm(reminder, tasks)

        // Also show a persistent notification
        notificationHelper.showReminderNotification(reminder, tasks)
    }

    suspend fun checkUnlockReminders() {
        val activeReminders = reminderRepository.getActiveReminders().value ?: return

        val currentLocation = locationClient.getCurrentLocation() ?: return

        activeReminders.forEach { reminder ->
            if (reminder.remindWhenUnlock && !reminder.isCompleted) {
                // Check if we're still in the reminder area for location-based reminders
                if (reminder.isLocationBased) {
                    val distance = locationClient.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        reminder.latitude, reminder.longitude
                    )

                    if (distance <= reminder.distanceThreshold) {
                        // We're in the area, show unlock dialog
                        val intent = Intent(context, UnlockReminderService::class.java).apply {
                            action = "SHOW_UNLOCK_DIALOG"
                            putExtra("reminder_id", reminder.id)
                        }
                        context.startService(intent)
                    }
                } else if (reminder.isTimeBased &&
                    reminder.triggerDateTime != null &&
                    reminder.triggerDateTime <= System.currentTimeMillis()) {
                    // Time-based reminder that is due
                    val intent = Intent(context, UnlockReminderService::class.java).apply {
                        action = "SHOW_UNLOCK_DIALOG"
                        putExtra("reminder_id", reminder.id)
                    }
                    context.startService(intent)
                }
            }
        }
    }

    suspend fun rescheduleAllReminders() {
        val timeBasedReminders = reminderRepository.getTimeBasedActiveReminders()
        reminderScheduler.rescheduleAllReminders(timeBasedReminders)
    }
}
