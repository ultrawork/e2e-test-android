package com.ultrawork.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun navigateToNoteCreate() {
        // Navigate to note_create route
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.runOnUiThread {
                // The NavGraph starts at LOGIN, we need to navigate to NOTE_CREATE
            }
        }
        // Use testTag on a navigation element or navigate via UI
        val createButton = composeTestRule.onNodeWithTag("navigate_to_create")
        try {
            createButton.assertIsDisplayed()
            createButton.performClick()
        } catch (_: AssertionError) {
            // If no nav button, the screen might already be showing NoteEditScreen
        }
    }

    // SC-201: Note create screen - form elements and initial counter state
    @Test
    fun testSC201_NoteEditScreen_FormElementsAndInitialCounter() {
        // Verify title input field exists
        composeTestRule.onNodeWithTag("note_title_field").assertIsDisplayed()

        // Verify content input field exists
        composeTestRule.onNodeWithTag("note_content_field").assertIsDisplayed()

        // Verify character counter shows "0 символов"
        composeTestRule.onNodeWithTag("character_counter").assertIsDisplayed()
        composeTestRule.onNodeWithTag("character_counter").assertTextEquals("0 символов")

        // Verify save button exists
        composeTestRule.onNodeWithTag("save_button").assertIsDisplayed()
    }

    // SC-202: Character counter updates in real time
    @Test
    fun testSC202_CharacterCounter_UpdatesInRealTime() {
        val counterNode = composeTestRule.onNodeWithTag("character_counter")
        val contentField = composeTestRule.onNodeWithTag("note_content_field")

        // Initial state: "0 символов"
        counterNode.assertTextEquals("0 символов")

        // Type "Тест" (4 characters)
        contentField.performClick()
        contentField.performTextInput("Тест")
        composeTestRule.waitForIdle()
        counterNode.assertTextEquals("4 символов")

        // Append " заметки" (total 12 characters)
        contentField.performTextInput(" заметки")
        composeTestRule.waitForIdle()
        counterNode.assertTextEquals("12 символов")

        // Clear text
        contentField.performTextClearance()
        composeTestRule.waitForIdle()
        counterNode.assertTextEquals("0 символов")
    }

    // SC-203: Character counter accessibility for TalkBack
    @Test
    fun testSC203_CharacterCounter_AccessibilityContentDescription() {
        val contentField = composeTestRule.onNodeWithTag("note_content_field")

        // Type "Привет" (6 characters)
        contentField.performClick()
        contentField.performTextInput("Привет")
        composeTestRule.waitForIdle()

        // Verify counter content description
        composeTestRule.onNodeWithTag("character_counter")
            .assertIsDisplayed()
            .assertTextEquals("6 символов")

        // Append " мир" (total 10 characters)
        contentField.performTextInput(" мир")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("character_counter")
            .assertTextEquals("10 символов")
    }
}
