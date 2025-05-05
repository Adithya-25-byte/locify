package com.example.Locify.ui.components

import android.app.Dialog
import android.content.Context
import android.view.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*

@Composable
fun MapFullScreenDialog(
    context: Context,
    onLocationSelected: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    var markerPosition by remember { mutableStateOf(LatLng(12.9716, 77.5946)) } // Default to Bangalore

    val dialog = remember {
        Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(ComposeView(context).apply {
                setContent {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            TextField(
                                value = "",
                                onValueChange = {},
                                label = { Text("Search location...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            AndroidView(factory = { ctx ->
                                MapView(ctx).apply {
                                    onCreate(null)
                                    getMapAsync(object : OnMapReadyCallback {
                                        override fun onMapReady(map: GoogleMap) {
                                            map.uiSettings.isZoomControlsEnabled = true
                                            val marker = map.addMarker(
                                                MarkerOptions().position(markerPosition).title("Selected Location")
                                            )
                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15f))

                                            map.setOnMapClickListener { latLng ->
                                                marker?.position = latLng
                                                markerPosition = latLng
                                            }
                                        }
                                    })
                                    onResume()
                                }
                            }, modifier = Modifier.weight(1f))

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = {
                                    onDismiss()
                                    dialog.dismiss()
                                }) {
                                    Text("Cancel")
                                }
                                Button(onClick = {
                                    onLocationSelected(markerPosition)
                                    dialog.dismiss()
                                }) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        dialog.show()
        onDispose { dialog.dismiss() }
    }
}
