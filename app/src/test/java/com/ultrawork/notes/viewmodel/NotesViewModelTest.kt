package com.ultrawork.notes.viewmodel

import com.ultrawork.notes.data.repository.NotesRepository
import com.ultrawork.notes.model.Note
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: NotesRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        viewModel = NotesViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotes populates notes from repository`() = runTest {
        val notes = listOf(
            Note(id = "1", title = "A", content = "B"),
            Note(id = "2", title = "C", content = "D")
        )
        coEvery { repo.getNotes() } returns notes

        viewModel.loadNotes()

        assertEquals(notes, viewModel.notes.value)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `loadNotes shows empty list when repository returns empty`() = runTest {
        coEvery { repo.getNotes() } returns emptyList()

        viewModel.loadNotes()

        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun `addNote appends note to list`() = runTest {
        val existing = Note(id = "1", title = "Old", content = "Old content")
        val created = Note(id = "2", title = "New", content = "New content")
        coEvery { repo.getNotes() } returns listOf(existing)
        coEvery { repo.addNote("New", "New content") } returns created

        viewModel.loadNotes()
        viewModel.addNote("New", "New content")

        assertEquals(2, viewModel.notes.value.size)
        assertEquals(created, viewModel.notes.value.last())
    }

    @Test
    fun `addNote sets error on failure`() = runTest {
        coEvery { repo.getNotes() } returns emptyList()
        coEvery { repo.addNote(any(), any()) } throws RuntimeException("API error")

        viewModel.loadNotes()
        viewModel.addNote("T", "C")

        assertEquals("API error", viewModel.error)
    }

    @Test
    fun `deleteNote removes note from list`() = runTest {
        val note1 = Note(id = "1", title = "A", content = "B")
        val note2 = Note(id = "2", title = "C", content = "D")
        coEvery { repo.getNotes() } returns listOf(note1, note2)
        coEvery { repo.removeNote("1") } returns Unit

        viewModel.loadNotes()
        viewModel.deleteNote("1")

        assertEquals(1, viewModel.notes.value.size)
        assertEquals(note2, viewModel.notes.value.first())
        coVerify { repo.removeNote("1") }
    }

    @Test
    fun `deleteNote sets error on failure`() = runTest {
        val note = Note(id = "1", title = "A", content = "B")
        coEvery { repo.getNotes() } returns listOf(note)
        coEvery { repo.removeNote("1") } throws RuntimeException("Delete failed")

        viewModel.loadNotes()
        viewModel.deleteNote("1")

        assertEquals("Delete failed", viewModel.error)
        assertEquals(1, viewModel.notes.value.size)
    }

    @Test
    fun `search filters notes by title`() = runTest {
        val notes = listOf(
            Note(id = "1", title = "Shopping List", content = "Milk"),
            Note(id = "2", title = "Meeting Notes", content = "Discuss")
        )
        coEvery { repo.getNotes() } returns notes

        // Collect filteredNotes to activate WhileSubscribed sharing
        val collectedValues = mutableListOf<List<Note>>()
        val job = launch(testDispatcher) {
            viewModel.filteredNotes.collect { collectedValues.add(it) }
        }

        viewModel.loadNotes()
        viewModel.onSearchQueryChanged("Meeting")
        advanceUntilIdle()

        val filtered = viewModel.filteredNotes.value
        assertEquals(1, filtered.size)
        assertEquals("Meeting Notes", filtered.first().title)

        job.cancel()
    }

    @Test
    fun `error is null after successful operations`() = runTest {
        coEvery { repo.getNotes() } returns emptyList()

        viewModel.loadNotes()

        assertNull(viewModel.error)
    }
}
