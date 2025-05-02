package com.example.Locify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.Locify.data.Reminder

@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reminder.title, style = MaterialTheme.typography.h6)
            Text(text = reminder.description, style = MaterialTheme.typography.body2)
            reminder.repeat?.let {
                Text(text = "Repeats: $it", style = MaterialTheme.typography.caption)
            }
        }
    }
}
