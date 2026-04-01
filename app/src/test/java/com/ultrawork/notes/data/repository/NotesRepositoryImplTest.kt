package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiException
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.data.remote.NoteRequest
import com.ultrawork.notes.data.remote.NotesApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NotesRepositoryImplTest {

    private lateinit var api: NotesApi
    private lateinit var repository: NotesRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = NotesRepositoryImpl(api)
    }

    @Test
    fun `getNotes returns success with notes list`() = runTest {
        val notes = listOf(
            NoteDto("1", "Title", "Content", "2024-01-01", "2024-01-01")
        )
        coEvery { api.getNotes() } returns Response.success(notes)

        val result = repository.getNotes()

        assertTrue(result.isSuccess)
        assertEquals(notes, result.getOrNull())
    }

    @Test
    fun `getNotes returns failure on API error`() = runTest {
        coEvery { api.getNotes() } returns Response.error(
            401, "Unauthorized".toResponseBody()
        )

        val result = repository.getNotes()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ApiException
        assertEquals(401, exception.code)
    }

    @Test
    fun `createNote returns success with created note`() = runTest {
        val note = NoteDto("1", "Title", "Content", "2024-01-01", "2024-01-01")
        coEvery { api.createNote(any()) } returns Response.success(201, note)

        val result = repository.createNote(NoteRequest("Title", "Content"))

        assertTrue(result.isSuccess)
        assertEquals(note, result.getOrNull())
    }

    @Test
    fun `createNote returns failure on API error`() = runTest {
        coEvery { api.createNote(any()) } returns Response.error(
            400, "Bad Request".toResponseBody()
        )

        val result = repository.createNote(NoteRequest("Title", ""))

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ApiException
        assertEquals(400, exception.code)
    }

    @Test
    fun `deleteNote returns success on 204`() = runTest {
        coEvery { api.deleteNote("1") } returns Response.success(204, Unit)

        val result = repository.deleteNote("1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteNote returns failure on 404`() = runTest {
        coEvery { api.deleteNote("invalid") } returns Response.error(
            404, "Not Found".toResponseBody()
        )

        val result = repository.deleteNote("invalid")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ApiException
        assertEquals(404, exception.code)
    }
}
