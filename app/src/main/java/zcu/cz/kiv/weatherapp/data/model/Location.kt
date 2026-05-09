package zcu.cz.kiv.weatherapp.data.model


data class Location(
    val name: String,
    val country: String?,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val displayName: String,
    val isFromGps: Boolean
)