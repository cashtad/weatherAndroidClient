package zcu.cz.kiv.weatherapp.data.remote.dto

import com.squareup.moshi.Json

data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @Json(name = "timezone_offset") val timezoneOffset: Int,
    val current: Current? = null,
    val minutely: List<Minutely>? = null,
    val hourly: List<Hourly>? = null,
    val daily: List<Daily>? = null,
    val alerts: List<Alert>? = null
) {

    data class Weather(
        val id: Int,
        val main: String,
        val description: String,
        val icon: String
    )

    data class Rain(
        @Json(name = "1h") val oneHour: Double?
    )

    data class Snow(
        @Json(name = "1h") val oneHour: Double?
    )

    data class Current(
        val dt: Long,
        val sunrise: Long? = null,
        val sunset: Long? = null,
        val temp: Double,
        @Json(name = "feels_like") val feelsLike: Double,
        val pressure: Int,
        val humidity: Int,
        @Json(name = "dew_point") val dewPoint: Double,
        val clouds: Int,
        val uvi: Double,
        val visibility: Int? = null,
        @Json(name = "wind_speed") val windSpeed: Double,
        @Json(name = "wind_gust") val windGust: Double? = null,
        @Json(name = "wind_deg") val windDeg: Int,
        val rain: Rain? = null,
        val snow: Snow? = null,
        val weather: List<Weather>
    )

    data class Minutely(
        val dt: Long,
        val precipitation: Double
    )

    data class Hourly(
        val dt: Long,
        val temp: Double,
        @Json(name = "feels_like") val feelsLike: Double,
        val pressure: Int,
        val humidity: Int,
        @Json(name = "dew_point") val dewPoint: Double,
        val uvi: Double,
        val clouds: Int,
        val visibility: Int? = null,
        @Json(name = "wind_speed") val windSpeed: Double,
        @Json(name = "wind_gust") val windGust: Double? = null,
        @Json(name = "wind_deg") val windDeg: Int,
        val pop: Double,
        val rain: Rain? = null,
        val snow: Snow? = null,
        val weather: List<Weather>
    )

    data class Daily(
        val dt: Long,
        val sunrise: Long? = null,
        val sunset: Long? = null,
        val moonrise: Long? = null,
        val moonset: Long? = null,
        @Json(name = "moon_phase") val moonPhase: Double? = null,
        val summary: String? = null,
        val temp: Temp,
        @Json(name = "feels_like") val feelsLike: FeelsLike,
        val pressure: Int,
        val humidity: Int,
        @Json(name = "dew_point") val dewPoint: Double,
        @Json(name = "wind_speed") val windSpeed: Double,
        @Json(name = "wind_gust") val windGust: Double? = null,
        @Json(name = "wind_deg") val windDeg: Int,
        val clouds: Int,
        val uvi: Double,
        val pop: Double,
        val rain: Double? = null,
        val snow: Double? = null,
        val weather: List<Weather>
    )

    data class Alert(
        val sender_name: String
    )

    data class Temp(
        val morn: Double,
        val day: Double,
        val eve: Double,
        val night: Double,
        val min: Double,
        val max: Double
    )

    data class FeelsLike(
        val morn: Double,
        val day: Double,
        val eve: Double,
        val night: Double
    )
}