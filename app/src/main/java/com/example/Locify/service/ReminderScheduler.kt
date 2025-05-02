package com.example.Locify.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.Locify.data.Reminder
import com.example.Locify.notification.FullScreenAlarmActivity
import com.example.Locify.utility.DateTimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

class ReminderScheduler @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) {
    fun scheduleReminder(reminder: Reminder) {
        if (reminder.isTimeBased && reminder.triggerDateTime != null) {
            alarmManager.scheduleAlarm(reminder.id, reminder.triggerDateTime)
        }
    }

    fun cancelReminder(reminderId: Long) {
        alarmManager.cancelAlarm(reminderId)
    }

    fun rescheduleAllReminders(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (reminder.isTimeBased && reminder.triggerDateTime != null && !reminder.isCompleted) {
                // Only schedule future reminders
                if (reminder.triggerDateTime > System.currentTimeMillis()) {
                    scheduleReminder(reminder)
                }
            }
        }
    }
}