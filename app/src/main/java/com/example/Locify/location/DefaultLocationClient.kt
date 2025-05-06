package com.example.Locify.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of the LocationClient interface
 * Uses Google's FusedLocationProviderClient for efficient location tracking
 */
@Singleton
class DefaultLocationClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : LocationClient {

    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        // Check if location permissions are granted
        if (!hasLocationPermissions(context)) {
            throw LocationClient.LocationException("Missing location permissions")
        }

        // Check if location services are enabled
        if (!isLocationEnabled()) {
            throw LocationClient.LocationException("Location services are disabled")
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                // Send each location to the flow
                result.locations.lastOrNull()?.let { location ->
                    launch { send(location) }
                }
            }
        }

        // Start location updates
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        isTracking = true

        awaitClose {
            stopLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): Location? {
        // Check if location permissions are granted
        if (!hasLocationPermissions(context)) {
            throw LocationClient.LocationException("Missing location permissions")
        }

        // Check if location services are enabled
        if (!isLocationEnabled()) {
            throw LocationClient.LocationException("Location services are disabled")
        }

        return try {
            fusedLocationProviderClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(): Boolean {
        if (isTracking) return true

        // Check if location permissions are granted
        if (!hasLocationPermissions(context)) {
            return false
        }

        // Check if location services are enabled
        if (!isLocationEnabled()) {
            return false
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                // Handle location updates - will be used by broadcast receivers
            }
        }

        // Start location updates
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        isTracking = true
        return true
    }

    override fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
            locationCallback = null
            isTracking = false
        }
    }

    private fun hasLocationPermissions(context: Context): Boolean {
        // This should be implemented in the PermissionHandler utility class
        // For now, we return true to avoid compilation errors
        // TODO: Replace with actual permission check from PermissionHandler
        return true
    }
}