package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import zcu.cz.kiv.weatherapp.data.WeatherRepository
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse

data class WeatherUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val current: WeatherResponse.Current? = null,
    val daily: List<WeatherResponse.Daily>? = null
)

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WeatherRepository()

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state

    fun load(location: Location) {
        viewModelScope.launch {
            _state.value = WeatherUiState(loading = true)

            runCatching {
                val current = async { repo.current(location.lat, location.lon).current }
                val daily = async { repo.daily(location.lat, location.lon).daily }
                WeatherUiState(
                    loading = false,
                    current = current.await(),
                    daily = daily.await()
                )
            }.onFailure {
                val msg = when (it) {
                    is HttpException -> "Server error: ${it.code()}"
                    else -> it.message ?: "Unknown error"
                }
                _state.value = WeatherUiState(loading = false, error = msg)
            }.onSuccess {
                _state.value = it
            }
        }
    }
}