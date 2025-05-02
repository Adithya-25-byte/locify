package com.example.Locify.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteReminderDao {

    @Insert
    suspend fun insertFavoriteReminder(favoriteReminder: FavoriteReminder): Long

    @Update
    suspend fun updateFavoriteReminder(favoriteReminder: FavoriteReminder)

    @Delete
    suspend fun deleteFavoriteReminder(favoriteReminder: FavoriteReminder)

    @Query("SELECT * FROM favorite_reminders ORDER BY creationTimestamp DESC")
    fun getAllFavoriteReminders(): Flow<List<FavoriteReminder>>

    @Query("SELECT * FROM favorite_reminders WHERE id = :id")
    suspend fun getFavoriteReminderById(id: Long): FavoriteReminder?

    @Query("SELECT * FROM favorite_reminders WHERE title LIKE '%' || :query || '%'")
    suspend fun searchFavoriteReminders(query: String): List<FavoriteReminder>

    @Transaction
    @Query("SELECT * FROM favorite_reminders WHERE id = :favoriteReminderId")
    suspend fun getFavoriteReminderWithTasks(favoriteReminderId: Long): FavoriteReminderWithTasks?
}