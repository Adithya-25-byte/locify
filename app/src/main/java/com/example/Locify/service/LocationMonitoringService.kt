package com.example.Locify.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.TaskDao
import com.example.Locify.location.LocationClient
import com.example.Locify.notification.AlarmManager
import com.example.Locify.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationMonitoringService : Service() {

    @Inject
    lateinit var locationClient: LocationClient

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmManager: AlarmManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }

        // Show foreground notification (swipeable)
        notificationHelper.showLocationServiceNotification()

        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        locationClient.getLocationUpdates(5000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                processLocationUpdate(location)
            }
            .launchIn(serviceScope)
    }

    private fun processLocationUpdate(location: Location) {
        serviceScope.launch {
            // Get all active reminders
            val reminders = reminderDao.getAllIncompleteReminders()

            for (reminder in reminders) {
                // Check if reminder should be triggered
                if (alarmManager.checkReminderTrigger(location, reminder)) {
                    alarmManager.triggerReminder(reminder)
                }

                // Check if user moved away and should be re-alerted
                if (alarmManager.checkUserMovedAwayFromReminder(location, reminder)) {
                    // Re-trigger the alarm for this reminder
                    val tasks = taskDao.getTasksForReminder(reminder.id)
                    notificationHelper.triggerFullScreenAlarm(reminder, tasks)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning = false

        // Remove the notification
        NotificationManagerCompat.from(this).cancel(1000)
    }
}