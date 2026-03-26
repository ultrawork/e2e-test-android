package com.ultrawork.notes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ultrawork.notes.data.remote.ApiService
import com.ultrawork.notes.data.remote.AuthInterceptor
import com.ultrawork.notes.data.remote.CreateNoteRequest
import com.ultrawork.notes.data.repository.NotesRepositoryImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * API-level E2E tests for Retrofit integration, AuthInterceptor, and NotesRepository.
 * These tests verify the network layer directly without UI, using MockWebServer.
 */
@HiltAndroidTest
class RetrofitApiE2ETests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createApiService(token: String? = null): ApiService {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("notes_prefs_api_test", Context.MODE_PRIVATE)
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
        return retrofit.create(ApiService::class.java)
    }

    /**
     * notes-api SC-001: Request without token returns 401, no Authorization header sent.
     */
    @Test
    fun apiSc001_noTokenReturns401AndNoAuthHeader() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":"Unauthorized"}""")
                .addHeader("Content-Type", "application/json")
        )

        val api = createApiService(token = null)

        try {
            api.getNotes()
            assertTrue("Expected HttpException for 401", false)
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }

        val request = mockWebServer.takeRequest()
        assertNull(
            "Authorization header should not be present without token",
            request.getHeader("Authorization")
        )
        assertEquals("GET", request.method)
        assertTrue(request.path!!.contains("/api/notes"))
    }

    /**
     * notes-api SC-003: Create note with token returns 201 with correct body.
     */
    @Test
    fun apiSc003_createNoteWithTokenReturns201() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""{"id":42,"title":"Test Note","content":"Test Content"}""")
                .addHeader("Content-Type", "application/json")
        )

        val api = createApiService(token = "test-token")
        val result = api.createNote(CreateNoteRequest(title = "Test Note", content = "Test Content"))

        assertEquals(42L, result.id)
        assertEquals("Test Note", result.title)
        assertEquals("Test Content", result.content)

        val request = mockWebServer.takeRequest()
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals("POST", request.method)
        assertTrue(request.path!!.contains("/api/notes"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"title\":\"Test Note\""))
        assertTrue(body.contains("\"content\":\"Test Content\""))
    }

    /**
     * notes-api SC-004: Delete note with token returns 204, then GET confirms removal.
     */
    @Test
    fun apiSc004_deleteNoteWithTokenReturns204AndConfirmsRemoval() = runBlocking {
        // Enqueue DELETE 204
        mockWebServer.enqueue(
            MockResponse().setResponseCode(204)
        )
        // Enqueue GET after delete — empty list
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
        )

        val api = createApiService(token = "test-token")
        val repo = NotesRepositoryImpl(api)

        // Delete note
        repo.deleteNote(1)

        val deleteRequest = mockWebServer.takeRequest()
        assertEquals("DELETE", deleteRequest.method)
        assertTrue(deleteRequest.path!!.contains("/api/notes/1"))
        assertEquals("Bearer test-token", deleteRequest.getHeader("Authorization"))

        // Verify note is gone
        val notes = repo.getNotes()
        assertTrue("Notes list should be empty after deletion", notes.isEmpty())

        val getRequest = mockWebServer.takeRequest()
        assertEquals("GET", getRequest.method)
        assertEquals("Bearer test-token", getRequest.getHeader("Authorization"))
    }
}
