package com.ultrawork.notes.data

/**
 * Модель категории заметки.
 *
 * @property id уникальный идентификатор категории
 * @property name название категории
 * @property color цвет в формате hex (напр. #FF5733)
 */
data class Category(
    val id: String,
    val name: String,
    val color: String
)
