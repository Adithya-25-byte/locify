package com.example.Locify.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.Locify.MainActivity
import com.example.Locify.R
import com.example.Locify.data.Reminder
import com.example.Locify.location.LocationClient
import com.example.Locify.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocationMonitoringService : Service() {

    @Inject
    lateinit var locationClient: LocationClient

    @Inject
    lateinit var repository: ReminderRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private var activeReminders: List<Reminder> = emptyList()
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val CHANNEL_ID = "location_reminder_channel"
        private const val NOTIFICATION_ID = 1
        private const val SERVICE_NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, LocationMonitoringService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Acquire a partial wake lock to keep service running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "LocationReminder::LocationMonitoringWakeLock"
        ).apply {
            acquire()
        }

        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification())

        // Start monitoring locations
        startLocationMonitoring()

        // Schedule periodic check of time-based reminders
        scheduleTimeBasedReminderCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        executor.shutdown()
        wakeLock?.release()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for location-based reminders"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createServiceNotification(): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Reminder Active")
            .setContentText("Monitoring your location for reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationMonitoring() {
        serviceScope.launch {
            // Load initial reminders
            repository.getAllActiveReminders().first().let { reminders ->
                activeReminders = reminders
            }

            // Keep track of active reminders
            repository.getAllActiveReminders().collect { reminders ->
                activeReminders = reminders
            }
        }

        serviceScope.launch {
            locationClient.locationUpdates().collect { location ->
                checkNearbyReminders(location)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleTimeBasedReminderCheck() {
        executor.scheduleAtFixedRate({
            serviceScope.launch {
                checkTimeBasedReminders()
            }
        }, 0, 1, TimeUnit.MINUTES)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkNearbyReminders(currentLocation: Location) {
        val remindersToShow = mutableListOf<Reminder>()

        for (reminder in activeReminders) {
            // Skip if this is time-based only (no location check needed)
            if (reminder.triggerTime != null &&
                (reminder.latitude == 0.0 && reminder.longitude == 0.0)) {
                continue
            }

            // Create location object for the reminder
            val reminderLocation = Location("reminder").apply {
                latitude = reminder.latitude
                longitude = reminder.longitude
            }

            // Calculate distance
            val distance = currentLocation.distanceTo(reminderLocation)

            // Check if we're within the radius
            if (distance <= reminder.radiusInMeters) {
                // Check time constraints if any
                val showReminder = if (reminder.triggerTime != null) {
                    LocalDateTime.now().isAfter(reminder.triggerTime)
                } else {
                    true
                }

                if (showReminder) {
                    remindersToShow.add(reminder)

                    // Mark as completed
                    serviceScope.launch {
                        repository.markReminderAsCompleted(reminder.id)
                    }
                }
            }
        }

        // Show notifications for triggered reminders
        remindersToShow.forEach { reminder ->
            showReminderNotification(reminder)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkTimeBasedReminders() {
        val now = LocalDateTime.now()
        val remindersToShow = mutableListOf<Reminder>()

        for (reminder in activeReminders) {
            reminder.triggerTime?.let { triggerTime ->
                if (now.isAfter(triggerTime)) {
                    // For pure time-based reminders (no location)
                    if (reminder.latitude == 0.0 && reminder.longitude == 0.0) {
                        remindersToShow.add(reminder)

                        // Mark as completed
                        serviceScope.launch {
                            repository.markReminderAsCompleted(reminder.id)
                        }
                    }
                    // For location-time reminders, we'll check them when at location
                }
            }
        }

        // Show notifications for triggered reminders
        remindersToShow.forEach { reminder ->
            showReminderNotification(reminder)
        }
    }

    private fun showReminderNotification(reminder: Reminder) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(reminder.title)
            .setContentText(reminder.message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminder.id.toInt(), notification)
    }
}
