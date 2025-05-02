package com.example.Locify.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class LocationSearchClient @Inject constructor(
    private val context: Context
) {
    /**
     * Search for locations by query string
     * @param query Search query (address, place name, etc.)
     * @param maxResults Maximum number of results to return
     * @return List of Address objects
     */
    suspend fun searchLocations(query: String, maxResults: Int = 5): List<Address> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, maxResults) ?: emptyList()
            } catch (e: IOException) {
                emptyList()
            }
        }
    }

    /**
     * Get address from coordinates
     * @param latLng Coordinates
     * @return Address object or null if not found
     */
    suspend fun getAddressFromCoordinates(latLng: LatLng): Address? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                addresses?.firstOrNull()
            } catch (e: IOException) {
                null
            }
        }
    }

    /**
     * Get formatted address string from coordinates
     * @param latLng Coordinates
     * @return Formatted address string or coordinates as string if address not found
     */
    suspend fun getFormattedAddress(latLng: LatLng): String {
        val address = getAddressFromCoordinates(latLng)
        return if (address != null) {
            buildString {
                address.thoroughfare?.let { append(it) }

                if (address.thoroughfare != null && address.subThoroughfare != null) {
                    append(" ")
                }

                address.subThoroughfare?.let { append(it) }

                if ((address.thoroughfare != null || address.subThoroughfare != null) &&
                    (address.locality != null || address.subAdminArea != null)) {
                    append(", ")
                }

                address.locality?.let { append(it) }

                if (address.locality == null && address.subAdminArea != null) {
                    append(address.subAdminArea)
                }

                if ((address.locality != null || address.subAdminArea != null) && address.adminArea != null) {
                    append(", ")
                }

                address.adminArea?.let { append(it) }
            }
        } else {
            "${latLng.latitude}, ${latLng.longitude}"
        }
    }

    /**
     * Suggest nearby places based on current location
     * @param currentLocation Current location coordinates
     * @param radius Search radius in meters
     * @param maxResults Maximum number of results
     * @return List of nearby places as Address objects
     */
    suspend fun suggestNearbyPlaces(currentLocation: LatLng, radius: Double = 1000.0, maxResults: Int = 5): List<String> {
        // This would typically use the Places API, but for now we'll return a simple list
        // In a real implementation, you'd connect to Google Places API or similar service
        return listOf(
            "Nearby cafe",
            "Local supermarket",
            "Nearest restaurant",
            "Park",
            "Gas station"
        )
    }
}