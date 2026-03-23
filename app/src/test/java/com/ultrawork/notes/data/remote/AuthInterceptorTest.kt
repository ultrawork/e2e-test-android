package com.ultrawork.notes.data.remote

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var interceptor: AuthInterceptor

    @Before
    fun setUp() {
        prefs = mockk()
        interceptor = AuthInterceptor(prefs)
    }

    @Test
    fun `adds Authorization header when token is present`() {
        every { prefs.getString("token", null) } returns "test-token-123"

        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder().url("https://example.com/api/notes").build()
        every { chain.request() } returns request
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        interceptor.intercept(chain)

        verify { chain.proceed(match { it.header("Authorization") == "Bearer test-token-123" }) }
    }

    @Test
    fun `does not add Authorization header when token is null`() {
        every { prefs.getString("token", null) } returns null

        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder().url("https://example.com/api/notes").build()
        every { chain.request() } returns request
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        interceptor.intercept(chain)

        verify { chain.proceed(match { it.header("Authorization") == null }) }
    }

    @Test
    fun `does not add Authorization header when token is blank`() {
        every { prefs.getString("token", null) } returns "  "

        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder().url("https://example.com/api/notes").build()
        every { chain.request() } returns request
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        interceptor.intercept(chain)

        verify { chain.proceed(match { it.header("Authorization") == null }) }
    }
}
