package example.Locify.repository

import com.example.Locify.location.LocationClient
import com.example.Locify.data.FavoriteLocation
import com.example.Locify.data.FavoriteLocationDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling all location-related operations
 */
@Singleton
class LocationRepository @Inject constructor(
    private val favoriteLocationDao: FavoriteLocationDao,
    private val locationClient: LocationClient
) {
    // Favorite Location Operations
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.getAllFavoriteLocations()

    fun getMostUsedLocations(limit: Int = 5): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.getMostUsedLocations(limit)

    fun getRecentlyUsedLocations(limit: Int = 5): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.getRecentlyUsedLocations(limit)

    fun getFavoriteLocationsByCategory(category: String): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.getFavoriteLocationsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = favoriteLocationDao.getAllCategories()

    fun searchFavoriteLocations(query: String): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.searchFavoriteLocations(query)

    suspend fun getFavoriteLocationById(locationId: Long): FavoriteLocation? =
        favoriteLocationDao.getFavoriteLocationById(locationId)

    suspend fun saveFavoriteLocation(favoriteLocation: FavoriteLocation): Long =
        favoriteLocationDao.insertFavoriteLocation(favoriteLocation)

    suspend fun updateFavoriteLocation(favoriteLocation: FavoriteLocation) =
        favoriteLocationDao.updateFavoriteLocation(favoriteLocation)

    suspend fun deleteFavoriteLocation(favoriteLocation: FavoriteLocation) =
        favoriteLocationDao.deleteFavoriteLocation(favoriteLocation)

    suspend fun incrementLocationUsage(locationId: Long) =
        favoriteLocationDao.incrementUsageCount(locationId, LocalDateTime.now())

    // Current Location Operations
    suspend fun getCurrentLocation() = locationClient.getCurrentLocation()

    suspend fun requestLocationUpdates(intervalMs: Long = 5000L) =
        locationClient.startLocationUpdates(intervalMs)

    fun stopLocationUpdates() = locationClient.stopLocationUpdates()

    // Location Search Operations (will be implemented with the search feature)
    suspend fun searchLocationsByName(query: String): List<LocationSearchResult> {
        // This will be implemented when we add the search feature
        // For now, return an empty list
        return emptyList()
    }

    suspend fun getLocationDetails(placeId: String): LocationDetails? {
        // This will be implemented when we add the search feature
        return null
    }
}

/**
 * Data class representing a location search result
 */
data class LocationSearchResult(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Data class representing detailed location information
 */
data class LocationDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String? = null,
    val website: String? = null,
    val types: List<String> = emptyList()
)