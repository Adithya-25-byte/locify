package com.example.Locify.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.Locify.notification.NotificationHelper
import com.example.Locify.service.ReminderManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                reminderManager.triggerTimeBasedReminder(reminderId)
            }
        }
    }
}