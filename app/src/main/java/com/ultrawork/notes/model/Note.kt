package com.ultrawork.notes.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val categories: List<Category> = emptyList()
)
