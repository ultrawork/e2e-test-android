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
 * E2E API tests for Android v25: Retrofit layer, DI, AuthInterceptor.
 * Verifies the backend API contract that the Android client depends on.
 */
@RunWith(AndroidJUnit4::class)
class NotesApiE2ETests {

    private lateinit var client: OkHttpClient
    private lateinit var baseUrl: String
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

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
            ?: "http://10.0.2.2:4140/api"

        // Ensure baseUrl ends with /api and no trailing slash for consistent path building
        baseUrl = baseUrl.trimEnd('/')
        if (!baseUrl.endsWith("/api")) {
            baseUrl = "$baseUrl/api"
        }
    }

    private fun getDevToken(): String {
        val request = Request.Builder()
            .url("$baseUrl/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()
        assertEquals("dev-token should return 200", 200, response.code)
        val body = gson.fromJson(response.body?.string(), JsonObject::class.java)
        val token = body.get("token")?.asString
        assertNotNull("Response must contain token field", token)
        assertTrue("Token must not be empty", token!!.isNotBlank())
        return token
    }

    /**
     * SC-001: Получение dev-токена авторизации
     * POST /api/auth/dev-token → 200, body contains non-empty JWT token
     */
    @Test
    fun sc001_getDevToken() {
        val request = Request.Builder()
            .url("$baseUrl/auth/dev-token")
            .post("".toRequestBody(jsonMediaType))
            .build()
        val response = client.newCall(request).execute()

        assertEquals("Expected HTTP 200", 200, response.code)

        val body = gson.fromJson(response.body?.string(), JsonObject::class.java)
        assertNotNull("Response body must not be null", body)
        assertTrue("Response must contain 'token' field", body.has("token"))

        val token = body.get("token").asString
        assertTrue("Token must not be empty", token.isNotBlank())
        // JWT tokens have 3 parts separated by dots
        assertEquals("Token should be a valid JWT (3 parts)", 3, token.split(".").size)
    }

    /**
     * SC-002: Получение списка заметок с Bearer-токеном
     * GET /api/notes with Authorization header → 200, JSON array
     */
    @Test
    fun sc002_getNotesWithBearerToken() {
        val token = getDevToken()

        val request = Request.Builder()
            .url("$baseUrl/notes")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        val response = client.newCall(request).execute()

        assertEquals("Expected HTTP 200", 200, response.code)

        val bodyStr = response.body?.string() ?: ""
        val notes = gson.fromJson(bodyStr, JsonArray::class.java)
        assertNotNull("Response must be a JSON array", notes)

        // If array is not empty, verify structure of first element
        if (notes.size() > 0) {
            val note = notes[0].asJsonObject
            assertTrue("Note must have 'id' field", note.has("id"))
            assertTrue("Note must have 'title' field", note.has("title"))
            assertTrue("Note must have 'content' field", note.has("content"))
        }
    }

    /**
     * SC-003: Отказ в доступе к заметкам без токена
     * GET /api/notes without Authorization → 401
     */
    @Test
    fun sc003_getNotesWithoutTokenReturns401() {
        val request = Request.Builder()
            .url("$baseUrl/notes")
            .get()
            .build()
        val response = client.newCall(request).execute()

        assertEquals("Expected HTTP 401 Unauthorized", 401, response.code)

        val body = gson.fromJson(response.body?.string(), JsonObject::class.java)
        assertNotNull("Response body must not be null", body)
        assertTrue("Response must contain 'error' field", body.has("error"))
    }

    /**
     * SC-004: Создание заметки с корректными данными
     * POST /api/notes with valid body → 201, returned note with id/title/content
     */
    @Test
    fun sc004_createNoteWithValidData() {
        val token = getDevToken()

        val noteJson = gson.toJson(mapOf(
            "title" to "Test Note v25",
            "content" to "Test content for E2E"
        ))

        val request = Request.Builder()
            .url("$baseUrl/notes")
            .post(noteJson.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $token")
            .build()
        val response = client.newCall(request).execute()

        assertEquals("Expected HTTP 201 Created", 201, response.code)

        val body = gson.fromJson(response.body?.string(), JsonObject::class.java)
        assertNotNull("Response body must not be null", body)
        assertTrue("Note must have 'id' field", body.has("id"))
        assertTrue("Note id must not be empty", body.get("id").asString.isNotBlank())
        assertEquals("Title must match", "Test Note v25", body.get("title").asString)
        assertEquals("Content must match", "Test content for E2E", body.get("content").asString)

        // Cleanup: delete created note
        val noteId = body.get("id").asString
        val deleteRequest = Request.Builder()
            .url("$baseUrl/notes/$noteId")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()
        client.newCall(deleteRequest).execute()
    }

    /**
     * SC-005: Отклонение создания заметки без обязательных полей
     * POST /api/notes with missing content → 400
     */
    @Test
    fun sc005_createNoteWithoutRequiredFieldsReturns400() {
        val token = getDevToken()

        val incompleteJson = gson.toJson(mapOf(
            "title" to "Only Title"
        ))

        val request = Request.Builder()
            .url("$baseUrl/notes")
            .post(incompleteJson.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $token")
            .build()
        val response = client.newCall(request).execute()

        assertEquals("Expected HTTP 400 Bad Request", 400, response.code)

        val body = gson.fromJson(response.body?.string(), JsonObject::class.java)
        assertNotNull("Response body must not be null", body)
        assertTrue("Response must contain 'error' field", body.has("error"))
    }

    /**
     * SC-006: Удаление существующей заметки
     * DELETE /api/notes/{id} → 204, note no longer in list
     */
    @Test
    fun sc006_deleteExistingNote() {
        val token = getDevToken()

        // Create a note to delete
        val noteJson = gson.toJson(mapOf(
            "title" to "To Delete",
            "content" to "Will be removed"
        ))
        val createRequest = Request.Builder()
            .url("$baseUrl/notes")
            .post(noteJson.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $token")
            .build()
        val createResponse = client.newCall(createRequest).execute()
        assertEquals("Create should return 201", 201, createResponse.code)

        val createdNote = gson.fromJson(createResponse.body?.string(), JsonObject::class.java)
        val noteId = createdNote.get("id").asString

        // Delete the note
        val deleteRequest = Request.Builder()
            .url("$baseUrl/notes/$noteId")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()
        val deleteResponse = client.newCall(deleteRequest).execute()

        assertTrue(
            "Expected HTTP 200 or 204 for delete",
            deleteResponse.code == 200 || deleteResponse.code == 204
        )

        // Verify note is no longer in the list
        val listRequest = Request.Builder()
            .url("$baseUrl/notes")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        val listResponse = client.newCall(listRequest).execute()
        val notes = gson.fromJson(listResponse.body?.string(), JsonArray::class.java)

        val deletedNoteExists = (0 until notes.size()).any { i ->
            notes[i].asJsonObject.get("id").asString == noteId
        }
        assertTrue("Deleted note should not appear in list", !deletedNoteExists)
    }

    /**
     * SC-007: Удаление несуществующей заметки
     * DELETE /api/notes/nonexistent-id → 404
     */
    @Test
    fun sc007_deleteNonExistentNoteReturns404() {
        val token = getDevToken()

        val request = Request.Builder()
            .url("$baseUrl/notes/nonexistent-id-00000")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()
        val response = client.newCall(request).execute()

        assertEquals("Expected HTTP 404 Not Found", 404, response.code)

        val body = gson.fromJson(response.body?.string(), JsonObject::class.java)
        assertNotNull("Response body must not be null", body)
        assertTrue("Response must contain 'error' field", body.has("error"))
    }
}
