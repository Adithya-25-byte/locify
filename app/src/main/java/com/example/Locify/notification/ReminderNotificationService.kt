package com.example.Locify.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.Locify.data.Reminder
import com.example.Locify.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderNotificationService : Service() {
    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderRepository: ReminderRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.example.Locify.SHOW_NOTIFICATION"
        const val ACTION_CANCEL_NOTIFICATION = "com.example.Locify.CANCEL_NOTIFICATION"

        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"

        const val TYPE_LOCATION = "location"
        const val TYPE_TIME = "time"
        const val TYPE_UNLOCK = "unlock"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_NOTIFICATION -> {
                val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                val notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: TYPE_LOCATION

                if (reminderId != -1L) {
                    showNotification(reminderId, notificationType)
                }
            }
            ACTION_CANCEL_NOTIFICATION -> {
                val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                val notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: TYPE_LOCATION

                if (reminderId != -1L) {
                    cancelNotification(reminderId, notificationType)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun showNotification(reminderId: Long, notificationType: String) {
        serviceScope.launch {
            val reminder = reminderRepository.getReminderById(reminderId)
            reminder?.let {
                val notificationId = when (notificationType) {
                    TYPE_LOCATION -> notificationHelper.showLocationReminderNotification(it)
                    TYPE_TIME -> notificationHelper.showTimeReminderNotification(it)
                    TYPE_UNLOCK -> notificationHelper.showUnlockReminderNotification(it)
                    else -> notificationHelper.showLocationReminderNotification(it)
                }

                // Update last notification time in the database
                reminderRepository.updateReminderLastNotified(reminderId, System.currentTimeMillis())

                // Stop service after showing notification
                stopSelf()
            }
        }
    }

    private fun cancelNotification(reminderId: Long, notificationType: String) {
        val notificationId = when (notificationType) {
            TYPE_LOCATION -> NotificationHelper.NOTIFICATION_ID_LOCATION_REMINDER + reminderId.toInt()
            TYPE_TIME -> NotificationHelper.NOTIFICATION_ID_TIME_REMINDER + reminderId.toInt()
            TYPE_UNLOCK -> NotificationHelper.NOTIFICATION_ID_UNLOCK_REMINDER + reminderId.toInt()
            else -> NotificationHelper.NOTIFICATION_ID_LOCATION_REMINDER + reminderId.toInt()
        }

        notificationHelper.cancelNotification(notificationId)

        // Stop service after canceling notification
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}