package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Insert
    suspend fun insert(favoriteLocation: FavoriteLocation): Long

    @Update
    suspend fun update(favoriteLocation: FavoriteLocation)

    @Delete
    suspend fun delete(favoriteLocation: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations ORDER BY name ASC")
    fun getAllFavoriteLocations(): LiveData<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations WHERE name LIKE '%' || :query || '%'")
    suspend fun searchFavoriteLocations(query: String): List<FavoriteLocation>
}