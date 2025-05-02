package com.example.Locify.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    /**
     * Gets continuous location updates
     * @param interval The interval between updates in milliseconds
     * @return Flow of Location objects
     */
    fun getLocationUpdates(interval: Long): Flow<Location>

    /**
     * Gets the current location once
     * @return The current location or null if not available
     */
    suspend fun getCurrentLocation(): Location?

    /**
     * Stops location updates
     */
    fun stopLocationUpdates()

    class LocationException(message: String): Exception(message)
}