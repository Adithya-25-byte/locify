package com.example.Locify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.Locify.data.FavoriteLocation
import com.example.Locify.data.FavoriteReminder
import com.example.Locify.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository
) : ViewModel() {

    private val _favoriteLocations = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favoriteLocations: StateFlow<List<FavoriteLocation>> = _favoriteLocations

    private val _favoriteReminders = MutableStateFlow<List<FavoriteReminder>>(emptyList())
    val favoriteReminders: StateFlow<List<FavoriteReminder>> = _favoriteReminders

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _favoriteLocations.value = repository.getAllFavoriteLocations()
            _favoriteReminders.value = repository.getAllFavoriteReminders()
        }
    }

    fun onLocationSelected(location: FavoriteLocation, navController: NavController) {
        // Navigate or load map with location
        navController.navigate("map_screen?lat=${location.latitude}&lng=${location.longitude}")
    }

    fun onReminderSelected(reminder: FavoriteReminder, navController: NavController) {
        // Navigate to reminder detail/edit screen
        navController.navigate("edit_reminder_screen?id=${reminder.reminderId}")
    }
}
