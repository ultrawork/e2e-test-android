package com.ultrawork.notes.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
