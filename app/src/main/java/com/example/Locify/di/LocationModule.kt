package com.example.Locify.di

import android.content.Context
import com.example.Locify.location.DefaultLocationClient
import com.example.Locify.location.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context): LocationClient {
        return DefaultLocationClient(context)
    }
}