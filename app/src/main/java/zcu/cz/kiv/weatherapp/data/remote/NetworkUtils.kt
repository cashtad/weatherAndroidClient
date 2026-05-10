package zcu.cz.kiv.weatherapp.data.remote

import com.squareup.moshi.JsonAdapter
import retrofit2.HttpException
import zcu.cz.kiv.weatherapp.data.remote.dto.ErrorResponse
import java.io.IOException

class AppException(
    override val message: String
) : RuntimeException(message)

suspend inline fun <T> safeApiCall(
    errorAdapter: JsonAdapter<ErrorResponse>,
    crossinline call: suspend () -> T
): Result<T> {
    return try {
        Result.success(call())
    } catch (t: Throwable) {
        val message = t.userMessage(errorAdapter)
        Result.failure(AppException(message))
    }
}

fun Throwable.userMessage(errorAdapter: JsonAdapter<ErrorResponse>): String =
    when (this) {

        is HttpException -> {
            val errorBody = response()?.errorBody()?.string()

            val apiError = runCatching {
                errorBody?.let { errorAdapter.fromJson(it) }
            }.getOrNull()

            apiError?.message ?: "Unexpected server error"
        }

        is IOException -> "Unable to connect to the server"

        else -> message ?: "Unknown error"
    }