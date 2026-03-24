package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.data.remote.DevTokenResponse
import com.ultrawork.notes.data.remote.UpdateNoteRequest
import com.ultrawork.notes.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NotesRepositoryImplTest {

    private val testNotes = listOf(
        Note(id = "1", title = "Note 1", content = "Content 1"),
        Note(id = "2", title = "Note 2", content = "Content 2")
    )

    private val fakeApiService = object : ApiService {
        override suspend fun getDevToken() = DevTokenResponse("fake-token")
        override suspend fun getNotes() = testNotes
        override suspend fun getNote(id: String) = testNotes.first { it.id == id }
        override suspend fun createNote(request: CreateNoteRequest) =
            Note(id = "3", title = request.title, content = request.content)
        override suspend fun updateNote(id: String, request: UpdateNoteRequest) =
            Note(id = id, title = request.title, content = request.content)
        override suspend fun deleteNote(id: String): Response<Unit> = Response.success(Unit)
    }

    @Test
    fun `getNotes returns list of notes on success`() = runTest {
        val repository = NotesRepositoryImpl(fakeApiService)
        val result = repository.getNotes()
        assertTrue(result.isSuccess)
        assertEquals(testNotes, result.getOrNull())
    }

    @Test
    fun `createNote returns created note on success`() = runTest {
        val repository = NotesRepositoryImpl(fakeApiService)
        val result = repository.createNote("New Note", "New Content")
        assertTrue(result.isSuccess)
        val note = result.getOrNull()!!
        assertEquals("3", note.id)
        assertEquals("New Note", note.title)
        assertEquals("New Content", note.content)
    }

    @Test
    fun `updateNote returns updated note on success`() = runTest {
        val repository = NotesRepositoryImpl(fakeApiService)
        val result = repository.updateNote("1", "Updated", "Updated Content")
        assertTrue(result.isSuccess)
        val note = result.getOrNull()!!
        assertEquals("1", note.id)
        assertEquals("Updated", note.title)
    }

    @Test
    fun `deleteNote returns success`() = runTest {
        val repository = NotesRepositoryImpl(fakeApiService)
        val result = repository.deleteNote("1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getNotes returns failure when api throws`() = runTest {
        val failingApi = object : ApiService {
            override suspend fun getDevToken() = DevTokenResponse("fake")
            override suspend fun getNotes(): List<Note> = throw RuntimeException("Network error")
            override suspend fun getNote(id: String): Note = throw RuntimeException("Network error")
            override suspend fun createNote(request: CreateNoteRequest): Note = throw RuntimeException("Network error")
            override suspend fun updateNote(id: String, request: UpdateNoteRequest): Note = throw RuntimeException("Network error")
            override suspend fun deleteNote(id: String): Response<Unit> = throw RuntimeException("Network error")
        }
        val repository = NotesRepositoryImpl(failingApi)
        val result = repository.getNotes()
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
