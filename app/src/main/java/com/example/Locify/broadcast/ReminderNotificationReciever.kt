package com.example.Locify.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.Locify.notification.NotificationHelper
import com.example.Locify.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderNotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACTION_COMPLETE_REMINDER -> {
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
                if (reminderId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        reminderRepository.markReminderAsCompleted(reminderId)
                    }
                }
            }
        }
    }
}