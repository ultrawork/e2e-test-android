package com.ultrawork.notes.data.repository.impl

import com.ultrawork.notes.data.Category
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.repository.CategoryRepository

/**
 * Реализация [CategoryRepository] через Retrofit [ApiService].
 */
class CategoryRepositoryImpl(private val api: ApiService) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        api.getCategories()
    }

    override suspend fun getCategory(id: String): Result<Category> = runCatching {
        api.getCategory(id)
    }

    override suspend fun create(category: Category): Result<Category> = runCatching {
        api.createCategory(category)
    }

    override suspend fun update(category: Category): Result<Category> = runCatching {
        api.updateCategory(category.id, category)
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        val response = api.deleteCategory(id)
        if (!response.isSuccessful) {
            throw RuntimeException("Delete failed with code ${response.code()}")
        }
    }
}
