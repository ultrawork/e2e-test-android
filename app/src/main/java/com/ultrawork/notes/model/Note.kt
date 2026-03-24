package com.ultrawork.notes.model

/**
 * Модель заметки, соответствующая контракту backend API.
 */
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String = "",
    val updatedAt: String = "",
    val categories: List<Category> = emptyList(),
    val userId: String? = null,
    val isFavorited: Boolean = false
)
