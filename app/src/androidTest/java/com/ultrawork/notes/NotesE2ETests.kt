package com.ultrawork.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.ultrawork.notes.data.repository.fake.FakeNotesRepository
import com.ultrawork.notes.model.Note
import com.ultrawork.notes.ui.screens.NotesListScreen
import com.ultrawork.notes.ui.theme.NotesTheme
import com.ultrawork.notes.viewmodel.NotesViewModel
import org.junit.Rule
import org.junit.Test

class NotesE2ETests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testNotes = listOf(
        Note(id = "1", title = "Shopping List", content = "Milk, Eggs, Bread"),
        Note(id = "2", title = "Meeting Notes", content = "Discuss project timeline"),
        Note(id = "3", title = "Ideas", content = "New app features"),
        Note(id = "4", title = "Travel Plans", content = "Book flights and hotel"),
        Note(id = "5", title = "Work Tasks", content = "Complete documentation")
    )

    private fun launchScreen() {
        val fakeRepo = FakeNotesRepository(initialNotes = testNotes)
        composeTestRule.setContent {
            NotesTheme {
                NotesListScreen(viewModel = NotesViewModel(fakeRepo))
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * SC-001: Verify initial state - counter shows total, all 5 notes visible, search field exists.
     */
    @Test
    fun sc001_initialStateShowsAllNotesAndCounter() {
        launchScreen()

        // Verify counter displays total
        composeTestRule
            .onNodeWithTag("notes_counter")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()

        // Verify search field exists
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Поиск заметок")
            .assertIsDisplayed()

        // Verify all 5 note cards are present
        composeTestRule
            .onNodeWithTag("note_card_1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_2")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_3")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_4")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_5")
            .assertIsDisplayed()

        // Verify first note content
        composeTestRule
            .onNodeWithText("Shopping List")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Milk, Eggs, Bread")
            .assertIsDisplayed()

        // Verify LazyColumn exists
        composeTestRule
            .onNodeWithTag("notes_list")
            .assertIsDisplayed()
    }

    /**
     * SC-002: Search filtering - enter "Notes", verify filtered results, clear search, verify restore.
     */
    @Test
    fun sc002_searchFiltersNotesAndClearRestoresAll() {
        launchScreen()

        // Verify initial counter
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()

        // Enter search query "Notes"
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Notes")
        composeTestRule.waitForIdle()

        // Verify only "Meeting Notes" is visible
        composeTestRule
            .onNodeWithText("Meeting Notes")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_2")
            .assertIsDisplayed()

        // Verify counter shows filtered count
        composeTestRule
            .onNodeWithText("Найдено: 1 из 5")
            .assertIsDisplayed()

        // Verify other notes are not displayed
        composeTestRule
            .onNodeWithText("Shopping List")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Ideas")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Travel Plans")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Work Tasks")
            .assertDoesNotExist()

        // Clear search
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextClearance()
        composeTestRule.waitForIdle()

        // Verify all 5 notes are visible again
        composeTestRule
            .onNodeWithTag("note_card_1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_2")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_3")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_4")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_5")
            .assertIsDisplayed()

        // Verify counter shows total again
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()
    }

    /**
     * SC-003: Search with no results - enter non-matching query, verify empty list and counter.
     */
    @Test
    fun sc003_searchWithNoResultsShowsEmptyStateAndCounter() {
        launchScreen()

        // Enter a non-matching search query
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("несуществующая заметка")
        composeTestRule.waitForIdle()

        // Verify no note cards are displayed
        composeTestRule
            .onNodeWithTag("note_card_1")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_2")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_3")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_4")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_5")
            .assertDoesNotExist()

        // Verify counter shows 0 filtered results
        composeTestRule
            .onNodeWithText("Найдено: 0 из 5")
            .assertIsDisplayed()
    }
}
