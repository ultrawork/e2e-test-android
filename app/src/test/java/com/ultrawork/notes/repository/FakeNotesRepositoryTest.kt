package com.ultrawork.notes.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `getNotes returns initial list`() = runTest {
        val result = repository.getNotes()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun `createNote adds note to list`() = runTest {
        val result = repository.createNote("Test", "Content")
        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrThrow().title)

        val notes = repository.getNotes().getOrThrow()
        assertEquals(3, notes.size)
    }

    @Test
    fun `deleteNote removes note from list`() = runTest {
        val notes = repository.getNotes().getOrThrow()
        val firstId = notes.first().id

        val result = repository.deleteNote(firstId)
        assertTrue(result.isSuccess)

        val remaining = repository.getNotes().getOrThrow()
        assertEquals(1, remaining.size)
    }

    @Test
    fun `toggleFavorite flips isFavorited flag`() = runTest {
        val notes = repository.getNotes().getOrThrow()
        val firstId = notes.first().id
        assertFalse(notes.first().isFavorited)

        val toggled = repository.toggleFavorite(firstId).getOrThrow()
        assertTrue(toggled.isFavorited)

        val toggledBack = repository.toggleFavorite(firstId).getOrThrow()
        assertFalse(toggledBack.isFavorited)
    }

    @Test
    fun `toggleFavorite returns failure for non-existent note`() = runTest {
        val result = repository.toggleFavorite("non-existent")
        assertTrue(result.isFailure)
    }
}
