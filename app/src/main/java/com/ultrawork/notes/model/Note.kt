package com.ultrawork.notes.model

import java.util.Date

data class Note(
    val id: String = "",
    val title: String,
    val content: String,
    val userId: String? = null,
    val isFavorited: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
