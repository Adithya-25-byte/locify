package com.example.Locify.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import com.example.Locify.broadcast.UnlockReceiver
import com.example.Locify.data.Reminder
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.TaskDao
import com.example.Locify.location.LocationClient
import com.example.Locify.notification.AlarmManager
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
    lateinit var locationClient: LocationClient

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var alarmManager: AlarmManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var unlockReceiver: UnlockReceiver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Register unlock receiver
        unlockReceiver = UnlockReceiver { handleUnlockEvent() }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(unlockReceiver, filter)
    }

    private fun handleUnlockEvent() {
        serviceScope.launch {
            // Get current location
            try {
                val location = locationClient.getCurrentLocation().first()

                // Get active reminders with unlock option enabled
                val reminders = reminderDao.getUnlockReminders()

                for (reminder in reminders) {
                    if (shouldShowUnlockReminder(location, reminder)) {
                        showUnlockReminder(reminder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun shouldShowUnlockReminder(location: Location, reminder: Reminder): Boolean {
        // Skip completed reminders
        if (reminder.isCompleted) return false

        // Skip reminders that haven't been triggered yet
        if (!alarmManager.isReminderActive(reminder.id)) return false

        // For time-based reminders, just check if it's active
        if (reminder.latitude == null || reminder.longitude == null) {
            return true
        }

        // For location reminders, check if user is still at the location
        val reminderLocation = Location("reminder").apply {
            latitude = reminder.latitude
            longitude = reminder.longitude
        }

        val distance = location.distanceTo(reminderLocation)
        return distance <= 100 // Within 100 meters
    }

    private fun showUnlockReminder(reminder: Reminder) {
        serviceScope.launch {
            val tasks = taskDao.getTasksForReminder(reminder.id)

            // Show the unlock reminder dialog
            val intent = Intent(this@UnlockReminderService, ReminderUnlockDialog::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("reminderId", reminder.id)
                putExtra("reminderTitle", reminder.title)
                putExtra("reminderDescription", reminder.description)
            }
            startActivity(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unlockReceiver?.let {
            unregisterReceiver(it)
        }
        serviceScope.cancel()
    }
}