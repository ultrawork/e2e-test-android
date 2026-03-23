package com.ultrawork.notes.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ultrawork.notes.data.Category

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val isFavorited: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @Ignore
    val categories: List<Category> = emptyList()
) {
    /** Secondary constructor without categories for Room. */
    constructor(
        id: String,
        title: String,
        content: String,
        isFavorited: Boolean,
        createdAt: String?,
        updatedAt: String?
    ) : this(id, title, content, isFavorited, createdAt, updatedAt, emptyList())
}
