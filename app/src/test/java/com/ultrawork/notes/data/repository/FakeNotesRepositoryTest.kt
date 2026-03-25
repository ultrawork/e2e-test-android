package com.ultrawork.notes.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeNotesRepositoryTest {

    @Test
    fun `getNotes returns 5 hardcoded notes`() = runTest {
        val repo = FakeNotesRepository()
        val result = repo.getNotes()

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow().size)
        assertEquals("Shopping List", result.getOrThrow()[0].title)
    }

    @Test
    fun `createNote adds a new note`() = runTest {
        val repo = FakeNotesRepository()
        val result = repo.createNote("New Note")

        assertTrue(result.isSuccess)
        assertEquals("New Note", result.getOrThrow().title)
        assertEquals("New Note", result.getOrThrow().content)

        val allNotes = repo.getNotes().getOrThrow()
        assertEquals(6, allNotes.size)
    }

    @Test
    fun `deleteNote removes a note by id`() = runTest {
        val repo = FakeNotesRepository()
        val result = repo.deleteNote("1")

        assertTrue(result.isSuccess)

        val allNotes = repo.getNotes().getOrThrow()
        assertEquals(4, allNotes.size)
        assertTrue(allNotes.none { it.id == "1" })
    }
}
