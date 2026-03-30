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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * E2E API tests for Android v30 — Notes endpoint verification.
 *
 * Scenarios SC-A01 through SC-A04 from android-notes-api-v30.md.
 * Verifies the backend API contract used by Retrofit + AuthInterceptor + ViewModel.
 */
@RunWith(AndroidJUnit4::class)
class NotesEndpointE2ETests {

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
     * SC-A01: Obtain dev-token and list notes (happy path).
     * 1. POST /api/auth/dev-token → 200 with token.
     * 2. GET /api/notes with Bearer → 200 with JSON array containing note fields.
     */
    @Test
    fun scA01_obtainTokenAndListNotes() {
        // Step 1: obtain dev token
        val tokenRequest = Request.Builder()
            .url("$baseUrl/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()

        val tokenResponse = client.newCall(tokenRequest).execute()
        assertEquals(200, tokenResponse.code)
        val tokenBody = tokenResponse.body?.string() ?: ""
        val tokenJson = gson.fromJson(tokenBody, JsonObject::class.java)
        assertTrue("Response must contain 'token' field", tokenJson.has("token"))
        val token = tokenJson.get("token").asString
        assertTrue("Token must not be empty", token.isNotEmpty())

        // Step 2: list notes with Bearer token
        val notesRequest = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val notesResponse = client.newCall(notesRequest).execute()
        assertEquals(200, notesResponse.code)
        val notesBody = notesResponse.body?.string() ?: ""
        val jsonArray = gson.fromJson(notesBody, JsonArray::class.java)
        assertNotNull("Response must be a JSON array", jsonArray)

        // Verify note structure if array is non-empty
        if (jsonArray.size() > 0) {
            val firstNote = jsonArray[0].asJsonObject
            assertTrue("Note must have 'id'", firstNote.has("id"))
            assertTrue("Note must have 'title'", firstNote.has("title"))
            assertTrue("Note must have 'content'", firstNote.has("content"))
            assertTrue("Note must have 'createdAt'", firstNote.has("createdAt"))
            assertTrue("Note must have 'updatedAt'", firstNote.has("updatedAt"))
        }
    }

    /**
     * SC-A02: Request without authorization — 401.
     * GET /api/notes without Authorization header → 401 Unauthorized.
     */
    @Test
    fun scA02_getNotesWithoutAuthReturns401() {
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
     * SC-A03: Create note and delete it.
     * 1. POST /api/notes with valid body → 201 with id, title, content.
     * 2. DELETE /api/notes/{id} → 204.
     * 3. GET /api/notes → verify deleted note is absent.
     */
    @Test
    fun scA03_createNoteAndDelete() {
        val token = obtainDevToken()

        // Step 1: create a note
        val noteJson = gson.toJson(mapOf(
            "title" to "Test Note v30",
            "content" to "Test content for E2E v30 verification"
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
        assertTrue("Response must contain 'id'", createdNote.has("id"))
        assertTrue("Response must contain 'title'", createdNote.has("title"))
        assertTrue("Response must contain 'content'", createdNote.has("content"))
        assertEquals("Test Note v30", createdNote.get("title").asString)
        assertEquals("Test content for E2E v30 verification", createdNote.get("content").asString)

        val noteId = createdNote.get("id").asString

        // Step 2: delete the note
        val deleteRequest = Request.Builder()
            .url("$baseUrl/notes/$noteId")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val deleteResponse = client.newCall(deleteRequest).execute()
        assertEquals(204, deleteResponse.code)

        // Step 3: verify note is absent from list
        val listRequest = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val listResponse = client.newCall(listRequest).execute()
        assertEquals(200, listResponse.code)
        val listBody = listResponse.body?.string() ?: ""
        val notesArray = gson.fromJson(listBody, JsonArray::class.java)

        val deletedNoteStillPresent = notesArray.any { element ->
            element.asJsonObject.get("id")?.asString == noteId
        }
        assertTrue("Deleted note must not appear in list", !deletedNoteStillPresent)
    }

    /**
     * SC-A04: Validation — create without content (400) + delete non-existent (404).
     * 1. POST /api/notes with missing content → 400.
     * 2. DELETE /api/notes/{non-existent-id} → 404.
     */
    @Test
    fun scA04_validationErrorsAndNotFound() {
        val token = obtainDevToken()

        // Step 1: create note without required 'content' field
        val invalidNoteJson = gson.toJson(mapOf(
            "title" to "Note without content"
        ))

        val createRequest = Request.Builder()
            .url("$baseUrl/notes")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(invalidNoteJson.toRequestBody(jsonMediaType))
            .build()

        val createResponse = client.newCall(createRequest).execute()
        assertEquals(400, createResponse.code)

        // Step 2: delete non-existent note
        val deleteRequest = Request.Builder()
            .url("$baseUrl/notes/00000000-0000-0000-0000-000000000000")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val deleteResponse = client.newCall(deleteRequest).execute()
        assertEquals(404, deleteResponse.code)
        val deleteBody = deleteResponse.body?.string() ?: ""
        val deleteJson = gson.fromJson(deleteBody, JsonObject::class.java)
        assertTrue("Response must contain 'error' field", deleteJson.has("error"))
    }
}
