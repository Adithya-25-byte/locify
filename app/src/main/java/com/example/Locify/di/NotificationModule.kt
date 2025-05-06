package com.example.Locify.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing notification-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    /**
     * Provides the NotificationManager system service
     * @param context The application context
     * @return The NotificationManager instance
     */
    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Provides the AlarmManager system service for scheduling alarms
     * @param context The application context
     * @return The AlarmManager instance
     */
    @Provides
    @Singleton
    fun provideAlarmManager(
        @ApplicationContext context: Context
    ): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
}