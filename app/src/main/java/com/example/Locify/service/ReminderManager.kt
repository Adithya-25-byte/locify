package com.example.Locify.service

import android.content.Context
import android.location.Location
import com.example.Locify.data.Reminder
import com.example.Locify.notification.AlarmManager
import com.example.Locify.notification.NotificationHelper
import com.example.Locify.repository.ReminderRepository
import com.example.Locify.utility.DateTimeUtils
import com.example.Locify.utility.LocationUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmManager: AlarmManager,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) {
    suspend fun handleLocationUpdate(currentLocation: Location) {
        val activeReminders = reminderRepository.getAllActiveReminders()
        for (reminder in activeReminders) {
            if (reminder.latitude != null && reminder.longitude != null) {
                val reminderLocation = LocationUtils.createLocation(reminder.latitude, reminder.longitude)
                val distance = currentLocation.distanceTo(reminderLocation)

                val isTimeValid = reminder.dateTime?.let {
                    DateTimeUtils.isCurrentTimeAfter(it)
                } ?: true

                val isCompleted = reminder.tasks.all { it.completed }

                if (!isCompleted && isTimeValid && distance <= reminder.radiusMeters) {
                    alarmManager.triggerAlarm(reminder)
                    notificationHelper.showPersistentNotification(reminder)
                }

                if (!isCompleted && distance > reminder.radiusMeters && reminder.awayAlertEnabled) {
                    alarmManager.triggerAwayAlarm(reminder)
                }
            }
        }
    }
}
