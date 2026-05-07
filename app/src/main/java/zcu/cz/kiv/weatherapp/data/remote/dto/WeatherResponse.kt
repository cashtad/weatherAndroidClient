package zcu.cz.kiv.weatherapp.data.remote.dto

import com.squareup.moshi.Json

data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @Json(name = "timezone_offset") val timezoneOffset: Int,
    val current: Current? = null,
    val daily: List<Daily>? = null
) {
    data class Current(
        val dt: Long,
        val sunrise: Long,
        val sunset: Long,
        val temp: Double,
        @Json(name = "feels_like") val feelsLike: Double,
        val pressure: Int,
        val humidity: Int,
        @Json(name = "dew_point") val dewPoint: Double,
        val uvi: Double,
        val clouds: Int,
        val visibility: Int,
        @Json(name = "wind_speed") val windSpeed: Double,
        @Json(name = "wind_deg") val windDeg: Int,
        val weather: List<Weather>
    )

    data class Daily(
        val dt: Long,
        val sunrise: Long,
        val sunset: Long,
        val moonrise: Long,
        val moonset: Long,
        @Json(name = "moon_phase") val moonPhase: Double,
        val summary: String?,
        val temp: Temp,
        @Json(name = "feels_like") val feelsLike: FeelsLike,
        val pressure: Int,
        val humidity: Int,
        @Json(name = "dew_point") val dewPoint: Double,
        @Json(name = "wind_speed") val windSpeed: Double,
        @Json(name = "wind_deg") val windDeg: Int,
        @Json(name = "wind_gust") val windGust: Double?,
        val weather: List<Weather>,
        val clouds: Int,
        val pop: Double,
        val rain: Double?,
        val uvi: Double
    )

    data class Temp(
        val day: Double,
        val min: Double,
        val max: Double,
        val night: Double,
        val eve: Double,
        val morn: Double
    )

    data class FeelsLike(
        val day: Double,
        val night: Double,
        val eve: Double,
        val morn: Double
    )

    data class Weather(
        val id: Int,
        val main: String,
        val description: String,
        val icon: String
    )
}