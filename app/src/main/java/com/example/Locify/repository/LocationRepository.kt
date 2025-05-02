package com.example.Locify.repository

import android.location.Address
import android.location.Geocoder
import com.example.Locify.data.FavoriteLocation
import com.example.Locify.location.LocationClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationClient: LocationClient,
    private val geocoder: Geocoder,
    private val favoritesRepository: FavoritesRepository
) {
    // Get current location
    fun getCurrentLocation() = locationClient.getLocationUpdates(100L)

    suspend fun getLastKnownLocation() = locationClient.getCurrentLocation()

    // Geocoding operations
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Address? {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLocationFromAddress(address: String): Address? {
        return try {
            val addresses = geocoder.getFromLocationName(address, 1)
            addresses?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // Combine with favorites for search
    suspend fun searchLocations(query: String): List<LocationSearchResult> {
        val results = mutableListOf<LocationSearchResult>()

        // First add favorite locations matching the query
        val favoriteResults = favoritesRepository.searchFavoriteLocations(query)
        results.addAll(favoriteResults.map {
            LocationSearchResult(
                name = it.name,
                address = it.address,
                latitude = it.latitude,
                longitude = it.longitude,
                isFavorite = true,
                favoriteId = it.id
            )
        })

        // Then add geocoded results if not too many favorites
        if (results.size < 5) {
            try {
                val addresses = geocoder.getFromLocationName(query, 5 - results.size)
                addresses?.forEach { address ->
                    // Check if this address is already in results (from favorites)
                    val lat = address.latitude
                    val lng = address.longitude
                    val exists = results.any {
                        Math.abs(it.latitude - lat) < 0.0001 &&
                                Math.abs(it.longitude - lng) < 0.0001
                    }

                    if (!exists) {
                        results.add(
                            LocationSearchResult(
                                name = address.featureName ?: "",
                                address = address.getAddressLine(0) ?: "",
                                latitude = lat,
                                longitude = lng,
                                isFavorite = false,
                                favoriteId = null
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Geocoding failed, just continue with what we have
            }
        }

        return results
    }

    // Helper function to check if location is within certain radius
    fun isWithinRadius(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        radiusInMeters: Float
    ): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] <= radiusInMeters
    }
}

// Model for location search results
data class LocationSearchResult(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean,
    val favoriteId: Long? = null
)