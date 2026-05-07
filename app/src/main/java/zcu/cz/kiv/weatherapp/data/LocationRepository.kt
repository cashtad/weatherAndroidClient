package zcu.cz.kiv.weatherapp.data

import android.content.Context
import zcu.cz.kiv.weatherapp.data.local.TokenStore
import zcu.cz.kiv.weatherapp.data.remote.RetrofitClient
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationRequest
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.data.remote.safeApiCall

class LocationRepository(context: Context) {
    private val api = RetrofitClient.api
    private val tokenStore = TokenStore(context)

    private fun authHeader(): String {
        val token = tokenStore.getToken() ?: ""
        return "Bearer $token"
    }

    suspend fun search(name: String): Result<List<GeoLocationDto>> =
        safeApiCall { api.search(name) }

    suspend fun getFavorites(): Result<List<FavoriteLocationResponse>> =
        safeApiCall { api.favorites(authHeader()) }

    suspend fun addFavorite(loc: GeoLocationDto): Result<FavoriteLocationResponse> =
        safeApiCall {
            api.addFavorite(
                authHeader(),
                FavoriteLocationRequest(
                    name = loc.name,
                    country = loc.country,
                    state = loc.state,
                    lat = loc.lat,
                    lon = loc.lon,
                    displayName = loc.displayName
                )
            )
        }

    suspend fun deleteFavorite(id: String): Result<Unit> =
        safeApiCall { api.deleteFavorite(authHeader(), id) }
}