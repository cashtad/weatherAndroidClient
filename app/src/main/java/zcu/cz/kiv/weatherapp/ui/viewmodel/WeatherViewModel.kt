package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.WeatherRepository
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.data.remote.userMessage

data class WeatherUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val current: WeatherResponse.Current? = null,
    val hourly: List<WeatherResponse.Hourly>? = null,
    val daily: List<WeatherResponse.Daily>? = null
)

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WeatherRepository()

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state

    fun load(location: Location) {
        viewModelScope.launch {
            val previous = _state.value
            _state.value = previous.copy(loading = true, error = null)

            val allDeferred = async { repo.all(location.lat, location.lon) }

            val allResult = allDeferred.await()

            val error = listOf(allResult)
                .mapNotNull { it.exceptionOrNull() }
                .firstOrNull()
                ?.userMessage()

            _state.value = WeatherUiState(
                loading = false,
                error = error,
                current = allResult.getOrNull()?.current ?: previous.current,
                hourly = allResult.getOrNull()?.hourly ?: previous.hourly,
                daily = allResult.getOrNull()?.daily ?: previous.daily
            )
        }
    }
}