package com.example.Locify.utility

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object LocationUtils {

    /**
     * Calculate distance between two points in meters
     */
    fun calculateDistance(location1: LatLng, location2: LatLng): Float {
        val loc1 = Location("").apply {
            latitude = location1.latitude
            longitude = location1.longitude
        }

        val loc2 = Location("").apply {
            latitude = location2.latitude
            longitude = location2.longitude
        }

        return loc1.distanceTo(loc2)
    }

    /**
     * Check if a location is within a specific radius of another location
     */
    fun isWithinRadius(currentLocation: LatLng, targetLocation: LatLng, radiusInMeters: Int): Boolean {
        return calculateDistance(currentLocation, targetLocation) <= radiusInMeters
    }

    /**
     * Calculate bearing between two points (degrees)
     */
    fun calculateBearing(start: LatLng, end: LatLng): Float {
        val startLat = Math.toRadians(start.latitude)
        val startLng = Math.toRadians(start.longitude)
        val endLat = Math.toRadians(end.latitude)
        val endLng = Math.toRadians(end.longitude)

        val dLng = endLng - startLng

        val y = sin(dLng) * cos(endLat)
        val x = cos(startLat) * sin(endLat) - sin(startLat) * cos(endLat) * cos(dLng)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()

        // Normalize to 0-360
        if (bearing < 0) {
            bearing += 360
        }

        return bearing
    }
}
