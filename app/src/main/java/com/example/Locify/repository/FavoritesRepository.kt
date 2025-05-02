package com.example.Locify.repository

import com.example.Locify.data.FavoriteLocation
import com.example.Locify.data.FavoriteLocationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoriteLocationDao: FavoriteLocationDao
) {
    // Favorite location operations
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.getAllFavoriteLocations()

    fun getMostUsedLocations(limit: Int): Flow<List<FavoriteLocation>> =
        favoriteLocationDao.getMostUsedLocations(limit)

    suspend fun getFavoriteLocationById(id: Long): FavoriteLocation? =
        favoriteLocationDao.getFavoriteLocationById(id)

    suspend fun insertFavoriteLocation(location: FavoriteLocation): Long =
        favoriteLocationDao.insertFavoriteLocation(location)

    suspend fun updateFavoriteLocation(location: FavoriteLocation) =
        favoriteLocationDao.updateFavoriteLocation(location)

    suspend fun deleteFavoriteLocation(location: FavoriteLocation) =
        favoriteLocationDao.deleteFavoriteLocation(location)

    suspend fun incrementLocationUsage(locationId: Long) =
        favoriteLocationDao.incrementUsageCount(locationId)

    suspend fun searchFavoriteLocations(query: String): List<FavoriteLocation> =
        favoriteLocationDao.searchFavoriteLocations(query)
}