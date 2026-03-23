package com.ultrawork.notes.data.dto

data class CreateNoteRequest(
    val title: String,
    val content: String,
    val categoryIds: List<String>? = null
)
