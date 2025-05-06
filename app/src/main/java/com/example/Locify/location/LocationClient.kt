package com.example.Locify.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface for location client operations
 * Defines the contract for accessing location data
 */
interface LocationClient {
    /**
     * Gets the current location as a flow
     * @return Flow of Location objects
     */
    fun getLocationUpdates(): Flow<Location>

    /**
     * Gets the last known location or null if not available
     * @return Last known Location or null
     */
    suspend fun getLastKnownLocation(): Location?

    /**
     * Checks if location services are enabled on the device
     * @return true if enabled, false otherwise
     */
    fun isLocationEnabled(): Boolean

    /**
     * Starts location updates for tracking
     * @return true if successfully started, false otherwise
     */
    fun startLocationUpdates(): Boolean

    /**
     * Stops location updates to save battery
     */
    fun stopLocationUpdates()

    /**
     * Custom exception class for location-related errors
     */
    class LocationException(message: String): Exception(message)
}