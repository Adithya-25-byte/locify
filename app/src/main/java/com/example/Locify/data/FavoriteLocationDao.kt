package com.example.Locify.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteLocation(location: FavoriteLocation): Long

    @Update
    suspend fun updateFavoriteLocation(location: FavoriteLocation)

    @Delete
    suspend fun deleteFavoriteLocation(location: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations ORDER BY name ASC")
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations ORDER BY usageCount DESC LIMIT :limit")
    fun getMostUsedLocations(limit: Int): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations WHERE id = :id")
    suspend fun getFavoriteLocationById(id: Long): FavoriteLocation?

    @Query("UPDATE favorite_locations SET usageCount = usageCount + 1 WHERE id = :locationId")
    suspend fun incrementUsageCount(locationId: Long)

    @Query("SELECT * FROM favorite_locations WHERE " +
            "name LIKE '%' || :query || '%' OR " +
            "address LIKE '%' || :query || '%' " +
            "ORDER BY usageCount DESC LIMIT 10")
    suspend fun searchFavoriteLocations(query: String): List<FavoriteLocation>
}