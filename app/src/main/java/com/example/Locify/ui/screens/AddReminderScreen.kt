package com.example.Locify.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.Locify.viewmodels.AddReminderViewModel
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: AddReminderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Check if location permissions are granted
    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // State for form fields
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var radius by remember { mutableStateOf(100) }
    var useTimeBasedTrigger by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    // State for date/time picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Format for displaying the selected date/time
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    // Remember validation state
    var isFormValid by remember { mutableStateOf(false) }

    // Check form validity whenever relevant fields change
    LaunchedEffect(title, message, selectedLocation, useTimeBasedTrigger, selectedDateTime) {
        isFormValid = title.isNotBlank() && message.isNotBlank() &&
                (selectedLocation != null || (useTimeBasedTrigger && selectedDateTime != null))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Enter reminder title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Message field
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                placeholder = { Text("Enter reminder details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Divider for location section
            Divider()

            // Location section
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            // Map for location selection
            if (hasLocationPermission.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    ReminderMapScreen(
                        onLocationSelected = { latLng ->
                            selectedLocation = latLng
                        }
                    )

                    // Show selected location marker or instructions
                    selectedLocation?.let {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Lat: ${it.latitude.format(6)}, Lng: ${it.longitude.format(6)}",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } ?: run {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Tap on map to select location",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Radius slider
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Trigger radius: $radius meters",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = radius.toFloat(),
                        onValueChange = { radius = it.toInt() },
                        valueRange = 50f..500f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Message when location permission is not granted
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Location permission required to set location-based reminders",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Divider()

            // Time-based trigger section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time-based Trigger",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = useTimeBasedTrigger,
                    onCheckedChange = { useTimeBasedTrigger = it }
                )
            }

            if (useTimeBasedTrigger) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        selectedDateTime?.format(dateTimeFormatter) ?: "Select Date & Time"
                    )
                }

                // We would need to implement the actual date/time picker dialogs here
                // This is a simplified example - you would need to implement this
                // based on your specific UI requirements

                if (showDatePicker) {
                    // This is where you would show a date/time picker
                    // For simplicity, we'll just set a default future time
                    LaunchedEffect(Unit) {
                        selectedDateTime = LocalDateTime.now().plusHours(1)
                        showDatePicker = false
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.saveReminder(
                        title = title,
                        message = message,
                        latitude = selectedLocation?.latitude ?: 0.0,
                        longitude = selectedLocation?.longitude ?: 0.0,
                        radius = radius.toDouble(),
                        triggerTime = if (useTimeBasedTrigger) selectedDateTime else null
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Save Reminder")
            }

            // Cancel button
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Extension function to format Double to specified decimal places
fun Double.format(digits: Int) = "%.${digits}f".format(this)

// In a real app, you would implement proper date/time pickers
// This is just a placeholder for the complete implementation
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimePicker(
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    // Implement your date/time picker here
    // For Android, you might use the MaterialDatePicker and TimePickerDialog
    // This would require platform-specific code
}