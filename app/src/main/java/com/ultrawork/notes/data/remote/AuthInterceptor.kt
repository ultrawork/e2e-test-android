package com.ultrawork.notes.data.remote

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val prefs: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString(KEY_JWT_TOKEN, null)
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }

    companion object {
        const val PREFS_NAME = "notes_prefs"
        const val KEY_JWT_TOKEN = "jwt_token"
    }
}
