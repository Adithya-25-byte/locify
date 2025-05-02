package com.example.Locify.ui.components

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.TaskDao
import com.example.Locify.notification.AlarmManager
import com.example.Locify.notification.NotificationHelper
import com.example.Locify.ui.theme.LocifyTheme
import com.example.Locify.ui.theme.PurplePrimary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderUnlockDialog : Activity() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reminderId = intent.getLongExtra("reminderId", -1L)
        val reminderTitle = intent.getStringExtra("reminderTitle") ?: "Reminder"
        val reminderDescription = intent.getStringExtra("reminderDescription") ?: ""

        setContent {
            LocifyTheme {
                UnlockReminderDialog(
                    reminderId = reminderId,
                    title = reminderTitle,
                    description = reminderDescription,
                    onDismiss = { markComplete ->
                        if (markComplete) {
                            lifecycleScope.launch {
                                notificationHelper.completeReminder(reminderId)
                                alarmManager.resetReminder(reminderId)
                            }
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun UnlockReminderDialog(
    reminderId: Long,
    title: String,
    description: String,
    onDismiss: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss(false) },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PurplePrimary,
                    textAlign = TextAlign.Center
                )

                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                // Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PurplePrimary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Text(
                        text = "Mark as completed",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
                        Text("Not now")
                    }

                    Button(
                        onClick = { onDismiss(true) },
                        modifier = Modifier.weight(1f),
                        enabled = isChecked,
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
}