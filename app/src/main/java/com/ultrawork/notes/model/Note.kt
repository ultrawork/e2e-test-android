package com.ultrawork.notes.model

/**
 * Модель заметки, соответствующая API-контракту.
 */
data class Note(
    val id: String = "",
    val title: String,
    val content: String,
    val categories: List<Category> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)
