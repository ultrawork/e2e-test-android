package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.CreateNoteRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        val result = repository.getNotes()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `createNote adds note and returns it with timestamps`() = runTest {
        val result = repository.createNote(CreateNoteRequest("Test", "Content"))

        assertTrue(result.isSuccess)
        val created = result.getOrNull()!!
        assertEquals("Test", created.title)
        assertEquals("Content", created.content)
        assertNotNull(created.createdAt)
        assertNotNull(created.updatedAt)
        assertEquals(1, repository.getNotes().getOrNull()!!.size)
    }

    @Test
    fun `createNote generates UUID for id`() = runTest {
        val result = repository.createNote(CreateNoteRequest("Test", "Content"))
        assertTrue(result.getOrNull()!!.id.isNotBlank())
    }

    @Test
    fun `deleteNote removes existing note`() = runTest {
        val created = repository.createNote(CreateNoteRequest("Test", "Content")).getOrNull()!!
        val result = repository.deleteNote(created.id)
        assertTrue(result.isSuccess)
        assertTrue(repository.getNotes().getOrNull()!!.isEmpty())
    }

    @Test
    fun `deleteNote succeeds for non-existent id`() = runTest {
        val result = repository.deleteNote("non-existent")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `toggleFavorite toggles note isFavorited`() = runTest {
        val created = repository.createNote(CreateNoteRequest("Test", "Content")).getOrNull()!!
        assertFalse(created.isFavorited)

        val toggled = repository.toggleFavorite(created.id).getOrNull()!!
        assertTrue(toggled.isFavorited)

        val toggledBack = repository.toggleFavorite(created.id).getOrNull()!!
        assertFalse(toggledBack.isFavorited)
    }

    @Test
    fun `toggleFavorite returns failure for non-existent id`() = runTest {
        val result = repository.toggleFavorite("non-existent")
        assertTrue(result.isFailure)
    }
}
