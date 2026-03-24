package com.ultrawork.notes.data.remote

import com.ultrawork.notes.BuildConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor для автоматической авторизации запросов.
 *
 * В debug-режиме при отсутствии токена выполняет синхронный запрос
 * к /auth/dev-token для получения JWT и кеширует его в TokenManager.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val authClient by lazy { OkHttpClient() }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.url.encodedPath.endsWith("auth/dev-token")) {
            return chain.proceed(originalRequest)
        }

        val token = getOrFetchToken()

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private fun getOrFetchToken(): String {
        tokenManager.getToken()?.let { return it }

        synchronized(this) {
            tokenManager.getToken()?.let { return it }

            if (BuildConfig.DEBUG) {
                val request = Request.Builder()
                    .url(BuildConfig.API_BASE_URL + "/auth/dev-token")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .build()

                authClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("Auth request failed: ${response.code}")
                    }
                    val body = response.body?.string()
                        ?: throw IllegalStateException("Empty auth response")
                    val json = JSONObject(body)
                    val token = json.getString("token")
                    tokenManager.setToken(token)
                    return token
                }
            }

            throw IllegalStateException("No auth token available")
        }
    }
}
