package zcu.cz.kiv.weatherapp.data.remote.dto

data class FavoriteLocationRequest(
    val name: String,
    val country: String?,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val displayName: String
)

data class FavoriteLocationResponse(
    val id: String,
    val name: String,
    val country: String?,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val displayName: String
)