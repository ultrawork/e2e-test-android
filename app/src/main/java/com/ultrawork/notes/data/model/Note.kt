package com.ultrawork.notes.data.model

/**
 * Domain-модель заметки, не зависящая от Room или сетевого слоя.
 */
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
