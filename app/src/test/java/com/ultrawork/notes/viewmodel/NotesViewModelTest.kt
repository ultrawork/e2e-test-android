package com.ultrawork.notes.viewmodel

import com.ultrawork.notes.data.remote.CreateNoteRequest
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeTestRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTestRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): NotesViewModel {
        return NotesViewModel(fakeRepository)
    }

    // --- init / loadNotes ---

    @Test
    fun `init calls loadNotes and populates notes`() = runTest {
        fakeRepository.notesToReturn = listOf(
            Note(id = 1, title = "A", content = "a"),
            Note(id = 2, title = "B", content = "b")
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.notes.value.size)
        assertEquals("A", viewModel.notes.value[0].title)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadNotes sets isLoading to true during load`() = runTest {
        fakeRepository.notesToReturn = listOf(Note(id = 1, title = "A", content = "a"))

        viewModel = createViewModel()
        // Before advancing, isLoading should be true after init kicks off
        testDispatcher.scheduler.advanceTimeBy(1)

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadNotes sets error on failure`() = runTest {
        fakeRepository.shouldFail = true
        fakeRepository.errorMessage = "Network error"

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.error.value)
        assertTrue(viewModel.notes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadNotes clears previous error on success`() = runTest {
        fakeRepository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        // Now succeed
        fakeRepository.shouldFail = false
        fakeRepository.notesToReturn = listOf(Note(id = 1, title = "A", content = "a"))
        viewModel.loadNotes()
        advanceUntilIdle()

        assertNull(viewModel.error.value)
        assertEquals(1, viewModel.notes.value.size)
    }

    // --- toggleFavorite ---

    @Test
    fun `toggleFavorite updates note in list`() = runTest {
        val note = Note(id = 1, title = "A", content = "a", isFavorited = false)
        fakeRepository.notesToReturn = listOf(note)
        fakeRepository.toggleResult = note.copy(isFavorited = true)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite(1L)
        advanceUntilIdle()

        assertTrue(viewModel.notes.value[0].isFavorited)
    }

    @Test
    fun `toggleFavorite sets error on failure`() = runTest {
        fakeRepository.notesToReturn = listOf(Note(id = 1, title = "A", content = "a"))

        viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.shouldFail = true
        fakeRepository.errorMessage = "Toggle failed"
        viewModel.toggleFavorite(1L)
        advanceUntilIdle()

        assertEquals("Toggle failed", viewModel.error.value)
    }

    // --- toggleShowFavoritesOnly ---

    @Test
    fun `toggleShowFavoritesOnly flips value`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.showFavoritesOnly.value)

        viewModel.toggleShowFavoritesOnly()
        assertTrue(viewModel.showFavoritesOnly.value)

        viewModel.toggleShowFavoritesOnly()
        assertFalse(viewModel.showFavoritesOnly.value)
    }

    // --- filteredNotes ---

    @Test
    fun `filteredNotes filters by search query`() = runTest {
        fakeRepository.notesToReturn = listOf(
            Note(id = 1, title = "Shopping List", content = "a"),
            Note(id = 2, title = "Meeting Notes", content = "b")
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Meeting")
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredNotes.value.size)
        assertEquals("Meeting Notes", viewModel.filteredNotes.value[0].title)
    }

    @Test
    fun `filteredNotes filters by favorites only`() = runTest {
        fakeRepository.notesToReturn = listOf(
            Note(id = 1, title = "A", content = "a", isFavorited = true),
            Note(id = 2, title = "B", content = "b", isFavorited = false)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleShowFavoritesOnly()
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredNotes.value.size)
        assertEquals("A", viewModel.filteredNotes.value[0].title)
    }

    @Test
    fun `filteredNotes combines search and favorites filter`() = runTest {
        fakeRepository.notesToReturn = listOf(
            Note(id = 1, title = "Shopping List", content = "a", isFavorited = true),
            Note(id = 2, title = "Shopping Cart", content = "b", isFavorited = false),
            Note(id = 3, title = "Meeting Notes", content = "c", isFavorited = true)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleShowFavoritesOnly()
        viewModel.onSearchQueryChanged("Shopping")
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredNotes.value.size)
        assertEquals("Shopping List", viewModel.filteredNotes.value[0].title)
    }

    @Test
    fun `filteredNotes returns all when no filters active`() = runTest {
        fakeRepository.notesToReturn = listOf(
            Note(id = 1, title = "A", content = "a"),
            Note(id = 2, title = "B", content = "b")
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.filteredNotes.value.size)
    }

    // --- onSearchQueryChanged ---

    @Test
    fun `onSearchQueryChanged updates searchQuery`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("test")
        assertEquals("test", viewModel.searchQuery.value)
    }

    /**
     * In-memory fake of [NotesRepository] for unit testing.
     */
    private class FakeTestRepository : NotesRepository {
        var notesToReturn: List<Note> = emptyList()
        var toggleResult: Note = Note(id = 0, title = "", content = "")
        var shouldFail: Boolean = false
        var errorMessage: String = "Test error"

        override suspend fun getNotes(): Result<List<Note>> =
            if (shouldFail) Result.failure(RuntimeException(errorMessage))
            else Result.success(notesToReturn)

        override suspend fun createNote(request: CreateNoteRequest): Result<Note> =
            if (shouldFail) Result.failure(RuntimeException(errorMessage))
            else Result.success(Note(id = 0, title = request.title, content = request.content))

        override suspend fun deleteNote(id: Long): Result<Unit> =
            if (shouldFail) Result.failure(RuntimeException(errorMessage))
            else Result.success(Unit)

        override suspend fun toggleFavorite(id: Long): Result<Note> =
            if (shouldFail) Result.failure(RuntimeException(errorMessage))
            else Result.success(toggleResult)
    }
}
