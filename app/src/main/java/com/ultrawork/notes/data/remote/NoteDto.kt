package com.ultrawork.notes.data.remote

data class NoteDto(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)
