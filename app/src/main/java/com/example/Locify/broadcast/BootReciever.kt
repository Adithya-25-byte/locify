package com.example.Locify.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.Locify.service.ReminderManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                // Reschedule all alarms after device reboot
                reminderManager.rescheduleAllReminders()
            }
        }
    }
}