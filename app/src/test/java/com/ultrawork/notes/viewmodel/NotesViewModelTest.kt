package com.ultrawork.notes.viewmodel

import com.ultrawork.notes.data.remote.ApiException
import com.ultrawork.notes.data.remote.NoteDto
import com.ultrawork.notes.data.remote.NoteRequest
import com.ultrawork.notes.data.repository.NotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private lateinit var repository: NotesRepository
    private lateinit var viewModel: NotesViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotes sets Success state on successful fetch`() = runTest {
        val notes = listOf(
            NoteDto("1", "Title", "Content", "2024-01-01", "2024-01-01")
        )
        coEvery { repository.getNotes() } returns Result.success(notes)
        viewModel = NotesViewModel(repository)

        viewModel.loadNotes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(notes, (state as UiState.Success).notes)
    }

    @Test
    fun `loadNotes sets Error state on 401`() = runTest {
        coEvery { repository.getNotes() } returns Result.failure(ApiException(401, "Unauthorized"))
        viewModel = NotesViewModel(repository)

        viewModel.loadNotes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Unauthorized (401)", (state as UiState.Error).message)
        assertEquals(401, state.code)
    }

    @Test
    fun `loadNotes sets Error state on 403`() = runTest {
        coEvery { repository.getNotes() } returns Result.failure(ApiException(403, "Forbidden"))
        viewModel = NotesViewModel(repository)

        viewModel.loadNotes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Forbidden (403)", (state as UiState.Error).message)
    }

    @Test
    fun `loadNotes sets Error state on 500`() = runTest {
        coEvery { repository.getNotes() } returns Result.failure(ApiException(500, "Internal Server Error"))
        viewModel = NotesViewModel(repository)

        viewModel.loadNotes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Server error (500)", (state as UiState.Error).message)
    }

    @Test
    fun `createNote reloads notes on success`() = runTest {
        val note = NoteDto("1", "New", "Content", "2024-01-01", "2024-01-01")
        val notes = listOf(note)
        coEvery { repository.createNote(any()) } returns Result.success(note)
        coEvery { repository.getNotes() } returns Result.success(notes)
        viewModel = NotesViewModel(repository)

        viewModel.createNote("New", "Content")
        advanceUntilIdle()

        coVerify { repository.createNote(NoteRequest("New", "Content")) }
        coVerify { repository.getNotes() }
    }

    @Test
    fun `deleteNote reloads notes on success`() = runTest {
        coEvery { repository.deleteNote("1") } returns Result.success(Unit)
        coEvery { repository.getNotes() } returns Result.success(emptyList())
        viewModel = NotesViewModel(repository)

        viewModel.deleteNote("1")
        advanceUntilIdle()

        coVerify { repository.deleteNote("1") }
        coVerify { repository.getNotes() }
    }

    @Test
    fun `deleteNote sets Error state on failure`() = runTest {
        coEvery { repository.deleteNote("invalid") } returns Result.failure(ApiException(404, "Not Found"))
        viewModel = NotesViewModel(repository)

        viewModel.deleteNote("invalid")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
    }

    @Test
    fun `onSearchQueryChanged updates search query`() = runTest {
        coEvery { repository.getNotes() } returns Result.success(emptyList())
        viewModel = NotesViewModel(repository)

        viewModel.onSearchQueryChanged("test")

        assertEquals("test", viewModel.searchQuery.value)
    }
}
