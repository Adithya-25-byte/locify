package com.example.Locify.repository

import com.example.Locify.data.FavoriteReminder
import com.example.Locify.data.FavoriteReminderDao
import com.example.Locify.data.Reminder
import javax.inject.Inject

class FavoritesRepository @Inject constructor(
    private val favoriteReminderDao: FavoriteReminderDao
) {
    val allFavoriteReminders = favoriteReminderDao.getAllFavoriteReminders()

    suspend fun insertFavoriteReminder(favoriteReminder: FavoriteReminder): Long {
        return favoriteReminderDao.insert(favoriteReminder)
    }

    suspend fun updateFavoriteReminder(favoriteReminder: FavoriteReminder) {
        favoriteReminderDao.update(favoriteReminder)
    }

    suspend fun deleteFavoriteReminder(favoriteReminder: FavoriteReminder) {
        favoriteReminderDao.delete(favoriteReminder)
    }

    suspend fun getFavoriteReminderById(id: Long): FavoriteReminder? {
        return favoriteReminderDao.getFavoriteReminderById(id)
    }

    suspend fun convertFavoriteToReminder(favoriteReminder: FavoriteReminder): Reminder {
        return Reminder(
            title = favoriteReminder.title,
            description = favoriteReminder.description,
            latitude = favoriteReminder.latitude ?: 0.0,
            longitude = favoriteReminder.longitude ?: 0.0,
            locationName = favoriteReminder.locationName ?: "",
            isLocationBased = favoriteReminder.isLocationBased,
            isTimeBased = favoriteReminder.isTimeBased,
            remindWhenUnlock = favoriteReminder.remindWhenUnlock
        )
    }
}