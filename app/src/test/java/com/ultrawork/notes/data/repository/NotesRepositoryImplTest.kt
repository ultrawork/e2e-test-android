package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotesRepositoryImplTest {

    private lateinit var fakeApi: FakeApiService
    private lateinit var repository: NotesRepositoryImpl

    @Before
    fun setUp() {
        fakeApi = FakeApiService()
        repository = NotesRepositoryImpl(fakeApi)
    }

    @Test
    fun `getNotes returns success with notes from api`() = runTest {
        val expected = listOf(
            Note(id = 1, title = "A", content = "a"),
            Note(id = 2, title = "B", content = "b")
        )
        fakeApi.notesToReturn = expected

        val result = repository.getNotes()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getNotes returns failure when api throws`() = runTest {
        fakeApi.shouldThrow = true

        val result = repository.getNotes()

        assertTrue(result.isFailure)
    }

    @Test
    fun `createNote returns success with created note`() = runTest {
        val created = Note(id = 10, title = "New", content = "Content")
        fakeApi.noteToReturn = created
        val request = CreateNoteRequest("New", "Content")

        val result = repository.createNote(request)

        assertTrue(result.isSuccess)
        assertEquals(created, result.getOrNull())
        assertEquals(request, fakeApi.lastCreateRequest)
    }

    @Test
    fun `createNote returns failure when api throws`() = runTest {
        fakeApi.shouldThrow = true

        val result = repository.createNote(CreateNoteRequest("T", "C"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteNote returns success`() = runTest {
        val result = repository.deleteNote(5L)

        assertTrue(result.isSuccess)
        assertEquals(5L, fakeApi.lastDeletedId)
    }

    @Test
    fun `deleteNote returns failure when api throws`() = runTest {
        fakeApi.shouldThrow = true

        val result = repository.deleteNote(5L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `toggleFavorite returns success with note from api`() = runTest {
        val note = Note(id = 1, title = "A", content = "a")
        fakeApi.noteToReturn = note

        val result = repository.toggleFavorite(1L)

        assertTrue(result.isSuccess)
        assertEquals(note, result.getOrNull())
    }

    @Test
    fun `toggleFavorite returns failure when api throws`() = runTest {
        fakeApi.shouldThrow = true

        val result = repository.toggleFavorite(1L)

        assertTrue(result.isFailure)
    }

    /**
     * In-memory fake of [ApiService] for unit testing.
     */
    private class FakeApiService : ApiService {

        var notesToReturn: List<Note> = emptyList()
        var noteToReturn: Note = Note(id = 0, title = "", content = "")
        var lastCreateRequest: CreateNoteRequest? = null
        var lastDeletedId: Long? = null
        var shouldThrow: Boolean = false

        override suspend fun getNotes(): List<Note> {
            if (shouldThrow) throw RuntimeException("API error")
            return notesToReturn
        }

        override suspend fun createNote(request: CreateNoteRequest): Note {
            if (shouldThrow) throw RuntimeException("API error")
            lastCreateRequest = request
            return noteToReturn
        }

        override suspend fun deleteNote(id: Long) {
            if (shouldThrow) throw RuntimeException("API error")
            lastDeletedId = id
        }

        override suspend fun toggleFavorite(id: Long): Note {
            if (shouldThrow) throw RuntimeException("API error")
            return noteToReturn
        }
    }
}
