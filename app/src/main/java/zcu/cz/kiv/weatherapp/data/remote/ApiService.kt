package zcu.cz.kiv.weatherapp.data.remote

import zcu.cz.kiv.weatherapp.data.remote.dto.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    // User
    @GET("user/me")
    suspend fun me(@Header("Authorization") token: String): UserResponse

    // Geo
    @GET("geo/search")
    suspend fun search(@Query("name") name: String): List<GeoLocationDto>

    // Weather
    @GET("weather/current")
    suspend fun current(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherResponse

    @GET("weather/hourly")
    suspend fun hourly(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherResponse

    @GET("weather/daily")
    suspend fun daily(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherResponse

    // Favorites
    @GET("favorites")
    suspend fun favorites(@Header("Authorization") token: String): List<FavoriteLocationResponse>

    @POST("favorites")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteLocationRequest
    ): FavoriteLocationResponse

    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(
        @Header("Authorization") token: String,
        @Path("id") id: String
    )
}