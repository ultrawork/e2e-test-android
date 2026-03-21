package com.ultrawork.notes.data.repository.fake

import com.ultrawork.notes.data.Category
import com.ultrawork.notes.data.repository.CategoryRepository
import java.util.UUID

/**
 * In-memory реализация [CategoryRepository] для работы без бэкенда.
 */
class FakeCategoryRepository : CategoryRepository {

    private val categories = mutableMapOf<String, Category>()

    init {
        val work = Category(id = UUID.randomUUID().toString(), name = "Work", color = "#FF5733")
        val personal = Category(id = UUID.randomUUID().toString(), name = "Personal", color = "#33FF57")
        categories[work.id] = work
        categories[personal.id] = personal
    }

    override suspend fun getCategories(): Result<List<Category>> =
        Result.success(categories.values.toList())

    override suspend fun getCategory(id: String): Result<Category> {
        val category = categories[id]
            ?: return Result.failure(NoSuchElementException("Category not found: $id"))
        return Result.success(category)
    }

    override suspend fun create(category: Category): Result<Category> {
        val newCategory = category.copy(id = UUID.randomUUID().toString())
        categories[newCategory.id] = newCategory
        return Result.success(newCategory)
    }

    override suspend fun update(category: Category): Result<Category> {
        if (!categories.containsKey(category.id)) {
            return Result.failure(NoSuchElementException("Category not found: ${category.id}"))
        }
        categories[category.id] = category
        return Result.success(category)
    }

    override suspend fun delete(id: String): Result<Unit> {
        if (!categories.containsKey(id)) {
            return Result.failure(NoSuchElementException("Category not found: $id"))
        }
        categories.remove(id)
        return Result.success(Unit)
    }
}
