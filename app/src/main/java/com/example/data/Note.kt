package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val color: Int, // Color as ARGB Int
    val tag: String = "",
    val isPrivate: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
