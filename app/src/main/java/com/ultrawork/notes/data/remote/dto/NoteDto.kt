package com.ultrawork.notes.data.remote.dto

import com.ultrawork.notes.model.Note
import java.util.Date

data class NoteDto(
    val id: String,
    val title: String,
    val content: String,
    val userId: String,
    val createdAt: Date,
    val updatedAt: Date
)

fun NoteDto.toNote() = Note(
    id = id,
    title = title,
    content = content,
    userId = userId,
    isFavorited = false,
    createdAt = createdAt,
    updatedAt = updatedAt
)
