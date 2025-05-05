package com.example.Locify

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {

    companion object {
        const val LOCATION_NOTIFICATION_CHANNEL_ID = "location_notification_channel"
        const val REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel"
        const val ALARM_NOTIFICATION_CHANNEL_ID = "alarm_notification_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Location tracking channel
            val locationChannel = NotificationChannel(
                LOCATION_NOTIFICATION_CHANNEL_ID,
                getString(R.string.location_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.location_channel_description)
                setShowBadge(false)
            }

            // Regular reminders channel
            val reminderChannel = NotificationChannel(
                REMINDER_NOTIFICATION_CHANNEL_ID,
                getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.reminder_channel_description)
                setShowBadge(true)
            }

            // Alarms channel (high priority, full-screen)
            val alarmChannel = NotificationChannel(
                ALARM_NOTIFICATION_CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_description)
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }

            // Register channels with system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(locationChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }
}