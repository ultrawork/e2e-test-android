package com.ultrawork.notes

import android.content.Context
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.AuthInterceptor
import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import com.ultrawork.notes.di.FakeNotesRepository
import com.ultrawork.notes.ui.screens.NotesListScreen
import com.ultrawork.notes.ui.theme.NotesTheme
import com.ultrawork.notes.viewmodel.NotesViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@HiltAndroidTest
class NotesE2ETests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun launchScreen(viewModel: NotesViewModel) {
        composeTestRule.setContent {
            NotesTheme {
                NotesListScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun createFakeViewModel(): NotesViewModel {
        return NotesViewModel(FakeNotesRepository())
    }

    private fun createMockApiViewModel(
        mockWebServer: MockWebServer,
        token: String? = null
    ): NotesViewModel {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("notes_prefs_test", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        if (token != null) {
            prefs.edit().putString(AuthInterceptor.KEY_AUTH_TOKEN, token).apply()
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(prefs))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/api/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)
        return NotesViewModel(NotesRepositoryImpl(api))
    }

    /**
     * SC-001: Verify initial state - counter shows total, all 5 notes visible, search field exists.
     */
    @Test
    fun sc001_initialStateShowsAllNotesAndCounter() {
        launchScreen(createFakeViewModel())

        composeTestRule
            .onNodeWithTag("notes_counter")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("notes_search_field")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Поиск заметок")
            .assertIsDisplayed()

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

        composeTestRule
            .onNodeWithText("Shopping List")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Milk, Eggs, Bread")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("notes_list")
            .assertIsDisplayed()
    }

    /**
     * SC-002: Search filtering - enter "Notes", verify filtered results, clear search, verify restore.
     */
    @Test
    fun sc002_searchFiltersNotesAndClearRestoresAll() {
        launchScreen(createFakeViewModel())

        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Notes")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Meeting Notes")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("note_card_2")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Найдено: 1 из 5")
            .assertIsDisplayed()

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

        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextClearance()
        composeTestRule.waitForIdle()

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

        composeTestRule
            .onNodeWithText("Всего заметок: 5")
            .assertIsDisplayed()
    }

    /**
     * SC-003: Search with no results - enter non-matching query, verify empty list and counter.
     */
    @Test
    fun sc003_searchWithNoResultsShowsEmptyStateAndCounter() {
        launchScreen(createFakeViewModel())

        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("несуществующая заметка")
        composeTestRule.waitForIdle()

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

        composeTestRule
            .onNodeWithText("Найдено: 0 из 5")
            .assertIsDisplayed()
    }

    /**
     * SC-004: No token — API returns 401, error state displayed in UI.
     */
    @Test
    fun sc004_noTokenReturns401Error() {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":"Unauthorized"}""")
        )
        mockWebServer.start()

        try {
            val viewModel = createMockApiViewModel(mockWebServer, token = null)
            launchScreen(viewModel)

            composeTestRule.waitUntil(5000) {
                composeTestRule
                    .onAllNodesWithTag("error_message")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule
                .onNodeWithTag("error_message")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Ошибка: 401")
                .assertIsDisplayed()

            val request = mockWebServer.takeRequest()
            assertEquals(null, request.getHeader("Authorization"))
        } finally {
            mockWebServer.shutdown()
        }
    }

    /**
     * SC-005: With token — notes load successfully, Authorization header sent.
     */
    @Test
    fun sc005_withTokenLoadsNotes() {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"id":1,"title":"Test Note","content":"Test Body"}]""")
                .addHeader("Content-Type", "application/json")
        )
        mockWebServer.start()

        try {
            val viewModel = createMockApiViewModel(mockWebServer, token = "test-token")
            launchScreen(viewModel)

            composeTestRule.waitUntil(5000) {
                composeTestRule
                    .onAllNodesWithText("Test Note")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule
                .onNodeWithText("Test Note")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Test Body")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Всего заметок: 1")
                .assertIsDisplayed()

            val request = mockWebServer.takeRequest()
            assertEquals("Bearer test-token", request.getHeader("Authorization"))
        } finally {
            mockWebServer.shutdown()
        }
    }

    /**
     * SC-006: With token — create note succeeds, note appears in list.
     */
    @Test
    fun sc006_withTokenCreatesNote() {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""{"id":1,"title":"New Note","content":"New Content"}""")
                .addHeader("Content-Type", "application/json")
        )
        mockWebServer.start()

        try {
            val viewModel = createMockApiViewModel(mockWebServer, token = "test-token")
            launchScreen(viewModel)

            composeTestRule.waitUntil(5000) {
                composeTestRule
                    .onAllNodesWithText("Всего заметок: 0")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            viewModel.createNote("New Note", "New Content")

            composeTestRule.waitUntil(5000) {
                composeTestRule
                    .onAllNodesWithText("New Note")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule
                .onNodeWithText("New Note")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("New Content")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Всего заметок: 1")
                .assertIsDisplayed()
        } finally {
            mockWebServer.shutdown()
        }
    }

    /**
     * SC-007: With token — delete note succeeds, note disappears from list.
     */
    @Test
    fun sc007_withTokenDeletesNote() {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"id":1,"title":"To Delete","content":"Delete me"}]""")
                .addHeader("Content-Type", "application/json")
        )
        mockWebServer.enqueue(
            MockResponse().setResponseCode(204)
        )
        mockWebServer.start()

        try {
            val viewModel = createMockApiViewModel(mockWebServer, token = "test-token")
            launchScreen(viewModel)

            composeTestRule.waitUntil(5000) {
                composeTestRule
                    .onAllNodesWithText("To Delete")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule
                .onNodeWithText("To Delete")
                .assertIsDisplayed()

            viewModel.deleteNote(1)

            composeTestRule.waitUntil(5000) {
                composeTestRule
                    .onAllNodesWithText("To Delete")
                    .fetchSemanticsNodes().isEmpty()
            }

            composeTestRule
                .onNodeWithText("To Delete")
                .assertDoesNotExist()
            composeTestRule
                .onNodeWithText("Всего заметок: 0")
                .assertIsDisplayed()
        } finally {
            mockWebServer.shutdown()
        }
    }
}
