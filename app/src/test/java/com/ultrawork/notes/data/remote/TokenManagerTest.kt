package com.ultrawork.notes.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenManagerTest {

    @Test
    fun `getToken returns null initially`() {
        val tokenManager = TokenManager()
        assertNull(tokenManager.getToken())
    }

    @Test
    fun `setToken stores token and getToken retrieves it`() {
        val tokenManager = TokenManager()
        tokenManager.setToken("test-jwt-token")
        assertEquals("test-jwt-token", tokenManager.getToken())
    }

    @Test
    fun `setToken overwrites previous token`() {
        val tokenManager = TokenManager()
        tokenManager.setToken("first-token")
        tokenManager.setToken("second-token")
        assertEquals("second-token", tokenManager.getToken())
    }
}
