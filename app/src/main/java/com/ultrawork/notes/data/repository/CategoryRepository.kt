package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.Category

/**
 * Контракт репозитория для работы с категориями.
 */
interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getCategory(id: String): Result<Category>
    suspend fun create(category: Category): Result<Category>
    suspend fun update(category: Category): Result<Category>
    suspend fun delete(id: String): Result<Unit>
}
