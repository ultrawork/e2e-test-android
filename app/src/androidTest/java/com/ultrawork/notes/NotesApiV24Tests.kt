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
 * E2E v24: Android API contract verification tests.
 * Verifies backend API endpoints that the Android Retrofit client will consume.
 */
@RunWith(AndroidJUnit4::class)
class NotesApiV24Tests {

    private lateinit var client: OkHttpClient
    private lateinit var baseUrl: String
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Before
    fun setUp() {
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val args = InstrumentationRegistry.getArguments()
        baseUrl = args.getString("API_URL")
            ?: args.getString("BASE_URL")
            ?: BuildConfig.API_BASE_URL.removeSuffix("/api")
                .let { if (it.contains("10.0.2.2")) "http://10.0.2.2:4000" else it }
        baseUrl = baseUrl.trimEnd('/')
    }

    private fun getDevToken(): String {
        val request = Request.Builder()
            .url("$baseUrl/api/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()
        assertEquals("Dev token request should return 200", 200, response.code)
        val body = response.body?.string() ?: ""
        val json = gson.fromJson(body, JsonObject::class.java)
        val token = json.get("token")?.asString
        assertNotNull("Token should not be null", token)
        assertTrue("Token should not be empty", token!!.isNotEmpty())
        return token
    }

    /**
     * SC-001: POST /api/auth/dev-token → 200 + JWT token
     */
    @Test
    fun sc001_getDevTokenReturns200WithJwt() {
        val request = Request.Builder()
            .url("$baseUrl/api/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string() ?: ""
        val json = gson.fromJson(body, JsonObject::class.java)
        assertTrue("Response should contain 'token' field", json.has("token"))
        val token = json.get("token").asString
        assertTrue("Token should not be empty", token.isNotEmpty())
    }

    /**
     * SC-002: GET /api/notes without token → 401
     */
    @Test
    fun sc002_getNotesWithoutTokenReturns401() {
        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .get()
            .build()
        val response = client.newCall(request).execute()

        assertEquals(401, response.code)
    }

    /**
     * SC-003: GET /api/notes with valid Bearer token → 200 + array of Note objects
     */
    @Test
    fun sc003_getNotesWithValidTokenReturns200() {
        val token = getDevToken()

        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string() ?: ""
        val array = gson.fromJson(body, JsonArray::class.java)
        assertNotNull("Response should be a JSON array", array)

        // If array is non-empty, verify Note structure (id: string, title, content)
        if (array.size() > 0) {
            val note = array[0].asJsonObject
            assertTrue("Note should have 'id' field", note.has("id"))
            assertTrue("Note should have 'title' field", note.has("title"))
            assertTrue("Note should have 'content' field", note.has("content"))
            // id should be a string (UUID), not a number
            assertTrue(
                "Note.id should be a string",
                note.get("id").isJsonPrimitive && note.get("id").asJsonPrimitive.isString
            )
        }
    }

    /**
     * SC-004: POST /api/notes with valid token and payload → 201 + created Note
     */
    @Test
    fun sc004_createNoteReturns201() {
        val token = getDevToken()

        val payload = JsonObject().apply {
            addProperty("title", "Test Note v24")
            addProperty("content", "Verification content v24")
        }
        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(201, response.code)
        val body = response.body?.string() ?: ""
        val note = gson.fromJson(body, JsonObject::class.java)
        assertTrue("Created note should have 'id'", note.has("id"))
        assertEquals("Test Note v24", note.get("title").asString)
        assertEquals("Verification content v24", note.get("content").asString)
    }

    /**
     * SC-005: DELETE /api/notes/:id with valid token → 204
     */
    @Test
    fun sc005_deleteNoteReturns204() {
        val token = getDevToken()

        // First create a note to delete
        val payload = JsonObject().apply {
            addProperty("title", "Note to delete v24")
            addProperty("content", "This note will be deleted")
        }
        val createRequest = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        val createResponse = client.newCall(createRequest).execute()
        assertEquals(201, createResponse.code)

        val createdNote = gson.fromJson(createResponse.body?.string(), JsonObject::class.java)
        val noteId = createdNote.get("id").asString

        // Delete the created note
        val deleteRequest = Request.Builder()
            .url("$baseUrl/api/notes/$noteId")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()
        val deleteResponse = client.newCall(deleteRequest).execute()

        assertEquals(204, deleteResponse.code)
    }

    /**
     * SC-006: POST /api/notes with empty payload → 400
     */
    @Test
    fun sc006_createNoteWithEmptyPayloadReturns400() {
        val token = getDevToken()

        val emptyPayload = JsonObject()
        val request = Request.Builder()
            .url("$baseUrl/api/notes")
            .header("Authorization", "Bearer $token")
            .post(emptyPayload.toString().toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(400, response.code)
    }

    /**
     * SC-007: DELETE /api/notes/nonexistent-id → 404
     */
    @Test
    fun sc007_deleteNonexistentNoteReturns404() {
        val token = getDevToken()

        val request = Request.Builder()
            .url("$baseUrl/api/notes/nonexistent-id-99999")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()
        val response = client.newCall(request).execute()

        assertEquals(404, response.code)
    }
}
