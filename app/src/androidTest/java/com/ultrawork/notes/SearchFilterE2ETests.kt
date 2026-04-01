package com.ultrawork.notes

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.ultrawork.notes.ui.screens.NotesListScreen
import com.ultrawork.notes.ui.theme.NotesTheme
import com.ultrawork.notes.viewmodel.NotesViewModel
import org.junit.Rule
import org.junit.Test

/**
 * E2E UI tests for Android v34 — Search and filter scenarios.
 *
 * Covers automated scenarios:
 * - SC-M03 from android-notes-ui.md: search filter by "Ideas"
 * - SC-M04 from android-notes-ui.md: clear search restores full list
 * - SC-M03 from android-notes-mobile-v34.md: clear search via X button
 */
class SearchFilterE2ETests {

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
     * SC-M03 (android-notes-ui.md): Search filter by "Ideas".
     *
     * Steps:
     * 1. Enter "Ideas" in search field (testTag: notes_search_field)
     * 2. Verify only note_card_3 is displayed
     * 3. Verify notes_counter shows "Найдено: 1 из 5"
     * 4. Verify other cards (note_card_1, 2, 4, 5) are not displayed
     */
    @Test
    fun scM03_ui_searchFilterByIdeasShowsOnlyMatchingNote() {
        launchScreen()

        // Enter search query "Ideas"
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Ideas")
        composeTestRule.waitForIdle()

        // Verify only note_card_3 ("Ideas") is displayed
        composeTestRule
            .onNodeWithTag("note_card_3")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Ideas")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("New app features")
            .assertIsDisplayed()

        // Verify counter shows filtered count
        composeTestRule
            .onNodeWithTag("notes_counter")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Найдено: 1 из 5")
            .assertIsDisplayed()

        // Verify other note cards are NOT displayed
        composeTestRule
            .onNodeWithTag("note_card_1")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_2")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_4")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("note_card_5")
            .assertDoesNotExist()
    }

    /**
     * SC-M04 (android-notes-ui.md): Clear search via X button restores full list.
     *
     * Steps:
     * 1. Enter "Meet" in search field
     * 2. Verify filtered to 1 note and counter "Найдено: 1 из 5"
     * 3. Tap clear icon (contentDescription: "Очистить поиск")
     * 4. Verify all 5 cards (note_card_1..note_card_5) are displayed
     * 5. Verify counter shows "Всего заметок: 5"
     * 6. Verify clear button is no longer visible
     */
    @Test
    fun scM04_ui_clearSearchRestoresFullList() {
        launchScreen()

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

        // Tap clear button (contentDescription: "Очистить поиск")
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify all 5 note cards are displayed
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

        // Verify clear button is hidden (search field is empty)
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .assertDoesNotExist()
    }

    /**
     * SC-M03 (android-notes-mobile-v34.md): Clear search via X button.
     *
     * Steps:
     * 1. Enter "Ideas" in search field
     * 2. Verify clear icon (X) appears
     * 3. Tap clear icon
     * 4. Verify search field is empty (all 5 notes restored)
     * 5. Verify counter shows "5 notes" / "Всего заметок: 5"
     */
    @Test
    fun scM03_mobile_clearSearchButtonRestoresAllNotes() {
        launchScreen()

        // Verify initial state
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()

        // Enter "Ideas" in search field
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Ideas")
        composeTestRule.waitForIdle()

        // Verify filtered to 1 note
        composeTestRule
            .onNodeWithTag("note_card_3")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Найдено: 1 из 5")
            .assertIsDisplayed()

        // Verify clear icon appeared and tap it
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify all 5 notes are restored
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

        // Verify counter restored
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()
    }
}
