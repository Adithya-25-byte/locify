package com.example.Locify.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import com.example.Locify.MainActivity
import com.example.Locify.broadcast.UnlockReceiver
import com.example.Locify.data.Reminder
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.TaskDao
import com.example.Locify.location.LocationClient
import com.example.Locify.notification.AlarmManager
import com.example.Locify.repository.ReminderRepository
import com.example.Locify.ui.components.ReminderUnlockDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnlockReminderService : Service() {
    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var locationClient: LocationClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "SHOW_UNLOCK_DIALOG") {
            val reminderId = intent.getLongExtra("reminder_id", -1L)
            if (reminderId != -1L) {
                serviceScope.launch {
                    showUnlockReminder(reminderId)
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun showUnlockReminder(reminderId: Long) {
        val reminder = reminderRepository.getReminderById(reminderId) ?: return
        val tasks = reminderRepository.getIncompleteTasksForReminder(reminderId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("SHOW_UNLOCK_DIALOG", true)
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
        }

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
