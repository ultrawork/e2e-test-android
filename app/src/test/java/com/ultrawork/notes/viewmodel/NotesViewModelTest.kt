package com.ultrawork.notes.viewmodel

import com.ultrawork.notes.model.Note
import com.ultrawork.notes.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private lateinit var viewModel: NotesViewModel
    private lateinit var fakeRepository: TestNotesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestNotesRepository()
        viewModel = NotesViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotes sets notes on success`() = runTest(testDispatcher) {
        viewModel.loadNotes()
        advanceUntilIdle()

        assertEquals(2, viewModel.notes.value.size)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadNotes sets error on failure`() = runTest(testDispatcher) {
        fakeRepository.shouldFail = true
        viewModel.loadNotes()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `toggleFavoritesFilter toggles state`() {
        assertFalse(viewModel.showFavoritesOnly.value)
        viewModel.toggleFavoritesFilter()
        assertTrue(viewModel.showFavoritesOnly.value)
        viewModel.toggleFavoritesFilter()
        assertFalse(viewModel.showFavoritesOnly.value)
    }

    @Test
    fun `onSearchQueryChanged updates search query`() {
        viewModel.onSearchQueryChanged("test")
        assertEquals("test", viewModel.searchQuery.value)
    }

    @Test
    fun `toggleFavorite updates note in list`() = runTest(testDispatcher) {
        viewModel.loadNotes()
        advanceUntilIdle()

        viewModel.toggleFavorite("1")
        advanceUntilIdle()

        val note = viewModel.notes.value.find { it.id == "1" }
        assertTrue(note!!.isFavorited)
    }

    @Test
    fun `deleteNote removes note and reloads`() = runTest(testDispatcher) {
        viewModel.loadNotes()
        advanceUntilIdle()
        assertEquals(2, viewModel.notes.value.size)

        viewModel.deleteNote("1")
        advanceUntilIdle()

        assertEquals(1, viewModel.notes.value.size)
    }

    @Test
    fun `createNote adds note and reloads`() = runTest(testDispatcher) {
        viewModel.loadNotes()
        advanceUntilIdle()

        viewModel.createNote("New", "Content")
        advanceUntilIdle()

        assertEquals(3, viewModel.notes.value.size)
    }
}

/**
 * Тестовая реализация NotesRepository для unit-тестов ViewModel.
 */
private class TestNotesRepository : NotesRepository {

    var shouldFail = false

    private val notes = mutableListOf(
        Note(id = "1", title = "Note 1", content = "Content 1"),
        Note(id = "2", title = "Note 2", content = "Content 2")
    )

    override suspend fun getNotes(): Result<List<Note>> {
        if (shouldFail) return Result.failure(RuntimeException("Network error"))
        return Result.success(notes.toList())
    }

    override suspend fun createNote(title: String, content: String): Result<Note> {
        if (shouldFail) return Result.failure(RuntimeException("Network error"))
        val note = Note(id = "${notes.size + 1}", title = title, content = content)
        notes.add(note)
        return Result.success(note)
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network error"))
        notes.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(id: String): Result<Note> {
        if (shouldFail) return Result.failure(RuntimeException("Network error"))
        val index = notes.indexOfFirst { it.id == id }
        if (index == -1) return Result.failure(NoSuchElementException("Not found"))
        val updated = notes[index].copy(isFavorited = !notes[index].isFavorited)
        notes[index] = updated
        return Result.success(updated)
    }
}
