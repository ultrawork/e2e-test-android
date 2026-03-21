package com.ultrawork.notes.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ultrawork.notes.data.Category
import java.util.Date

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    @Ignore
    val categories: List<Category> = emptyList()
) {
    /** Конструктор для Room (без categories). */
    constructor(
        id: Long,
        title: String,
        content: String,
        createdAt: Date,
        updatedAt: Date
    ) : this(id, title, content, createdAt, updatedAt, emptyList())
}
