package com.ultrawork.notes.data.remote

import com.google.gson.annotations.SerializedName

data class NoteDto(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
