package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.Category
import com.ultrawork.notes.data.repository.fake.FakeCategoryRepository
import com.ultrawork.notes.data.repository.fake.FakeNotesRepository
import com.ultrawork.notes.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class FakeRepositoriesTest {

    private lateinit var categoryRepo: FakeCategoryRepository
    private lateinit var notesRepo: FakeNotesRepository

    @Before
    fun setUp() {
        categoryRepo = FakeCategoryRepository()
        notesRepo = FakeNotesRepository()
    }

    // --- FakeCategoryRepository ---

    @Test
    fun `getCategories returns initial categories`() = runTest {
        val result = categoryRepo.getCategories()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun `create adds a new category with generated id`() = runTest {
        val category = Category(id = "", name = "Test", color = "#000000")
        val created = categoryRepo.create(category).getOrThrow()
        assertTrue(created.id.isNotBlank())
        assertEquals("Test", created.name)

        val all = categoryRepo.getCategories().getOrThrow()
        assertEquals(3, all.size)
    }

    @Test
    fun `getCategory returns correct category`() = runTest {
        val created = categoryRepo.create(Category(id = "", name = "Find Me", color = "#123456")).getOrThrow()
        val found = categoryRepo.getCategory(created.id).getOrThrow()
        assertEquals("Find Me", found.name)
    }

    @Test
    fun `getCategory returns failure for unknown id`() = runTest {
        val result = categoryRepo.getCategory("nonexistent")
        assertTrue(result.isFailure)
    }

    @Test
    fun `update modifies existing category`() = runTest {
        val created = categoryRepo.create(Category(id = "", name = "Old", color = "#111111")).getOrThrow()
        val updated = categoryRepo.update(created.copy(name = "New")).getOrThrow()
        assertEquals("New", updated.name)

        val fetched = categoryRepo.getCategory(created.id).getOrThrow()
        assertEquals("New", fetched.name)
    }

    @Test
    fun `category update returns failure for unknown id`() = runTest {
        val result = categoryRepo.update(Category(id = "nonexistent", name = "X", color = "#000"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `delete removes category`() = runTest {
        val created = categoryRepo.create(Category(id = "", name = "Del", color = "#222222")).getOrThrow()
        val deleteResult = categoryRepo.delete(created.id)
        assertTrue(deleteResult.isSuccess)

        val getResult = categoryRepo.getCategory(created.id)
        assertTrue(getResult.isFailure)
    }

    @Test
    fun `category delete returns failure for unknown id`() = runTest {
        val result = categoryRepo.delete("nonexistent")
        assertTrue(result.isFailure)
    }

    // --- FakeNotesRepository ---

    @Test
    fun `getNotes returns initial notes`() = runTest {
        val result = notesRepo.getNotes()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun `create adds a new note with generated id`() = runTest {
        val note = Note(
            id = 0,
            title = "New",
            content = "Content",
            createdAt = Date(),
            updatedAt = Date(),
            categories = emptyList()
        )
        val created = notesRepo.create(note).getOrThrow()
        assertTrue(created.id > 0)

        val all = notesRepo.getNotes().getOrThrow()
        assertEquals(3, all.size)
    }

    @Test
    fun `getNote returns correct note`() = runTest {
        val note = notesRepo.create(
            Note(id = 0, title = "Find", content = "Me", createdAt = Date(), updatedAt = Date(), categories = emptyList())
        ).getOrThrow()
        val found = notesRepo.getNote(note.id).getOrThrow()
        assertEquals("Find", found.title)
    }

    @Test
    fun `getNote returns failure for unknown id`() = runTest {
        val result = notesRepo.getNote(9999)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getNotes filters by categoryId`() = runTest {
        val cat = Category(id = "filter-cat", name = "Filter", color = "#AABBCC")
        notesRepo.create(
            Note(id = 0, title = "With Cat", content = "C", createdAt = Date(), updatedAt = Date(), categories = listOf(cat))
        )
        notesRepo.create(
            Note(id = 0, title = "Without Cat", content = "C", createdAt = Date(), updatedAt = Date(), categories = emptyList())
        )

        val filtered = notesRepo.getNotes(categoryId = "filter-cat").getOrThrow()
        assertTrue(filtered.all { note -> note.categories.any { it.id == "filter-cat" } })
        assertEquals(1, filtered.size)
    }

    @Test
    fun `getNotes with unknown categoryId returns empty list`() = runTest {
        val filtered = notesRepo.getNotes(categoryId = "nonexistent").getOrThrow()
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `update modifies existing note`() = runTest {
        val created = notesRepo.create(
            Note(id = 0, title = "Old", content = "C", createdAt = Date(), updatedAt = Date(), categories = emptyList())
        ).getOrThrow()
        val updated = notesRepo.update(created.copy(title = "New")).getOrThrow()
        assertEquals("New", updated.title)
    }

    @Test
    fun `note update returns failure for unknown id`() = runTest {
        val result = notesRepo.update(
            Note(id = 9999, title = "X", content = "X", createdAt = Date(), updatedAt = Date(), categories = emptyList())
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `delete removes note`() = runTest {
        val created = notesRepo.create(
            Note(id = 0, title = "Del", content = "C", createdAt = Date(), updatedAt = Date(), categories = emptyList())
        ).getOrThrow()
        assertTrue(notesRepo.delete(created.id).isSuccess)
        assertTrue(notesRepo.getNote(created.id).isFailure)
    }

    @Test
    fun `note delete returns failure for unknown id`() = runTest {
        val result = notesRepo.delete(9999)
        assertTrue(result.isFailure)
    }
}
