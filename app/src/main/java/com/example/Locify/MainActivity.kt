package com.example.Locify

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.Locify.ui.screens.AddReminderScreen
import com.example.Locify.ui.screens.EditReminderScreen
import com.example.Locify.ui.screens.FavoritesScreen
import com.example.Locify.ui.screens.PermissionRequestScreen
import com.example.Locify.ui.screens.ReminderListScreen
import com.example.Locify.ui.screens.ReminderMapScreen
import com.example.Locify.ui.theme.LocifyTheme
import com.example.Locify.utility.PermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionHandler: PermissionHandler

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocifyTheme {
                val navController = rememberNavController()
                var showFab by remember { mutableStateOf(true) }

                // Check if permissions are granted
                val arePermissionsGranted = permissionHandler.areLocationPermissionsGranted()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            if (arePermissionsGranted) {
                                BottomNavigation(navController)
                            }
                        },
                        floatingActionButton = {
                            if (showFab && arePermissionsGranted) {
                                FloatingActionButton(
                                    onClick = { navController.navigate(Screen.AddReminder.route) },
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.add_reminder)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = if (arePermissionsGranted) Screen.ReminderList.route else Screen.PermissionRequest.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.PermissionRequest.route) {
                                showFab = false
                                PermissionRequestScreen(
                                    permissionHandler = permissionHandler,
                                    onPermissionsGranted = {
                                        navController.navigate(Screen.ReminderList.route) {
                                            popUpTo(Screen.PermissionRequest.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Screen.ReminderList.route) {
                                showFab = true
                                ReminderListScreen(navController)
                            }

                            composable(Screen.ReminderMap.route) {
                                showFab = true
                                ReminderMapScreen(navController)
                            }

                            composable(Screen.Favorites.route) {
                                showFab = true
                                FavoritesScreen(navController)
                            }

                            composable("${Screen.AddReminder.route}?lat={lat}&lng={lng}") { backStackEntry ->
                                showFab = false
                                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                                val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
                                AddReminderScreen(
                                    navController = navController,
                                    initialLatitude = lat,
                                    initialLongitude = lng
                                )
                            }

                            composable("${Screen.EditReminder.route}/{reminderId}") { backStackEntry ->
                                showFab = false
                                val reminderId = backStackEntry.arguments?.getString("reminderId")?.toLongOrNull() ?: -1L
                                EditReminderScreen(
                                    navController = navController,
                                    reminderId = reminderId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    val items = listOf(
        Screen.ReminderList,
        Screen.ReminderMap,
        Screen.Favorites
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.resourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String, val resourceId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object ReminderList : Screen("reminder_list", R.string.reminder_list, Icons.Filled.List)
    object ReminderMap : Screen("reminder_map", R.string.reminder_map, Icons.Filled.Place)
    object Favorites : Screen("favorites", R.string.favorites, Icons.Filled.Favorite)
    object AddReminder : Screen("add_reminder", R.string.add_reminder, Icons.Filled.Add)
    object EditReminder : Screen("edit_reminder", R.string.edit_reminder, Icons.Filled.Add)
    object PermissionRequest : Screen("permission_request", R.string.permission_request, Icons.Filled.List)
}