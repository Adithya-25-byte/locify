package com.example.Locify.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLastLocation(): Location?
    fun locationUpdates(): Flow<Location>
}