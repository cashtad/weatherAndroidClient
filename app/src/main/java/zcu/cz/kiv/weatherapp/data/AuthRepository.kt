package zcu.cz.kiv.weatherapp.data

import android.content.Context
import com.squareup.moshi.JsonAdapter
import zcu.cz.kiv.weatherapp.data.local.TokenStore
import zcu.cz.kiv.weatherapp.data.remote.RetrofitClient
import zcu.cz.kiv.weatherapp.data.remote.dto.AuthRequest
import zcu.cz.kiv.weatherapp.data.remote.dto.ErrorResponse
import zcu.cz.kiv.weatherapp.data.remote.safeApiCall

class AuthRepository(context: Context) {
    private val api = RetrofitClient.api
    private val tokenStore = TokenStore(context)

    private val errorAdapter: JsonAdapter<ErrorResponse> = RetrofitClient.errorAdapter


    suspend fun login(email: String, password: String): Result<Unit> = safeApiCall(errorAdapter) {
        val response = api.login(AuthRequest(email, password))
        tokenStore.saveToken(response.token)
    }

    suspend fun register(email: String, password: String): Result<Unit> =
        safeApiCall(errorAdapter) {
            val response = api.register(AuthRequest(email, password))
            tokenStore.saveToken(response.token)
        }

    fun getToken(): String? = tokenStore.getToken()
    fun logout() = tokenStore.clearToken()
}