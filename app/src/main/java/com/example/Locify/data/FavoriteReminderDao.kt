package com.example.Locify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteReminder(favoriteReminder: FavoriteReminder): Long

    @Update
    suspend fun updateFavoriteReminder(favoriteReminder: FavoriteReminder)

    @Delete
    suspend fun deleteFavoriteReminder(favoriteReminder: FavoriteReminder)

    @Query("DELETE FROM favorite_reminders WHERE id = :reminderId")
    suspend fun deleteFavoriteReminderById(reminderId: Long)

    @Query("SELECT * FROM favorite_reminders WHERE id = :reminderId")
    suspend fun getFavoriteReminderById(reminderId: Long): FavoriteReminder?

    @Query("SELECT * FROM favorite_reminders ORDER BY title ASC")
    fun getAllFavoriteReminders(): Flow<List<FavoriteReminder>>

    @Query("SELECT * FROM favorite_reminders WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title ASC")
    fun searchFavoriteReminders(searchQuery: String): Flow<List<FavoriteReminder>>
}