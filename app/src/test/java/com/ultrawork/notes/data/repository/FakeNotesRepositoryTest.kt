package com.ultrawork.notes.data.repository

import com.ultrawork.notes.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeNotesRepositoryTest {

    private lateinit var repository: FakeNotesRepository

    @Before
    fun setUp() {
        repository = FakeNotesRepository()
    }

    @Test
    fun `getNotes returns empty list initially`() = runTest {
        val notes = repository.getNotes()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun `createNote adds note and returns it with timestamps`() = runTest {
        val note = Note(id = "1", title = "Test", content = "Content")
        val created = repository.createNote(note)

        assertEquals("1", created.id)
        assertEquals("Test", created.title)
        assertNotNull(created.createdAt)
        assertNotNull(created.updatedAt)
        assertEquals(1, repository.getNotes().size)
    }

    @Test
    fun `createNote generates UUID when id is blank`() = runTest {
        val note = Note(id = "", title = "Test", content = "Content")
        val created = repository.createNote(note)

        assertTrue(created.id.isNotBlank())
    }

    @Test
    fun `getNote returns existing note`() = runTest {
        repository.createNote(Note(id = "1", title = "Test", content = "Content"))
        val found = repository.getNote("1")
        assertNotNull(found)
        assertEquals("Test", found!!.title)
    }

    @Test
    fun `getNote returns null for non-existent id`() = runTest {
        assertNull(repository.getNote("non-existent"))
    }

    @Test
    fun `updateNote replaces note and updates timestamp`() = runTest {
        repository.createNote(Note(id = "1", title = "Old", content = "Old content"))
        val updated = repository.updateNote(
            Note(id = "1", title = "New", content = "New content")
        )

        assertEquals("New", updated.title)
        assertNotNull(updated.updatedAt)
        assertEquals("New", repository.getNote("1")!!.title)
    }

    @Test(expected = NoSuchElementException::class)
    fun `updateNote throws when note not found`() = runTest {
        repository.updateNote(Note(id = "missing", title = "T", content = "C"))
    }

    @Test
    fun `deleteNote removes existing note`() = runTest {
        repository.createNote(Note(id = "1", title = "Test", content = "Content"))
        assertTrue(repository.deleteNote("1"))
        assertTrue(repository.getNotes().isEmpty())
    }

    @Test
    fun `deleteNote returns false for non-existent id`() = runTest {
        assertFalse(repository.deleteNote("non-existent"))
    }
}
