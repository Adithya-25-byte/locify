package com.example.Locify.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteReminderDao {
    @Insert
    suspend fun insert(favoriteReminder: FavoriteReminder): Long

    @Update
    suspend fun update(favoriteReminder: FavoriteReminder)

    @Delete
    suspend fun delete(favoriteReminder: FavoriteReminder)

    @Query("SELECT * FROM favorite_reminders ORDER BY title ASC")
    fun getAllFavoriteReminders(): LiveData<List<FavoriteReminder>>

    @Query("SELECT * FROM favorite_reminders WHERE id = :id")
    suspend fun getFavoriteReminderById(id: Long): FavoriteReminder?
}
