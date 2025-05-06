package com.example.Locify.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.example.Locify.service.LocationMonitoringService
import com.example.Locify.service.ReminderManager
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver for handling location-related broadcasts
 * Processes location updates and triggers location-based reminders
 */
@AndroidEntryPoint
class LocationBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "LocationBroadcastReceiver"

    @Inject
    lateinit var reminderManager: ReminderManager

    /**
     * Called when a location update is received
     * @param context The Context in which the receiver is running
     * @param intent The Intent being received
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the intent contains a location result
        if (intent.action == LocationMonitoringService.ACTION_LOCATION_UPDATED) {
            Log.d(TAG, "Received location update broadcast")

            // Extract the location from the intent
            if (LocationResult.hasResult(intent)) {
                val result = LocationResult.extractResult(intent)
                result?.let {
                    val location = it.lastLocation
                    location?.let { loc ->
                        handleLocationUpdate(context, loc)
                    }
                }
            } else {
                // Try to get the location directly from the intent extras
                val location = intent.getParcelableExtra<Location>(LocationMonitoringService.EXTRA_LOCATION)
                location?.let {
                    handleLocationUpdate(context, it)
                }
            }
        } else if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // If the device has been restarted, restart the location service
            Log.d(TAG, "Device booted, starting location service")
            val serviceIntent = Intent(context, LocationMonitoringService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    /**
     * Process a location update and check for nearby reminders
     * @param context The application context
     * @param location The updated location
     */
    private fun handleLocationUpdate(context: Context, location: Location) {
        Log.d(TAG, "Handling location update: ${location.latitude}, ${location.longitude}")

        // Use ReminderManager to check if there are any reminders nearby
        reminderManager.checkLocationBasedReminders(location)
    }

    companion object {
        /**
         * Create an intent filter for this receiver
         * @return The intent filter with relevant actions
         */
        fun getIntentFilter(): android.content.IntentFilter {
            return android.content.IntentFilter().apply {
                addAction(LocationMonitoringService.ACTION_LOCATION_UPDATED)
                addAction(Intent.ACTION_BOOT_COMPLETED)
            }
        }
    }
}