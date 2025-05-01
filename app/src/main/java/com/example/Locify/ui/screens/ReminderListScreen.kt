package com.example.Locify.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.Locify.data.Reminder
import com.example.Locify.viewmodels.ReminderListViewModel
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    onAddReminderClick: () -> Unit,
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val reminders by viewModel.activeReminders.collectAsState(initial = emptyList())
    val showCompleted by viewModel.showCompleted.collectAsState(initial = false)
    val completedReminders by viewModel.completedReminders.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location Reminders") },
                actions = {
                    Switch(
                        checked = showCompleted,
                        onCheckedChange = { viewModel.toggleShowCompleted() }
                    )
                    Text("Show Completed")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminderClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        if ((if (showCompleted) completedReminders else reminders).isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showCompleted) "No completed reminders" else "No active reminders",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(if (showCompleted) completedReminders else reminders) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItem(reminder: Reminder, onDelete: () -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reminder.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                reminder.triggerTime?.let {
                    Text(
                        text = "Time: ${it.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "Radius: ${reminder.radiusInMeters}m",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Delete")
            }
        }
    }
}
