package com.ultrawork.notes.data.remote

import com.google.gson.annotations.SerializedName

data class NoteDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CreateNoteRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String
)

data class DevTokenResponse(
    @SerializedName("token") val token: String
)
