package com.ultrawork.notes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val isFavorited: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
