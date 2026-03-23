package com.ultrawork.notes.data.repository

import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `getNotes delegates to apiService`() = runTest {
        val expected = listOf(
            Note(id = "1", title = "A", content = "a"),
            Note(id = "2", title = "B", content = "b")
        )
        fakeApi.notesToReturn = expected

        val result = repository.getNotes()

        assertEquals(expected, result)
    }

    @Test
    fun `getNote returns matching note from API list`() = runTest {
        fakeApi.notesToReturn = listOf(
            Note(id = "1", title = "A", content = "a"),
            Note(id = "2", title = "B", content = "b")
        )

        val result = repository.getNote("2")

        assertEquals("B", result?.title)
    }

    @Test
    fun `getNote returns null when note not found`() = runTest {
        fakeApi.notesToReturn = listOf(
            Note(id = "1", title = "A", content = "a")
        )

        val result = repository.getNote("99")

        assertNull(result)
    }

    @Test
    fun `createNote sends CreateNoteRequest to API`() = runTest {
        val note = Note(id = "", title = "New", content = "Content")
        val created = Note(id = "10", title = "New", content = "Content")
        fakeApi.noteToReturn = created

        val result = repository.createNote(note)

        assertEquals(created, result)
        assertEquals(CreateNoteRequest("New", "Content"), fakeApi.lastCreateRequest)
    }

    @Test
    fun `deleteNote calls API and returns true`() = runTest {
        val result = repository.deleteNote("5")

        assertTrue(result)
        assertEquals("5", fakeApi.lastDeletedId)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `updateNote throws UnsupportedOperationException`() = runTest {
        repository.updateNote(Note(id = "1", title = "T", content = "C"))
    }

    /**
     * In-memory fake of [ApiService] for unit testing.
     */
    private class FakeApiService : ApiService {

        var notesToReturn: List<Note> = emptyList()
        var noteToReturn: Note = Note(id = "0", title = "", content = "")
        var lastCreateRequest: CreateNoteRequest? = null
        var lastDeletedId: String? = null

        override suspend fun getNotes(): List<Note> = notesToReturn

        override suspend fun createNote(request: CreateNoteRequest): Note {
            lastCreateRequest = request
            return noteToReturn
        }

        override suspend fun deleteNote(id: String) {
            lastDeletedId = id
        }

        override suspend fun toggleFavorite(id: String): Note {
            return notesToReturn.first { it.id == id }.let {
                it.copy(isFavorited = !it.isFavorited)
            }
        }
    }
}
