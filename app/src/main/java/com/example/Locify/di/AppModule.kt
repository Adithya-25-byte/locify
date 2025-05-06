package com.example.Locify.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.Locify.data.AppDatabase
import com.example.Locify.data.ReminderDao
import com.example.Locify.data.TaskDao
import com.example.Locify.data.FavoriteLocationDao
import com.example.Locify.data.FavoriteReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the application database instance
     * @param app The application context
     * @return The Room database instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "locify_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the ReminderDao for data access
     * @param db The app database instance
     * @return The ReminderDao implementation
     */
    @Provides
    @Singleton
    fun provideReminderDao(db: AppDatabase): ReminderDao {
        return db.reminderDao()
    }

    /**
     * Provides the TaskDao for data access
     * @param db The app database instance
     * @return The TaskDao implementation
     */
    @Provides
    @Singleton
    fun provideTaskDao(db: AppDatabase): TaskDao {
        return db.taskDao()
    }

    /**
     * Provides the FavoriteLocationDao for data access
     * @param db The app database instance
     * @return The FavoriteLocationDao implementation
     */
    @Provides
    @Singleton
    fun provideFavoriteLocationDao(db: AppDatabase): FavoriteLocationDao {
        return db.favoriteLocationDao()
    }

    /**
     * Provides the FavoriteReminderDao for data access
     * @param db The app database instance
     * @return The FavoriteReminderDao implementation
     */
    @Provides
    @Singleton
    fun provideFavoriteReminderDao(db: AppDatabase): FavoriteReminderDao {
        return db.favoriteReminderDao()
    }

    /**
     * Provides application context
     * @param application The application instance
     * @return The application context
     */
    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }
}