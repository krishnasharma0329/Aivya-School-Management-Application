package com.aivya.schoolmanagement.network

import com.aivya.schoolmanagement.auth.TokenStore
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Base URL for the Spring Boot backend.
 * - Android emulator: 10.0.2.2 reaches the host machine's localhost.
 * - A real device on the same Wi-Fi: replace with the host's LAN IP.
 * Override by changing this constant before building a release APK.
 */
const val API_BASE_URL = "http://10.0.2.2:8080/"

class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.load()?.token
        val request = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}

object ApiClient {

    @Volatile
    private var retrofit: Retrofit? = null

    fun create(tokenStore: TokenStore): AivyaApi {
        val existing = retrofit
        if (existing != null) return existing.create(AivyaApi::class.java)

        val logging = HttpLoggingInterceptor().apply {
            // Headers only -- never logs the request/response body, so a JWT
            // or password never ends up in device logs.
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val built = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit = built
        return built.create(AivyaApi::class.java)
    }
}

/** Parses the backend's {status, message, timestamp} error body, if present. */
fun okhttp3.ResponseBody?.toApiErrorMessage(fallback: String): String {
    if (this == null) return fallback
    return try {
        Gson().fromJson(this.charStream(), ApiErrorBody::class.java)?.message ?: fallback
    } catch (_: Exception) {
        fallback
    }
}
