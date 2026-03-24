package com.ultrawork.notes.model

/**
 * Модель категории заметки, соответствующая API-контракту.
 */
data class Category(
    val id: String = "",
    val name: String,
    val color: String = "",
    val createdAt: String = ""
)
