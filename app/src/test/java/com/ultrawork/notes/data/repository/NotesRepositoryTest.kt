package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.model.Note
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NotesRepositoryTest {

    private lateinit var api: ApiService
    private lateinit var repository: NotesRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = NotesRepository(api)
    }

    @Test
    fun `getNotes returns list from API`() = runTest {
        val expected = listOf(
            Note(id = "1", title = "Test", content = "Content")
        )
        coEvery { api.fetchNotes() } returns expected

        val result = repository.getNotes()

        assertEquals(expected, result)
        coVerify { api.fetchNotes() }
    }

    @Test
    fun `getNotes returns empty list on API failure`() = runTest {
        coEvery { api.fetchNotes() } throws RuntimeException("Network error")

        val result = repository.getNotes()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `addNote delegates to API and returns created note`() = runTest {
        val created = Note(id = "2", title = "New", content = "Body")
        coEvery { api.createNote(any()) } returns created

        val result = repository.addNote("New", "Body")

        assertEquals(created, result)
        coVerify { api.createNote(mapOf("title" to "New", "content" to "Body")) }
    }

    @Test
    fun `removeNote delegates to API`() = runTest {
        coEvery { api.deleteNote("3") } returns Response.success(Unit)

        repository.removeNote("3")

        coVerify { api.deleteNote("3") }
    }
}
