package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for FavoriteLocation entities
 */
@Dao
interface FavoriteLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteLocation(favoriteLocation: FavoriteLocation): Long

    @Update
    suspend fun updateFavoriteLocation(favoriteLocation: FavoriteLocation)

    @Delete
    suspend fun deleteFavoriteLocation(favoriteLocation: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations WHERE id = :locationId")
    suspend fun getFavoriteLocationById(locationId: Long): FavoriteLocation?

    @Query("SELECT * FROM favorite_locations ORDER BY name ASC")
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations ORDER BY usageCount DESC LIMIT :limit")
    fun getMostUsedLocations(limit: Int): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations ORDER BY lastUsed DESC LIMIT :limit")
    fun getRecentlyUsedLocations(limit: Int): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations WHERE category = :category ORDER BY name ASC")
    fun getFavoriteLocationsByCategory(category: String): Flow<List<FavoriteLocation>>

    @Query("SELECT DISTINCT category FROM favorite_locations WHERE category != ''")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM favorite_locations WHERE " +
            "name LIKE '%' || :searchQuery || '%' OR " +
            "description LIKE '%' || :searchQuery || '%' OR " +
            "address LIKE '%' || :searchQuery || '%'")
    fun searchFavoriteLocations(searchQuery: String): Flow<List<FavoriteLocation>>

    @Query("UPDATE favorite_locations SET usageCount = usageCount + 1, lastUsed = :timestamp WHERE id = :locationId")
    suspend fun incrementUsageCount(locationId: Long, timestamp: LocalDateTime)
}