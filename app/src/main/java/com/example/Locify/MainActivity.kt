package com.example.Locify

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.Locify.service.LocationMonitoringService
import com.example.Locify.ui.screens.AddReminderScreen
import com.example.Locify.ui.screens.ReminderListScreen
import com.example.Locify.ui.theme.LocifyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Separate background location permission as it needs special handling on Android 10+
    private val foregroundPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val backgroundLocationPermission =
        Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        ""
    }

    // Request foreground permissions first
    private val foregroundLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allLocationGranted = foregroundPermissions.all { permission ->
            permissions[permission] == true
        }

        if (allLocationGranted) {
            // Request notification permission
            if (notificationPermission.isNotEmpty()) {
                notificationPermissionRequest.launch(notificationPermission)
            }

            // After foreground permissions granted, request background location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocationPermission()
            } else {
                // For older versions, start the service directly
                startLocationService()
            }
        } else {
            Toast.makeText(
                this,
                "Location permissions are needed for this app to work properly",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Handle notification permission request
    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                this,
                "Notification permission denied. You won't receive reminder alerts.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Handle background location permission request
    private val backgroundLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationService()
        } else {
            Toast.makeText(
                this,
                "Background location permission denied. Reminders will only work when app is open.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions immediately
        requestPermissions()

        setContent {
            LocifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "reminderList"
                    ) {
                        composable("reminderList") {
                            ReminderListScreen(
                                onAddReminderClick = {
                                    navController.navigate("addReminder")
                                }
                            )
                        }
                        composable("addReminder") {
                            AddReminderScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        // Check if foreground location permissions are already granted
        val foregroundPermissionsToRequest = foregroundPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (foregroundPermissionsToRequest.isNotEmpty()) {
            // Request foreground location permissions
            foregroundLocationPermissionRequest.launch(foregroundPermissionsToRequest)
        } else {
            // If we already have foreground permissions, check for notification permission
            if (notificationPermission.isNotEmpty() &&
                ContextCompat.checkSelfPermission(this, notificationPermission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionRequest.launch(notificationPermission)
            }

            // Check background permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, backgroundLocationPermission) !=
                    PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission()
                } else {
                    startLocationService()
                }
            } else {
                // For older Android versions
                startLocationService()
            }
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Toast.makeText(
                this,
                "Please grant background location access to receive location-based reminders even when the app is closed",
                Toast.LENGTH_LONG
            ).show()

            backgroundLocationPermissionRequest.launch(backgroundLocationPermission)
        }
    }

    private fun startLocationService() {
        // Only start the service if we have the necessary permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationMonitoringService.startService(this)
        }
    }
}