package com.example.Locify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapFullScreenDialog(
    initialLocation: LatLng,
    onLocationSelected: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 15f)
    }
    var selectedLocation by remember { mutableStateOf(initialLocation) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar at the top
                MapSearchBar(
                    query = searchQuery,
                    onQueryChange = { query ->
                        searchQuery = query
                        // Implementation for search functionality will be added
                    },
                    onSearch = { query ->
                        scope.launch {
                            // Call the search service and update results
                            // searchResults = locationSearchClient.searchLocations(query)
                        }
                    },
                    onResultSelected = { result ->
                        selectedLocation = result.latLng
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(result.latLng, 15f)
                            )
                        }
                    },
                    searchResults = searchResults
                )

                // Map with pin
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                        }
                    )

                    // Center pin
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LocationPin()
                    }
                }

                // Bottom action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PurpleButton(
                        onClick = onDismiss,
                        text = "Cancel"
                    )

                    PurpleButton(
                        onClick = {
                            onLocationSelected(selectedLocation)
                            onDismiss()
                        },
                        text = "Confirm Location"
                    )
                }
            }
        }
    }
}

data class SearchResult(
    val name: String,
    val address: String,
    val latLng: LatLng
)