package com.example.Locify.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.example.Locify.utility.LocationUtils
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class DefaultLocationClient @Inject constructor(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        if (!LocationUtils.hasLocationPermissions(context)) {
            throw LocationClient.LocationException("Missing location permissions")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            throw LocationClient.LocationException("GPS is disabled")
        }

        val request = LocationRequest.create()
            .setInterval(interval)
            .setFastestInterval(interval)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    trySend(location)
                }
            }
        }

        client.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        if (!LocationUtils.hasLocationPermissions(context)) {
            throw LocationClient.LocationException("Missing location permissions")
        }

        return suspendCancellableCoroutine { cont ->
            client.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    cont.resume(task.result)
                } else {
                    // If last location is null, request a new location
                    val request = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(1)
                        .setExpirationDuration(10000)

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            locationResult.lastLocation?.let { location ->
                                cont.resume(location)
                                client.removeLocationUpdates(this)
                            }
                        }
                    }

                    client.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    cont.invokeOnCancellation {
                        client.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }
    }

    override fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    companion object {
        class LocationException(message: String) : Exception(message)
    }
}