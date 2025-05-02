package com.example.Locify.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.example.Locify.service.ReminderManager
import com.example.Locify.utility.LocationUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        val location = intent.getParcelableExtra<Location>("location") ?: return

        // Trigger location-based reminder check
        reminderManager.handleLocationUpdate(location)
    }
}
