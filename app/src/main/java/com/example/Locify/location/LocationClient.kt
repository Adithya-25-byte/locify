package com.example.Locify.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>
    suspend fun getCurrentLocation(): Location?
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float
}