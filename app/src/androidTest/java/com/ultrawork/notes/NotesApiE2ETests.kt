package com.ultrawork.notes

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.TimeUnit

/**
 * E2E API tests verifying Retrofit integration contract with backend.
 * Covers SC-001..SC-007 from e2e/scenarios/android-notes-api.md
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class NotesApiE2ETests {

    private lateinit var client: OkHttpClient
    private lateinit var baseUrl: String

    companion object {
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private var token: String = ""
        private var createdNoteId: String = ""
    }

    @Before
    fun setUp() {
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val args = InstrumentationRegistry.getArguments()
        baseUrl = args.getString("API_URL")
            ?: args.getString("BASE_URL")
            ?: "http://10.0.2.2:4000"
    }

    /**
     * SC-001: POST /api/auth/dev-token → 200 + JWT
     */
    @Test
    fun test01_sc001_devTokenReturns200WithJwt() {
        val request = Request.Builder()
            .url("$baseUrl/api/auth/dev-token")
            .post("".toRequestBody(null))
            .build()

        val response = client.newCall(request).execute()
        assertEquals("Expected 200 for dev-token", 200, response.code)

        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        assertTrue("Response must contain 'token' field", json.has("token"))

        val jwt = json.getString("token")
        val parts = jwt.split(".")
        assertEquals("JWT must have 3 parts separated by dots", 3, parts.size)
        assertTrue("Each JWT part must be non-empty", parts.all { it.isNotEmpty() })

        // Store token for subsequent tests
        token = jwt
    }

    /**
     * SC-002: GET /api/notes without Authorization → 401
     */
    @Test
    fun test02_sc002_getNotesWithoutTokenReturns401() {
        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals("Expected 401 without auth token", 401, response.code)

        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        assertTrue("Response must contain 'error' field", json.has("error"))
    }

    /**
     * SC-003: GET /api/notes with Bearer token → 200 + correct structure
     */
    @Test
    fun test03_sc003_getNotesWithTokenReturns200AndCorrectStructure() {
        ensureToken()

        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals("Expected 200 with valid token", 200, response.code)

        val body = response.body?.string() ?: ""
        val array = JSONArray(body)

        // If array is not empty, verify structure of each element
        for (i in 0 until array.length()) {
            val note = array.getJSONObject(i)
            assertTrue("Note must have 'id' field", note.has("id"))
            assertTrue("Note must have 'title' field", note.has("title"))
            assertTrue("Note must have 'content' field", note.has("content"))

            // Verify id is a string (not a number)
            val id = note.get("id")
            assertTrue("Note id must be a String", id is String)
        }
    }

    /**
     * SC-004: POST /api/notes — create note → 201
     */
    @Test
    fun test04_sc004_createNoteReturns201WithCorrectFields() {
        ensureToken()

        val payload = JSONObject().apply {
            put("title", "Test Note v23")
            put("content", "Verification content v23")
        }

        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(request).execute()
        assertEquals("Expected 201 for note creation", 201, response.code)

        val body = response.body?.string() ?: ""
        val json = JSONObject(body)

        assertTrue("Created note must have 'id'", json.has("id"))
        assertTrue("Created note must have 'title'", json.has("title"))
        assertTrue("Created note must have 'content'", json.has("content"))

        val id = json.get("id")
        assertTrue("Note id must be a String", id is String)
        assertEquals("Title must match", "Test Note v23", json.getString("title"))
        assertEquals("Content must match", "Verification content v23", json.getString("content"))

        // Store ID for delete test
        createdNoteId = json.getString("id")
    }

    /**
     * SC-005: DELETE /api/notes/:id → 204 No Content
     */
    @Test
    fun test05_sc005_deleteExistingNoteReturns204() {
        ensureToken()

        // Create a note to delete (don't rely on ordering)
        val payload = JSONObject().apply {
            put("title", "Note to delete")
            put("content", "Temporary note for deletion test")
        }
        val createRequest = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val createResponse = client.newCall(createRequest).execute()
        assertEquals("Setup: create note should return 201", 201, createResponse.code)
        val noteId = JSONObject(createResponse.body?.string() ?: "").getString("id")
        assertNotNull("Setup: created note must have id", noteId)

        // Delete the note
        val deleteRequest = Request.Builder()
            .url("$baseUrl/api/notes/$noteId")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val deleteResponse = client.newCall(deleteRequest).execute()
        assertEquals("Expected 204 for successful deletion", 204, deleteResponse.code)
    }

    /**
     * SC-006: POST /api/notes with empty payload → 400
     */
    @Test
    fun test06_sc006_createNoteWithEmptyPayloadReturns400() {
        ensureToken()

        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(request).execute()
        assertEquals("Expected 400 for empty payload", 400, response.code)

        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        assertTrue("Response must contain 'error' field", json.has("error"))
    }

    /**
     * SC-007: DELETE /api/notes/nonexistent-id → 404
     */
    @Test
    fun test07_sc007_deleteNonExistentNoteReturns404() {
        ensureToken()

        val request = Request.Builder()
            .url("$baseUrl/api/notes/nonexistent-id-99999")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val response = client.newCall(request).execute()
        assertEquals("Expected 404 for non-existent note", 404, response.code)

        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        assertTrue("Response must contain 'error' field", json.has("error"))
    }

    /**
     * Ensures a valid token is available for tests that need auth.
     * Fetches one if not already set.
     */
    private fun ensureToken() {
        if (token.isNotEmpty()) return

        val request = Request.Builder()
            .url("$baseUrl/api/auth/dev-token")
            .post("".toRequestBody(null))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: ""
        token = JSONObject(body).getString("token")
    }
}
