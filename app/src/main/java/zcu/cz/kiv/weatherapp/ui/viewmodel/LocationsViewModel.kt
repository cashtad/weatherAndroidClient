package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.LocationRepository
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto

class LocationsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LocationRepository(app)

    private val _favorites = MutableStateFlow<List<FavoriteLocationResponse>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocationResponse>> = _favorites

    private val _searchResults = MutableStateFlow<List<GeoLocationDto>>(emptyList())
    val searchResults: StateFlow<List<GeoLocationDto>> = _searchResults

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadFavorites() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { repo.getFavorites() }
                .onSuccess { _favorites.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            _loading.value = true
            _error.value = null
            runCatching { repo.search(query) }
                .onSuccess { _searchResults.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun addFavorite(loc: GeoLocationDto, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { repo.addFavorite(loc) }
                .onSuccess {
                    loadFavorites()
                    onDone()
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}