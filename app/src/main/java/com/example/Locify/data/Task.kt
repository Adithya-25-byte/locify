package com.example.Locify.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity representing a task within a reminder.
 * Each reminder can have multiple tasks (checkboxes).
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Reminder::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reminderId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Foreign key to parent reminder
    val reminderId: Long,

    // Task details
    val description: String,
    val isCompleted: Boolean = false,

    // Order within the reminder's task list
    val orderPosition: Int = 0,

    // Timestamps
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)