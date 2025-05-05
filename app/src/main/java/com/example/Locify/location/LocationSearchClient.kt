package com.example.Locify.location

import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

class LocationSearchClient(private val placesClient: PlacesClient) {

    suspend fun searchPlaces(query: String): List<PlaceSearchResult> {
        return try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountry("IN") // restrict to India; remove if not needed
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()

            response.autocompletePredictions.mapNotNull { prediction ->
                prediction.placeId?.let { placeId ->
                    PlaceSearchResult(name = prediction.getFullText(null).toString(), latLng = LatLng(0.0, 0.0)) // Actual latLng fetching deferred
                }
            }
        } catch (e: Exception) {
            Log.e("LocationSearchClient", "Error searching places", e)
            emptyList()
        }
    }
}

// Later: You may want to fetch exact LatLng using Place ID and a separate Places API call

data class PlaceSearchResult(
    val name: String,
    val latLng: LatLng
)

// To initialize Places in Application.kt
// Places.initialize(context, "YOUR_API_KEY") // <- Replace YOUR_API_KEY
// val placesClient = Places.createClient(context)
