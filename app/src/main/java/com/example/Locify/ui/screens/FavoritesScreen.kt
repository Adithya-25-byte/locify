package com.example.Locify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.Locify.ui.screens.favorites.FavoriteLocationsScreen
import com.example.Locify.ui.screens.favorites.FavoriteRemindersScreen

@Composable
fun FavoritesScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Locations", "Reminders")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> FavoriteLocationsScreen(navController)
            1 -> FavoriteRemindersScreen(navController)
        }
    }
}
