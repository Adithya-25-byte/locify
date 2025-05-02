package com.example.Locify.ui.screens.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.Locify.viewmodels.FavoritesViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoriteLocationsScreen(navController: NavController, viewModel: FavoritesViewModel = hiltViewModel()) {
    val locations by viewModel.favoriteLocations.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorite Locations", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(locations) { location ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.onLocationSelected(location, navController) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = location.name)
                        Text(text = "Lat: ${location.latitude}, Lng: ${location.longitude}", style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}
