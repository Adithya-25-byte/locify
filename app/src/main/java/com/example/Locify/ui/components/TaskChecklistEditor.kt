package com.example.Locify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskChecklistEditor(
    tasks: List<String>,
    onTasksChanged: (List<String>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        tasks.forEachIndexed { index, task ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = task,
                    onValueChange = {
                        val updatedTasks = tasks.toMutableList()
                        updatedTasks[index] = it
                        onTasksChanged(updatedTasks)
                    },
                    label = { Text("Task ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val updatedTasks = tasks.toMutableList()
                    updatedTasks.removeAt(index)
                    onTasksChanged(updatedTasks)
                }) {
                    androidx.compose.material.Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = "",
            onValueChange = { newTask ->
                if (newTask.isNotBlank()) {
                    onTasksChanged(tasks + newTask)
                }
            },
            label = { Text("Add New Task") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
