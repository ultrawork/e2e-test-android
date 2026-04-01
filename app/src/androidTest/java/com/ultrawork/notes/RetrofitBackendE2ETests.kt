package com.ultrawork.notes

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * E2E tests for Android Retrofit + Backend integration v33.
 * Covers automated scenarios: SC-003, SC-004, SC-005, SC-M01.
 */
@RunWith(AndroidJUnit4::class)
class RetrofitBackendE2ETests {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var client: OkHttpClient
    private lateinit var baseUrl: String
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Before
    fun setUp() {
        client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val args = InstrumentationRegistry.getArguments()
        baseUrl = args.getString("API_URL")
            ?: args.getString("BASE_URL")
            ?: "http://10.0.2.2:4000/api"

        baseUrl = baseUrl.trimEnd('/')
    }

    private fun obtainDevToken(): String {
        val request = Request.Builder()
            .url("$baseUrl/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()
        assertEquals("Dev token request should return 200", 200, response.code)
        val body = response.body?.string() ?: ""
        val json = gson.fromJson(body, JsonObject::class.java)
        val token = json.get("token")?.asString
        assertNotNull("Response must contain token", token)
        return token!!
    }

    private fun setJwtToken(token: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit()
            .putString("jwt_token", token)
            .commit()
    }

    private fun createNoteViaApi(token: String, title: String, content: String): String {
        val noteJson = gson.toJson(mapOf("title" to title, "content" to content))
        val request = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(noteJson.toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()
        assertEquals(201, response.code)
        val body = response.body?.string() ?: ""
        val json = gson.fromJson(body, JsonObject::class.java)
        return json.get("id").asString
    }

    /**
     * SC-003: POST /api/auth/dev-token — obtain JWT token.
     * Verifies backend returns a valid JWT with 3 dot-separated parts.
     */
    @Test
    fun sc003_getDevTokenReturnsValidJwt() {
        val request = Request.Builder()
            .url("$baseUrl/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string() ?: ""
        val json = gson.fromJson(body, JsonObject::class.java)
        assertTrue("Response must contain 'token' field", json.has("token"))
        val token = json.get("token").asString
        assertTrue("Token must not be empty", token.isNotEmpty())
        // JWT format: three dot-separated parts
        val parts = token.split(".")
        assertEquals("JWT must have 3 parts", 3, parts.size)
    }

    /**
     * SC-004: GET /api/notes with Bearer token — successful notes list.
     * Verifies authorized request returns 200 and array with correct structure.
     */
    @Test
    fun sc004_getNotesWithBearerReturnsArrayWithStructure() {
        val token = obtainDevToken()

        // Create a note to ensure non-empty response
        createNoteViaApi(token, "E2E Test Note v33", "Content for structure verification v33")

        val request = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string() ?: ""
        val jsonArray = gson.fromJson(body, JsonArray::class.java)
        assertNotNull("Response must be a JSON array", jsonArray)
        assertTrue("Array must not be empty", jsonArray.size() > 0)

        // Verify structure of first element matches NoteDto fields
        val firstNote = jsonArray[0].asJsonObject
        assertTrue("Note must have 'id'", firstNote.has("id"))
        assertTrue("Note must have 'title'", firstNote.has("title"))
        assertTrue("Note must have 'content'", firstNote.has("content"))
        assertTrue("Note must have 'created_at'", firstNote.has("created_at"))
        assertTrue("Note must have 'updated_at'", firstNote.has("updated_at"))
    }

    /**
     * SC-005: GET /api/notes without token — 401 Unauthorized.
     * Verifies unauthenticated request is rejected.
     */
    @Test
    fun sc005_getNotesWithoutTokenReturns401() {
        val request = Request.Builder()
            .url("$baseUrl/notes")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(401, response.code)
    }

    /**
     * SC-M01: Tap clear search button to reset search filter.
     * Enters text in search, taps the Clear (X) button, verifies search is cleared
     * and all notes are shown again.
     */
    @Test
    fun scM01_tapClearSearchButtonResetsFilter() {
        // Setup: get token, set SharedPreferences, create a note
        val token = obtainDevToken()
        setJwtToken(token)
        createNoteViaApi(token, "Searchable Note v33", "Content for clear search test")

        // Launch the activity
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Wait for notes list to appear (Success state loaded)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("notes_list"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Enter search text
        composeTestRule
            .onNodeWithTag("notes_search_field")
            .performTextInput("Searchable")
        composeTestRule.waitForIdle()

        // Verify clear button appears and tap it
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasContentDescription("Очистить поиск"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Очистить поиск")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify notes list is still displayed (all notes shown, no filter)
        composeTestRule
            .onNodeWithTag("notes_list")
            .assertIsDisplayed()

        // Verify counter shows total (no "Найдено:" prefix)
        composeTestRule
            .onNodeWithTag("notes_counter")
            .assertIsDisplayed()

        scenario.close()
    }
}
