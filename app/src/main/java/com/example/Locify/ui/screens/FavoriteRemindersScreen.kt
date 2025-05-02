package com.example.Locify.ui.screens.favorites

import ads_mobile_sdk.h6
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
fun FavoriteRemindersScreen(navController: NavController, viewModel: FavoritesViewModel = hiltViewModel()) {
    val reminders by viewModel.favoriteReminders.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorite Reminders", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(reminders) { reminder ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.onReminderSelected(reminder, navController) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = reminder.title)
                        Text(text = reminder.description, style = MaterialTheme.typography.body2)
                        if (reminder.repeat != null) {
                            Text(text = "Repeats: ${reminder.repeat}", style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
    }
}
