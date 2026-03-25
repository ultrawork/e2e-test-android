package com.ultrawork.notes.data.remote

data class NoteDto(
    val id: String,
    val title: String,
    val content: String,
    val userId: String,
    val createdAt: String,
    val updatedAt: String
)

data class CreateNoteRequest(
    val title: String,
    val content: String
)

data class DevTokenResponse(
    val token: String
)
