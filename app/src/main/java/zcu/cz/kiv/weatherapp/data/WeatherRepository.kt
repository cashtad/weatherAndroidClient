package zcu.cz.kiv.weatherapp.data

import zcu.cz.kiv.weatherapp.data.remote.RetrofitClient
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse

class WeatherRepository {
    private val api = RetrofitClient.api

    suspend fun current(lat: Double, lon: Double): WeatherResponse =
        api.current(lat, lon)

    suspend fun daily(lat: Double, lon: Double): WeatherResponse =
        api.daily(lat, lon)
}