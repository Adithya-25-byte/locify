package com.example.Locify.utility

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import javax.inject.Inject
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class LocationUtils @Inject constructor() {

    /**
     * Calculate distance between two coordinates using the Haversine formula
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return Distance in meters
     */
    fun calculateDistance(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    /**
     * Calculate distance between two LatLng points
     * @param start Starting coordinates
     * @param end Ending coordinates
     * @return Distance in meters
     */
    fun calculateDistance(start: LatLng, end: LatLng): Float {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
    }

    /**
     * Check if current location is within radius of target location
     * @param currentLat Current latitude
     * @param currentLng Current longitude
     * @param targetLat Target latitude
     * @param targetLng Target longitude
     * @param radiusMeters Radius in meters
     * @return True if current location is within radius of target
     */
    fun isWithinRadius(
        currentLat: Double,
        currentLng: Double,
        targetLat: Double,
        targetLng: Double,
        radiusMeters: Float
    ): Boolean {
        val distance = calculateDistance(currentLat, currentLng, targetLat, targetLng)
        return distance <= radiusMeters
    }

    /**
     * Check if current location is within radius of target location
     * @param current Current coordinates
     * @param target Target coordinates
     * @param radiusMeters Radius in meters
     * @return True if current location is within radius of target
     */
    fun isWithinRadius(current: LatLng, target: LatLng, radiusMeters: Float): Boolean {
        return isWithinRadius(
            current.latitude,
            current.longitude,
            target.latitude,
            target.longitude,
            radiusMeters
        )
    }

    /**
     * Format latitude and longitude to a readable string
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Formatted string
     */
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }

    /**
     * Format coordinates with direction indicators (N/S, E/W)
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Formatted string with direction indicators
     */
    fun formatCoordinatesWithDirection(latitude: Double, longitude: Double): String {
        val latDirection = if (latitude >= 0) "N" else "S"
        val lngDirection = if (longitude >= 0) "E" else "W"
        return String.format("%.6f°%s, %.6f°%s",
            Math.abs(latitude), latDirection,
            Math.abs(longitude), lngDirection)
    }

    /**
     * Calculate bearing between two points
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return Bearing in degrees (0-360)
     */
    fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
        val startLatRad = toRadians(startLat)
        val startLngRad = toRadians(startLng)
        val endLatRad = toRadians(endLat)
        val endLngRad = toRadians(endLng)

        val dLng = endLngRad - startLngRad

        val y = sin(dLng) * cos(endLatRad)
        val x = cos(startLatRad) * sin(endLatRad) - sin(startLatRad) * cos(endLatRad) * cos(dLng)

        var bearing = toDegrees(Math.atan2(y, x))
        bearing = (bearing + 360) % 360

        return bearing.toFloat()
    }

    /**
     * Get cardinal direction (N, NE, E, etc.) from bearing
     * @param bearing Bearing in degrees
     * @return Cardinal direction string
     */
    fun getCardinalDirection(bearing: Float): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        return directions[(Math.round(bearing / 45) % 8).toInt()]
    }
}