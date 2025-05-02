package com.example.Locify.notification

import android.app.Activity
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.Locify.R
import com.example.Locify.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenAlarmActivity : Activity() {
    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_alarm)

        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Reminder"
        val reminderDesc = intent.getStringExtra("reminder_description") ?: ""

        findViewById<TextView>(R.id.tvAlarmTitle).text = reminderTitle
        findViewById<TextView>(R.id.tvAlarmDescription).text = reminderDesc

        // Play alarm sound
        val ringtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone.play()

        findViewById<CheckBox>(R.id.cbCompleteReminder).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && reminderId != -1L) {
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    reminderRepository.markReminderAsCompleted(reminderId)
                    ringtone.stop()
                    finish()
                }
            }
        }

        findViewById<android.widget.Button>(R.id.btnDismissAlarm).setOnClickListener {
            ringtone.stop()
            finish()
        }
    }
}