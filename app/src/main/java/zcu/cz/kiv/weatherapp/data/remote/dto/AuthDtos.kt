package zcu.cz.kiv.weatherapp.data.remote.dto

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val createdAt: String
)