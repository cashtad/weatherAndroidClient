package zcu.cz.kiv.weatherapp.data

import zcu.cz.kiv.weatherapp.data.remote.RetrofitClient
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.data.remote.safeApiCall

class WeatherRepository {
    private val api = RetrofitClient.api

    suspend fun current(lat: Double, lon: Double): Result<WeatherResponse> =
        safeApiCall { api.current(lat, lon) }

    suspend fun hourly(lat: Double, lon: Double): Result<WeatherResponse> =
        safeApiCall { api.hourly(lat, lon) }

    suspend fun daily(lat: Double, lon: Double): Result<WeatherResponse> =
        safeApiCall { api.daily(lat, lon) }
}