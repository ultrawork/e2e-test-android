package com.ultrawork.notes.data

/**
 * Represents a note category with optional color and creation timestamp.
 */
data class Category(
    val id: String,
    val name: String,
    val color: String? = null,
    val createdAt: String? = null
)
