package com.example.Locify.notification

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.Locify.data.Reminder
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.TaskDao
import com.example.Locify.utility.LocationUtils
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao,
    private val notificationHelper: NotificationHelper,
    private val locationUtils: LocationUtils
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Track if user has moved away from reminder location
    private val userMovedAway = ConcurrentHashMap<Long, Boolean>()

    // Track active reminders that have triggered already
    private val activeReminders = ConcurrentHashMap<Long, Boolean>()

    // Specify the return distance in meters to retrigger alarm
    private val RETURN_DISTANCE = 200.0

    // Specifies the radius in meters to consider a user at a location
    private val LOCATION_RADIUS = 50.0

    /**
     * Check if a reminder should be triggered based on location and time
     */
    fun checkReminderTrigger(currentLocation: Location, reminder: Reminder): Boolean {
        // Skip completed reminders
        if (reminder.isCompleted) return false

        // If already active, don't retrigger
        if (activeReminders[reminder.id] == true) return false

        val isAtLocation = isAtReminderLocation(currentLocation, reminder)

        // Handle time-based conditions
        val shouldTriggerTime = if (reminder.dateTime != null) {
            val now = LocalDateTime.now()
            now.isAfter(reminder.dateTime) || now.isEqual(reminder.dateTime)
        } else {
            true
        }

        // Handle location-based conditions
        val shouldTriggerLocation = if (reminder.latitude != null && reminder.longitude != null) {
            isAtLocation
        } else {
            true
        }

        return shouldTriggerTime && shouldTriggerLocation
    }

    /**
     * Trigger the reminder and show notification with alarm
     */
    fun triggerReminder(reminder: Reminder) {
        scope.launch {
            // Mark as active
            activeReminders[reminder.id] = true

            // Reset moved away flag
            userMovedAway[reminder.id] = false

            // Fetch tasks for this reminder
            val tasks = taskDao.getTasksForReminder(reminder.id)

            // Show notification and trigger alarm
            withContext(Dispatchers.Main) {
                notificationHelper.showReminderNotification(reminder, tasks)
            }
        }
    }

    /**
     * Check if the user has moved away from a reminder location
     * Returns true if user has moved away and should trigger a re-alert
     */
    fun checkUserMovedAwayFromReminder(
        currentLocation: Location,
        reminder: Reminder
    ): Boolean {
        // Only check non-completed reminders that have been triggered
        if (reminder.isCompleted || activeReminders[reminder.id] != true) {
            return false
        }

        // Skip reminders without location
        if (reminder.latitude == null || reminder.longitude == null) {
            return false
        }

        val isAtLocation = isAtReminderLocation(currentLocation, reminder)

        // If user was at location and now is not, mark as moved away
        if (!isAtLocation && userMovedAway[reminder.id] != true) {
            userMovedAway[reminder.id] = true
            return false
        }

        // If user moved away and now is far enough, trigger re-alert
        if (userMovedAway[reminder.id] == true) {
            val reminderLocation = Location("reminder").apply {
                latitude = reminder.latitude
                longitude = reminder.longitude
            }

            val distance = currentLocation.distanceTo(reminderLocation)

            // If user has moved far enough away, reset the moved away flag and re-alert
            if (distance > RETURN_DISTANCE) {
                userMovedAway[reminder.id] = false
                return true
            }
        }

        return false
    }

    /**
     * Reset the alarm state when a reminder is completed
     */
    fun resetReminder(reminderId: Long) {
        activeReminders.remove(reminderId)
        userMovedAway.remove(reminderId)
    }

    /**
     * Check if the user is at the reminder location
     */
    private fun isAtReminderLocation(currentLocation: Location, reminder: Reminder): Boolean {
        if (reminder.latitude == null || reminder.longitude == null) {
            return false
        }

        val reminderLocation = Location("reminder").apply {
            latitude = reminder.latitude
            longitude = reminder.longitude
        }

        val distance = currentLocation.distanceTo(reminderLocation)
        return distance <= LOCATION_RADIUS
    }

    /**
     * Schedule a time-based reminder to trigger at the specified time
     */
    fun scheduleTimeBasedReminder(reminder: Reminder) {
        // For time-based reminders, we'll use the ReminderScheduler service
        // Which will be implemented separately
    }

    /**
     * Check if the reminder has triggered already
     */
    fun isReminderActive(reminderId: Long): Boolean {
        return activeReminders[reminderId] == true
    }
}