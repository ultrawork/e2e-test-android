package com.ultrawork.notes.viewmodel

import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testNotes = listOf(
        Note(id = "1", title = "Shopping List", content = "Milk, Eggs, Bread"),
        Note(id = "2", title = "Meeting Notes", content = "Discuss project timeline"),
        Note(id = "3", title = "Ideas", content = "New app features")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createFakeRepository(
        notesResult: Result<List<Note>> = Result.success(testNotes)
    ): NotesRepository = object : NotesRepository {
        override suspend fun getNotes(): Result<List<Note>> = notesResult
        override suspend fun createNote(title: String, content: String) =
            Result.success(Note(title = title, content = content))
        override suspend fun updateNote(id: String, title: String, content: String) =
            Result.success(Note(id = id, title = title, content = content))
        override suspend fun deleteNote(id: String) = Result.success(Unit)
    }

    @Test
    fun `loadNotes sets notes on success`() = runTest {
        val viewModel = NotesViewModel(createFakeRepository())
        viewModel.loadNotes()
        advanceUntilIdle()

        assertEquals(testNotes, viewModel.notes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadNotes sets error on failure`() = runTest {
        val repository = createFakeRepository(
            notesResult = Result.failure(RuntimeException("Network error"))
        )
        val viewModel = NotesViewModel(repository)
        viewModel.loadNotes()
        advanceUntilIdle()

        assertTrue(viewModel.notes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("Network error", viewModel.error.value)
    }

    @Test
    fun `onSearchQueryChanged updates search query`() = runTest {
        val viewModel = NotesViewModel(createFakeRepository())
        viewModel.onSearchQueryChanged("test")
        assertEquals("test", viewModel.searchQuery.value)
    }

    @Test
    fun `filteredNotes filters by title`() = runTest {
        val viewModel = NotesViewModel(createFakeRepository())
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredNotes.collect {}
        }

        viewModel.loadNotes()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Notes")
        advanceUntilIdle()

        val filtered = viewModel.filteredNotes.value
        assertEquals(1, filtered.size)
        assertEquals("Meeting Notes", filtered[0].title)
        job.cancel()
    }

    @Test
    fun `filteredNotes returns all when query is blank`() = runTest {
        val viewModel = NotesViewModel(createFakeRepository())
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredNotes.collect {}
        }

        viewModel.loadNotes()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()

        assertEquals(testNotes.size, viewModel.filteredNotes.value.size)
        job.cancel()
    }

    @Test
    fun `initial state has empty notes and no loading`() = runTest {
        val viewModel = NotesViewModel(createFakeRepository())
        assertTrue(viewModel.notes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
}
