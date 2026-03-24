package com.ultrawork.notes.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton-хранилище авторизационного токена в памяти.
 */
@Singleton
class TokenManager @Inject constructor() {

    @Volatile
    private var token: String? = null

    fun getToken(): String? = token

    fun setToken(t: String) {
        token = t
    }
}
