package com.example.Locify.repository

import com.example.Locify.data.FavoriteLocation
import com.example.Locify.data.FavoriteLocationDao
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val favoriteLocationDao: FavoriteLocationDao
) {
    val allFavoriteLocations = favoriteLocationDao.getAllFavoriteLocations()

    suspend fun insertFavoriteLocation(favoriteLocation: FavoriteLocation): Long {
        return favoriteLocationDao.insert(favoriteLocation)
    }

    suspend fun updateFavoriteLocation(favoriteLocation: FavoriteLocation) {
        favoriteLocationDao.update(favoriteLocation)
    }

    suspend fun deleteFavoriteLocation(favoriteLocation: FavoriteLocation) {
        favoriteLocationDao.delete(favoriteLocation)
    }

    suspend fun searchFavoriteLocations(query: String): List<FavoriteLocation> {
        return favoriteLocationDao.searchFavoriteLocations(query)
    }
}