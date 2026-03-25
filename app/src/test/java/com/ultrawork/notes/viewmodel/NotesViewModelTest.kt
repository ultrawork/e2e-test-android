package com.ultrawork.notes.viewmodel

import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotes populates notes on success`() = runTest {
        val fakeNotes = listOf(
            Note(id = "1", title = "Test", content = "Content")
        )
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.success(fakeNotes)
            override suspend fun createNote(title: String) = Result.success(fakeNotes[0])
            override suspend fun deleteNote(id: String) = Result.success(Unit)
        }
        val viewModel = NotesViewModel(repo)

        viewModel.loadNotes()
        advanceUntilIdle()

        assertEquals(fakeNotes, viewModel.notes.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadNotes sets error on failure`() = runTest {
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.failure<List<Note>>(RuntimeException("Network error"))
            override suspend fun createNote(title: String) = Result.failure<Note>(RuntimeException("err"))
            override suspend fun deleteNote(id: String) = Result.failure<Unit>(RuntimeException("err"))
        }
        val viewModel = NotesViewModel(repo)

        viewModel.loadNotes()
        advanceUntilIdle()

        assertTrue(viewModel.notes.value.isEmpty())
        assertEquals("Network error", viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `createNote reloads notes on success`() = runTest {
        var getNotesCallCount = 0
        val note = Note(id = "1", title = "New", content = "New")
        val repo = object : NotesRepository {
            override suspend fun getNotes(): Result<List<Note>> {
                getNotesCallCount++
                return Result.success(listOf(note))
            }
            override suspend fun createNote(title: String) = Result.success(note)
            override suspend fun deleteNote(id: String) = Result.success(Unit)
        }
        val viewModel = NotesViewModel(repo)

        viewModel.createNote("New")
        advanceUntilIdle()

        // createNote triggers loadNotes on success
        assertTrue(getNotesCallCount >= 1)
        assertEquals(listOf(note), viewModel.notes.value)
    }

    @Test
    fun `createNote sets error on failure`() = runTest {
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.success(emptyList<Note>())
            override suspend fun createNote(title: String) = Result.failure<Note>(RuntimeException("Create failed"))
            override suspend fun deleteNote(id: String) = Result.success(Unit)
        }
        val viewModel = NotesViewModel(repo)

        viewModel.createNote("X")
        advanceUntilIdle()

        assertEquals("Create failed", viewModel.error.value)
    }

    @Test
    fun `deleteNote removes note from list on success`() = runTest {
        val notes = listOf(
            Note(id = "1", title = "A", content = "A"),
            Note(id = "2", title = "B", content = "B")
        )
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.success(notes)
            override suspend fun createNote(title: String) = Result.success(notes[0])
            override suspend fun deleteNote(id: String) = Result.success(Unit)
        }
        val viewModel = NotesViewModel(repo)

        viewModel.loadNotes()
        advanceUntilIdle()
        assertEquals(2, viewModel.notes.value.size)

        viewModel.deleteNote("1")
        advanceUntilIdle()

        assertEquals(1, viewModel.notes.value.size)
        assertEquals("2", viewModel.notes.value[0].id)
    }

    @Test
    fun `deleteNote sets error on failure`() = runTest {
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.success(emptyList<Note>())
            override suspend fun createNote(title: String) = Result.failure<Note>(RuntimeException("err"))
            override suspend fun deleteNote(id: String) = Result.failure<Unit>(RuntimeException("Delete failed"))
        }
        val viewModel = NotesViewModel(repo)

        viewModel.deleteNote("1")
        advanceUntilIdle()

        assertEquals("Delete failed", viewModel.error.value)
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery`() = runTest {
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.success(emptyList<Note>())
            override suspend fun createNote(title: String) = Result.failure<Note>(RuntimeException("err"))
            override suspend fun deleteNote(id: String) = Result.success(Unit)
        }
        val viewModel = NotesViewModel(repo)

        viewModel.onSearchQueryChanged("test")

        assertEquals("test", viewModel.searchQuery.value)
    }

    @Test
    fun `filteredNotes filters by title`() = runTest {
        val notes = listOf(
            Note(id = "1", title = "Alpha", content = "A"),
            Note(id = "2", title = "Beta", content = "B")
        )
        val repo = object : NotesRepository {
            override suspend fun getNotes() = Result.success(notes)
            override suspend fun createNote(title: String) = Result.success(notes[0])
            override suspend fun deleteNote(id: String) = Result.success(Unit)
        }
        val viewModel = NotesViewModel(repo)

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("alph")
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredNotes.value.size)
        assertEquals("Alpha", viewModel.filteredNotes.value[0].title)
    }
}
