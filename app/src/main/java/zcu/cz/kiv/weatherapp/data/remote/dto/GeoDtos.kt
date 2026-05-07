package zcu.cz.kiv.weatherapp.data.remote.dto

data class GeoLocationDto(
    val name: String,
    val country: String?,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val displayName: String
)