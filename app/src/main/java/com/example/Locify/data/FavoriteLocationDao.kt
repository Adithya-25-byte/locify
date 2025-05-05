package com.example.Locify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteLocation(favoriteLocation: FavoriteLocation): Long

    @Update
    suspend fun updateFavoriteLocation(favoriteLocation: FavoriteLocation)

    @Delete
    suspend fun deleteFavoriteLocation(favoriteLocation: FavoriteLocation)

    @Query("DELETE FROM favorite_locations WHERE id = :locationId")
    suspend fun deleteFavoriteLocationById(locationId: Long)

    @Query("SELECT * FROM favorite_locations WHERE id = :locationId")
    suspend fun getFavoriteLocationById(locationId: Long): FavoriteLocation?

    @Query("SELECT * FROM favorite_locations ORDER BY name ASC")
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchFavoriteLocations(searchQuery: String): Flow<List<FavoriteLocation>>
}