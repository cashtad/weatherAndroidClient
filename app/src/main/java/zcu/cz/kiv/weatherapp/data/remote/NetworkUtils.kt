package zcu.cz.kiv.weatherapp.data.remote

import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeApiCall(crossinline call: suspend () -> T): Result<T> =
    runCatching { call() }

fun Throwable.userMessage(): String = when (this) {
    is HttpException -> "Server error: ${code()}"
    is IOException -> "Unable to connect to the server"
    else -> message ?: "Unknown error"
}