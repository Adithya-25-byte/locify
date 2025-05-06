package com.example.Locify.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for searching and geocoding locations
 * Handles location search functionality using Geocoder
 */
@Singleton
class LocationSearchClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Search for locations by query string
     * @param query The search query (e.g., "123 Main St" or "Coffee Shops")
     * @param maxResults Maximum number of results to return
     * @return List of Address objects matching the query, or empty list if none found
     */
    suspend fun searchLocations(query: String, maxResults: Int = 5): List<Address> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList<Address>()

        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            // Use the appropriate method based on API level
            return@withContext if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val addresses = mutableListOf<Address>()
                geocoder.getFromLocationName(query, maxResults) { results ->
                    addresses.addAll(results)
                }
                addresses
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, maxResults) ?: emptyList()
            }
        } catch (e: IOException) {
            return@withContext emptyList<Address>()
        }
    }

    /**
     * Get address from coordinates
     * @param latLng The latitude and longitude to reverse geocode
     * @return The first matching Address or null if not found
     */
    suspend fun getAddressFromLocation(latLng: LatLng): Address? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            // Use the appropriate method based on API level
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                var result: Address? = null
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        result = addresses[0]
                    }
                }
                result
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.firstOrNull()
            }
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Get formatted address from coordinates
     * @param latLng The latitude and longitude
     * @return Formatted address string or coordinates as string if address not found
     */
    suspend fun getFormattedAddress(latLng: LatLng): String {
        val address = getAddressFromLocation(latLng)
        return if (address != null) {
            val addressLines = mutableListOf<String>()

            // Add the street address if available
            if (!address.thoroughfare.isNullOrEmpty()) {
                val streetAddress = buildString {
                    address.subThoroughfare?.let { append(it).append(" ") }
                    append(address.thoroughfare)
                }
                addressLines.add(streetAddress)
            }

            // Add the locality, admin area and postal code if available
            val cityStateZip = buildString {
                address.locality?.let { append(it) }
                address.adminArea?.let {
                    if (isNotEmpty()) append(", ")
                    append(it)
                }
                address.postalCode?.let {
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
            }
            if (cityStateZip.isNotEmpty()) {
                addressLines.add(cityStateZip)
            }

            // Return the formatted address or the coordinates if no address components are available
            if (addressLines.isNotEmpty()) {
                addressLines.joinToString(", ")
            } else {
                "${String.format("%.6f", latLng.latitude)}, ${String.format("%.6f", latLng.longitude)}"
            }
        } else {
            // Return coordinates as string if address not found
            "${String.format("%.6f", latLng.latitude)}, ${String.format("%.6f", latLng.longitude)}"
        }
    }
}