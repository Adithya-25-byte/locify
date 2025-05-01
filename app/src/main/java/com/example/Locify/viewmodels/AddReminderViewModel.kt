package com.example.Locify.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Locify.data.Reminder
import com.example.Locify.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    /**
     * Saves a new reminder with the provided details
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveReminder(
        title: String,
        message: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        triggerTime: LocalDateTime?
    ) {
        // Create a new Reminder object
        val reminder = Reminder(
            id = 0, // Repository will assign the actual ID
            title = title,
            message = message,
            latitude = latitude,
            longitude = longitude,
            radiusInMeters = radius,
            triggerTime = triggerTime,
            isCompleted = false,
            createdAt = LocalDateTime.now()
        )

        // Save the reminder using a coroutine
        viewModelScope.launch {
            repository.addReminder(reminder)
        }
    }
}