package com.ultrawork.notes

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * E2E API tests for Android v32 — Retrofit and ViewModel verification.
 *
 * Verifies the backend API contract that the Android app relies on
 * (Retrofit + AuthInterceptor + ViewModel). Scenarios SC-001..SC-007.
 */
@RunWith(AndroidJUnit4::class)
class NotesApiE2EV32Tests {

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

    /**
     * SC-001: POST /api/auth/dev-token — obtain JWT token.
     * Verifies backend returns a valid JWT dev-token with status 200.
     */
    @Test
    fun sc001_postDevTokenReturns200WithToken() {
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
    }

    /**
     * SC-002: GET /api/notes with Bearer token — list notes.
     * Verifies authorized request returns 200 and a JSON array of notes.
     */
    @Test
    fun sc002_getNotesWithBearerReturns200AndJsonArray() {
        val token = obtainDevToken()

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
    }

    /**
     * SC-003: GET /api/notes without token — 401 Unauthorized.
     * Verifies request without Authorization header is rejected.
     */
    @Test
    fun sc003_getNotesWithoutTokenReturns401() {
        val request = Request.Builder()
            .url("$baseUrl/notes")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(401, response.code)
        val body = response.body?.string() ?: ""
        val json = gson.fromJson(body, JsonObject::class.java)
        assertTrue("Response must contain 'error' field", json.has("error"))
    }

    /**
     * SC-004: POST /api/notes — create a note (201 + id).
     * Verifies authorized user can create a note and receives the created entity.
     */
    @Test
    fun sc004_postNoteReturns201WithId() {
        val token = obtainDevToken()

        val noteJson = gson.toJson(mapOf(
            "title" to "Test Note v32",
            "content" to "Test content for E2E v32 verification"
        ))

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
        assertTrue("Response must contain 'id'", json.has("id"))
        assertTrue("Response must contain 'title'", json.has("title"))
        assertTrue("Response must contain 'content'", json.has("content"))
        assertEquals("Test Note v32", json.get("title").asString)
        assertEquals("Test content for E2E v32 verification", json.get("content").asString)
        assertTrue("Response must contain 'createdAt'", json.has("createdAt"))
        assertTrue("Response must contain 'updatedAt'", json.has("updatedAt"))
    }

    /**
     * SC-005: POST /api/notes without content — 400 Bad Request.
     * Verifies backend validates the required 'content' field.
     */
    @Test
    fun sc005_postNoteWithoutContentReturns400() {
        val token = obtainDevToken()

        val noteJson = gson.toJson(mapOf(
            "title" to "Note without content"
        ))

        val request = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(noteJson.toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()

        assertEquals(400, response.code)
    }

    /**
     * SC-006: DELETE /api/notes/{id} — successful deletion (204).
     * Creates a note, then deletes it and verifies 204 response and removal from list.
     */
    @Test
    fun sc006_deleteExistingNoteReturns204() {
        val token = obtainDevToken()

        // Create a note to delete
        val noteJson = gson.toJson(mapOf(
            "title" to "Note to delete v32",
            "content" to "This note will be deleted in E2E v32 test"
        ))

        val createRequest = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(noteJson.toRequestBody(jsonMediaType))
            .build()

        val createResponse = client.newCall(createRequest).execute()
        assertEquals(201, createResponse.code)
        val createBody = createResponse.body?.string() ?: ""
        val createdNote = gson.fromJson(createBody, JsonObject::class.java)
        val noteId = createdNote.get("id").asString

        // Delete the note
        val deleteRequest = Request.Builder()
            .url("$baseUrl/notes/$noteId")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val deleteResponse = client.newCall(deleteRequest).execute()
        assertEquals(204, deleteResponse.code)

        // Verify note is no longer in the list
        val listRequest = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val listResponse = client.newCall(listRequest).execute()
        assertEquals(200, listResponse.code)
        val listBody = listResponse.body?.string() ?: ""
        val notesList = gson.fromJson(listBody, JsonArray::class.java)
        val noteStillExists = (0 until notesList.size()).any {
            notesList.get(it).asJsonObject.get("id").asString == noteId
        }
        assertFalse("Deleted note should not be in the list", noteStillExists)
    }

    /**
     * SC-007: DELETE /api/notes/{id} non-existent note — 404 Not Found.
     * Verifies deleting a non-existent note returns 404.
     */
    @Test
    fun sc007_deleteNonExistentNoteReturns404() {
        val token = obtainDevToken()

        val deleteRequest = Request.Builder()
            .url("$baseUrl/notes/00000000-0000-0000-0000-000000000000")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val deleteResponse = client.newCall(deleteRequest).execute()

        assertEquals(404, deleteResponse.code)
    }
}
