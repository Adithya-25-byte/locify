package com.example.Locify.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {

    @Insert
    suspend fun insertFavoriteLocation(favoriteLocation: FavoriteLocation): Long

    @Update
    suspend fun updateFavoriteLocation(favoriteLocation: FavoriteLocation)

    @Delete
    suspend fun deleteFavoriteLocation(favoriteLocation: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations ORDER BY creationTimestamp DESC")
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations WHERE id = :id")
    suspend fun getFavoriteLocationById(id: Long): FavoriteLocation?

    @Query("SELECT * FROM favorite_locations WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    suspend fun searchFavoriteLocations(query: String): List<FavoriteLocation>
}