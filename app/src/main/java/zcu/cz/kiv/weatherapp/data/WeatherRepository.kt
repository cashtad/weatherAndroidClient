package zcu.cz.kiv.weatherapp.data

import com.squareup.moshi.JsonAdapter
import zcu.cz.kiv.weatherapp.data.remote.RetrofitClient
import zcu.cz.kiv.weatherapp.data.remote.dto.ErrorResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.data.remote.safeApiCall

class WeatherRepository {
    private val api = RetrofitClient.api

    private val errorAdapter: JsonAdapter<ErrorResponse> = RetrofitClient.errorAdapter

    suspend fun all(lat: Double, lon: Double): Result<WeatherResponse> =
        safeApiCall(errorAdapter) { api.all(lat, lon) }
}