package zcu.cz.kiv.weatherapp.data

import zcu.cz.kiv.weatherapp.data.remote.RetrofitClient
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.data.remote.safeApiCall

class WeatherRepository {
    private val api = RetrofitClient.api
    suspend fun all(lat: Double, lon: Double): Result<WeatherResponse> =
        safeApiCall { api.all(lat, lon) }
}