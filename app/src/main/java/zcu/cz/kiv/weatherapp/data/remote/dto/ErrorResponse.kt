package zcu.cz.kiv.weatherapp.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val message: String?,
    val code: String?,
    val status: Int?,
    val path: String?,
    val timestamp: String?
)