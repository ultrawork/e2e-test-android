package com.ultrawork.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.ultrawork.notes.ui.screens.NotesListScreen
import com.ultrawork.notes.ui.theme.NotesTheme
import com.ultrawork.notes.viewmodel.NotesViewModel
import org.junit.Rule
import org.junit.Test

/**
 * E2E UI tests for Android v35 — Mobile UI (NotesListScreen).
 * Covers automated scenarios SC-M03 and SC-M04.
 */
class NotesSearchUiE2ETests {

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
     * SC-M03: Clear search via "X" (Clear) button.
     *
     * Steps:
     * 1. Enter "Meet" in search field
     * 2. Verify filtered results (1 note: "Meeting Notes")
     * 3. Tap clear button (contentDescription: "Очистить поиск")
     * 4. Verify search field is cleared and all 5 notes are shown
     * 5. Verify counter shows total without filtered part
     */
    @Test
    fun scM03_clearSearchButtonRestoresFullList() {
        launchScreen()

        // Verify initial state — all 5 notes visible
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()

        // Enter search query "Meet"
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Meet")
        composeTestRule.waitForIdle()

        // Verify filtered — only "Meeting Notes" visible
        composeTestRule
            .onNodeWithText("Meeting Notes")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Найдено: 1 из 5")
            .assertIsDisplayed()

        // Verify clear button is visible and tap it
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .performClick()
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

        // Verify counter shows total without filtered part
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()
    }

    /**
     * SC-M04: ImeAction.Search hides keyboard and keeps filtered results.
     *
     * Steps:
     * 1. Tap search field
     * 2. Enter "Ideas"
     * 3. Perform ImeAction.Search
     * 4. Verify list still shows filtered result (1 card: "Ideas")
     */
    @Test
    fun scM04_imeActionSearchKeepsFilteredResults() {
        launchScreen()

        // Enter search query "Ideas"
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Ideas")
        composeTestRule.waitForIdle()

        // Perform ImeAction.Search
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performImeAction()
        composeTestRule.waitForIdle()

        // Verify filtered result — only "Ideas" card shown
        composeTestRule
            .onNodeWithText("Ideas")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("New app features")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_3")
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
            .onNodeWithText("Meeting Notes")
            .assertDoesNotExist()
    }
}
