package com.example.Locify.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao
) {
    companion object {
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val LOCATION_SERVICE_CHANNEL_ID = "location_service_channel"
        const val FULL_SCREEN_ALARM_REQUEST_CODE = 1001
        const val MARK_COMPLETE_REQUEST_CODE = 2001
        const val OPEN_APP_REQUEST_CODE = 3001
    }

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminder channel (high priority)
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for location-based reminders"
                enableLights(true)
                enableVibration(true)
            }

            // Location service channel (low priority)
            val serviceChannel = NotificationChannel(
                LOCATION_SERVICE_CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for background location service"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun showReminderNotification(reminder: Reminder, tasks: List<Task>) {
        val notificationId = reminder.id.toInt()

        // Trigger full-screen alarm
        triggerFullScreenAlarm(reminder, tasks)

        // Create notification with checkbox
        updateReminderNotification(reminder.id, reminder.title, reminder.description, tasks)
    }

    fun updateReminderNotification(reminderId: Long, title: String, description: String, tasks: List<Task>) {
        val notificationId = reminderId.toInt()
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderId", reminderId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE + notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark complete action
        val markCompleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_MARK_COMPLETE"
            putExtra("reminderId", reminderId)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            MARK_COMPLETE_REQUEST_CODE + notificationId,
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_location_pin))
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setOngoing(true) // Cannot be swiped away
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                markCompletePendingIntent
            )
            .setColor(ContextCompat.getColor(context, R.color.purple_primary))

        // If there are multiple tasks, use inbox style
        if (tasks.size > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText("${tasks.count { it.isCompleted }}/${tasks.size} tasks completed")

            tasks.forEach { task ->
                val prefix = if (task.isCompleted) "✓ " else "○ "
                inboxStyle.addLine("$prefix ${task.description}")
            }

            builder.setStyle(inboxStyle)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun triggerFullScreenAlarm(reminder: Reminder, tasks: List<Task>) {
        val fullScreenIntent = Intent(context, FullScreenAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("reminderId", reminder.id)
            putExtra("reminderTitle", reminder.title)
            putExtra("reminderDescription", reminder.description)
            putExtra("tasks", serializeTasks(tasks))
        }

        context.startActivity(fullScreenIntent)
    }

    fun showLocationServiceNotification() {
        val notificationId = 1000 // Fixed ID for service notification

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification
        val builder = NotificationCompat.Builder(context, LOCATION_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Locify is active")
            .setContentText("Monitoring your location for reminders")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(false) // Can be swiped away
            .setColor(ContextCompat.getColor(context, R.color.purple_primary))

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun completeReminder(reminderId: Long) {
        scope.launch {
            val reminder = reminderDao.getReminderById(reminderId)
            if (reminder != null) {
                // Mark all tasks as completed
                val tasks = taskDao.getTasksForReminder(reminderId)
                tasks.forEach { task ->
                    task.isCompleted = true
                    taskDao.updateTask(task)
                }

                // Mark reminder as completed
                reminder.isCompleted = true
                reminderDao.updateReminder(reminder)

                // Cancel notification
                NotificationManagerCompat.from(context).cancel(reminderId.toInt())
            }
        }
    }

    fun serializeTasks(tasks: List<Task>): String {
        return gson.toJson(tasks)
    }

    fun deserializeTasks(json: String): List<Task> {
        val type = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}