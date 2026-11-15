package com.aivya.schoolmanagement.network

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val isAuthExpired: Boolean = false) : NetworkResult<Nothing>()
}

/**
 * Runs a Retrofit call and turns every outcome -- success, a handled
 * 4xx/5xx body, a timeout, or no connection at all -- into one calm,
 * specific message a teacher can act on, instead of a raw exception.
 */
suspend fun <T> safeApiCall(block: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("The server sent an empty response. Please try again.")
            }
        } else {
            val message = response.errorBody().toApiErrorMessage(
                "Something went wrong (code ${response.code()}). Please try again."
            )
            NetworkResult.Error(
                message = message,
                isAuthExpired = response.code() == 401,
            )
        }
    } catch (e: SocketTimeoutException) {
        NetworkResult.Error("The server took too long to respond. Check your connection and try again.")
    } catch (e: UnknownHostException) {
        NetworkResult.Error("Can't reach the school server. Check your internet connection.")
    } catch (e: IOException) {
        NetworkResult.Error("Network error. Please check your connection and try again.")
    } catch (e: Exception) {
        NetworkResult.Error("Something unexpected happened. Please try again.")
    }
}
