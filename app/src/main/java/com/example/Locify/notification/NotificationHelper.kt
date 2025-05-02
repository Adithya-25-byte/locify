package com.example.Locify.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.Locify.MainActivity
import com.example.Locify.R
import com.example.Locify.data.Reminder
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.Task
import com.example.Locify.data.TaskDao
import com.example.Locify.notification.FullScreenAlarmActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

class NotificationHelper @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID_REMINDER = "locify_reminder_channel"
        const val CHANNEL_ID_LOCATION_SERVICE = "locify_location_service_channel"
        const val CHANNEL_ID_ALARM = "locify_alarm_channel"

        const val NOTIFICATION_ID_LOCATION_SERVICE = 1001

        const val ACTION_COMPLETE_REMINDER = "com.example.Locify.COMPLETE_REMINDER"
        const val ACTION_DISMISS_ALARM = "com.example.Locify.DISMISS_ALARM"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for location-based reminders"
                enableLights(true)
                lightColor = Color.BLUE
            }

            // Location service channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_LOCATION_SERVICE,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification for the location monitoring service"
                setShowBadge(false)
            }

            // Alarm channel
            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARM,
                "Reminder Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen alarms for reminders"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(defaultSoundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    fun createReminderNotification(reminder: Reminder, tasks: List<Task> = emptyList()): Notification {
        val pendingIntent = createPendingIntentForReminder(reminder)

        // Create complete action
        val completeIntent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            action = ACTION_COMPLETE_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt() + 100,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val taskText = if (tasks.isNotEmpty()) {
            val completedCount = tasks.count { it.isCompleted }
            "\nTasks: $completedCount/${tasks.size} completed"
        } else {
            ""
        }

        return NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminder.title)
            .setContentText("${reminder.description}$taskText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Complete",
                completePendingIntent
            )
            .build()
    }

    fun createLocationServiceNotification(): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_LOCATION_SERVICE)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Locify")
            .setContentText("Monitoring location for reminders")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun triggerFullScreenAlarm(reminder: Reminder, tasks: List<Task>) {
        val alarmIntent = Intent(context, FullScreenAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
        }

        // Wake up device if screen is off
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Locify:AlarmWakeLock"
            )
            wakeLock.acquire(10000) // 10 seconds
        }

        context.startActivity(alarmIntent)
    }

    fun showReminderNotification(reminder: Reminder, tasks: List<Task> = emptyList()) {
        val notification = createReminderNotification(reminder, tasks)
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
    }

    private fun createPendingIntentForReminder(reminder: Reminder): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        return PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}