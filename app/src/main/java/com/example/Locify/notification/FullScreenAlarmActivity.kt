package com.example.Locify.notification

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.Locify.R
import com.example.Locify.data.Reminder
import com.example.Locify.data.Task
import com.example.Locify.ui.components.ReminderCheckboxWidget
import com.example.Locify.ui.theme.LocifyTheme
import com.example.Locify.ui.theme.PurplePrimary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenAlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindowFlags()

        val reminderId = intent.getLongExtra("reminderId", -1L)
        val reminderTitle = intent.getStringExtra("reminderTitle") ?: "Reminder"
        val reminderDescription = intent.getStringExtra("reminderDescription") ?: ""
        val tasksJson = intent.getStringExtra("tasks") ?: "[]"
        val tasks = notificationHelper.deserializeTasks(tasksJson)

        startAlarm()

        setContent {
            LocifyTheme {
                AlarmScreen(
                    reminderTitle = reminderTitle,
                    reminderDescription = reminderDescription,
                    tasks = tasks,
                    onDismiss = { completeReminder ->
                        stopAlarm()
                        if (completeReminder) {
                            notificationHelper.completeReminder(reminderId)
                        } else {
                            // Just keep the notification with the checkbox
                            notificationHelper.updateReminderNotification(reminderId, reminderTitle, reminderDescription, tasks)
                        }
                        finish()
                    }
                )
            }
        }

        // Auto-dismiss after 1 minute to prevent battery drain
        lifecycleScope.launch {
            delay(60000)
            if (!isFinishing) {
                stopAlarm()
                notificationHelper.updateReminderNotification(reminderId, reminderTitle, reminderDescription, tasks)
                finish()
            }
        }
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun startAlarm() {
        // Start playing alarm sound
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound).apply {
            isLooping = true
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            start()
        }

        // Start vibration
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 500, 500, 500),
                    0
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 500, 500, 500), 0)
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}

@Composable
fun AlarmScreen(
    reminderTitle: String,
    reminderDescription: String,
    tasks: List<Task>,
    onDismiss: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val tasksState = remember { mutableStateMapOf<Long, Boolean>() }

    // Initialize task states
    LaunchedEffect(tasks) {
        tasks.forEach { task ->
            tasksState[task.id] = task.isCompleted
        }
    }

    // Check if all tasks are completed
    val allTasksCompleted = tasks.all { tasksState[it.id] == true }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = "Alarm",
                tint = PurplePrimary,
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = reminderTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = PurplePrimary,
                textAlign = TextAlign.Center
            )

            if (reminderDescription.isNotEmpty()) {
                Text(
                    text = reminderDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            // Task list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    ReminderCheckboxWidget(
                        task = task,
                        isChecked = tasksState[task.id] ?: false,
                        onCheckedChange = { isChecked ->
                            tasksState[task.id] = isChecked
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onDismiss(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dismiss")
                }

                Button(
                    onClick = { onDismiss(true) },
                    modifier = Modifier.weight(1f),
                    enabled = allTasksCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Complete")
                }
            }
        }
    }
}