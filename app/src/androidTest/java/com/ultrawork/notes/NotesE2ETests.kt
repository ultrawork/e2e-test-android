package com.ultrawork.notes

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.ultrawork.notes.ui.screens.NotesListScreen
import com.ultrawork.notes.ui.theme.NotesTheme
import com.ultrawork.notes.viewmodel.NotesViewModel
import org.junit.Rule
import org.junit.Test

class NotesE2ETests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen() {
        composeTestRule.setContent {
            NotesTheme {
                NotesListScreen(viewModel = NotesViewModel())
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

    /**
     * SC-004: Toggle favorite changes note state and preserves all notes.
     */
    @Test
    fun sc004_toggleFavoriteChangesNoteState() {
        launchScreen()

        // Toggle favorite on first note
        composeTestRule
            .onNodeWithTag("favorite_button_1")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Toggle favorite off on first note
        composeTestRule
            .onNodeWithTag("favorite_button_1")
            .performClick()
        composeTestRule.waitForIdle()

        // All 5 notes are still visible
        composeTestRule.onNodeWithTag("note_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_card_5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Всего заметок: 5").assertIsDisplayed()
    }

    /**
     * SC-005: Favorites filter shows only favorited notes with correct counter.
     */
    @Test
    fun sc005_favoritesFilterShowsOnlyFavoritedNotes() {
        launchScreen()

        // Favorite note 1
        composeTestRule
            .onNodeWithTag("favorite_button_1")
            .performClick()
        composeTestRule.waitForIdle()

        // Enable favorites filter
        composeTestRule
            .onNodeWithTag("favorites_filter_button")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Only note_card_1 is visible
        composeTestRule.onNodeWithTag("note_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_card_2").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_card_3").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_card_4").assertDoesNotExist()
        composeTestRule.onNodeWithTag("note_card_5").assertDoesNotExist()

        // Counter shows 1 of 5
        composeTestRule.onNodeWithText("Найдено: 1 из 5").assertIsDisplayed()

        // Disable favorites filter — all 5 notes visible again
        composeTestRule
            .onNodeWithTag("favorites_filter_button")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Всего заметок: 5").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("note_card_5").assertIsDisplayed()
    }
}
