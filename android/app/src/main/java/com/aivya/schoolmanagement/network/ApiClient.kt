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
 * Base URL for the Spring Boot backend, deployed on Render's free tier.
 * The free instance spins down after inactivity -- the first request after
 * a while can take up to ~50s to wake it back up, which NetworkResult's
 * 10s connect/read timeout will surface as a clear "took too long" message
 * rather than hanging silently.
 */
const val API_BASE_URL = "https://aivya-school-management-application.onrender.com/"

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
            // Generous timeout: the free Render instance can take ~50s to wake
            // up from a cold start after sitting idle.
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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
