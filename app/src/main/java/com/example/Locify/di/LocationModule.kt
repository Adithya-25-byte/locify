package com.example.Locify.di

import android.content.Context
import com.example.Locify.location.DefaultLocationClient
import com.example.Locify.location.LocationClient
import com.example.Locify.location.LocationSearchClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing location-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    /**
     * Binds the DefaultLocationClient as the implementation of LocationClient
     * @param defaultLocationClient The default implementation
     * @return The LocationClient interface instance
     */
    @Binds
    @Singleton
    abstract fun bindLocationClient(defaultLocationClient: DefaultLocationClient): LocationClient

    companion object {
        /**
         * Provides the FusedLocationProviderClient for location services
         * @param context The application context
         * @return The FusedLocationProviderClient instance
         */
        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
        ): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(context)
        }

        /**
         * Provides the LocationSearchClient for location search functionality
         * @param context The application context
         * @return The LocationSearchClient instance
         */
        @Provides
        @Singleton
        fun provideLocationSearchClient(
            @ApplicationContext context: Context
        ): LocationSearchClient {
            return LocationSearchClient(context)
        }
    }
}