package com.ultrawork.notes.data.dto

data class UpdateNoteRequest(
    val title: String,
    val content: String,
    val categoryIds: List<String>? = null
)
