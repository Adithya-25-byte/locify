package com.example.Locify.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.example.Locify.broadcast.LocationBroadcastReceiver
import com.example.Locify.data.Reminder
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
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderManager: ReminderManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isFirstRun = true
    private var activeReminders = mutableMapOf<Long, Reminder>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NotificationHelper.NOTIFICATION_ID_LOCATION_SERVICE,
            notificationHelper.createLocationServiceNotification()
        )

        activeReminders.clear()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }

        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        locationClient.getLocationUpdates(10000L) // Update every 10 seconds
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                // Broadcast location update for any components that need it
                val intent = Intent(this, LocationBroadcastReceiver::class.java).apply {
                    action = "com.example.Locify.LOCATION_UPDATE"
                    putExtra("latitude", location.latitude)
                    putExtra("longitude", location.longitude)
                }
                sendBroadcast(intent)

                // Also directly check reminders
                checkLocationBasedReminders(location)

                // Update service notification with better info if needed
                if (isFirstRun) {
                    isFirstRun = false
                    // Could update the notification here if needed
                }
            }
            .launchIn(serviceScope)
    }

    private fun checkLocationBasedReminders(location: Location) {
        serviceScope.launch {
            reminderManager.checkLocationBasedReminders(location)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_STOP_SERVICE = "com.example.Locify.STOP_LOCATION_SERVICE"

        @RequiresApi(Build.VERSION_CODES.O)
        fun startService(context: Context) {
            val intent = Intent(context, LocationMonitoringService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationMonitoringService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}