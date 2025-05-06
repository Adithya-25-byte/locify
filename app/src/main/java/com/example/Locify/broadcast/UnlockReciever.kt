package com.example.Locify.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.Locify.service.UnlockReminderService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver that listens for screen unlock events
 * Triggers the UnlockReminderService to check for any unlock-based reminders
 */
@AndroidEntryPoint
class UnlockReceiver : BroadcastReceiver() {

    private val TAG = "UnlockReceiver"

    /**
     * Called when the phone is unlocked (ACTION_USER_PRESENT)
     * @param context The Context in which the receiver is running
     * @param intent The Intent being received
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            Log.d(TAG, "Device unlocked, checking for unlock reminders")

            // Start the UnlockReminderService to check for any unlock-based reminders
            val serviceIntent = Intent(context, UnlockReminderService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    companion object {
        /**
         * Register the UnlockReceiver
         * Should be called when the app starts or when the reminder service starts
         * @param context The application context
         */
        fun register(context: Context) {
            // Register the receiver in code - also should be registered in the manifest
            // This is handled within the service that manages unlock reminders
        }
    }
}