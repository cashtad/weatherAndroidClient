package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.LocationRepository
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.data.remote.userMessage

class LocationsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LocationRepository(app)

    private val _favorites = MutableStateFlow<List<FavoriteLocationResponse>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocationResponse>> = _favorites

    private var recentlyDeleted: FavoriteLocationResponse? = null

    private val searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<GeoLocationDto>>(emptyList())
    val searchResults: StateFlow<List<GeoLocationDto>> = _searchResults

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeSearch()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _error.value = null
            return
        }

        _loading.value = true
        _error.value = null

        repo.search(query)
            .onSuccess { _searchResults.value = it }
            .onFailure { _error.value = it.userMessage() }

        _loading.value = false
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.getFavorites()
                .onSuccess { _favorites.value = it }
                .onFailure { _error.value = it.userMessage() }

            _loading.value = false
        }
    }

    fun addFavorite(loc: GeoLocationDto, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.addFavorite(loc)
                .onSuccess {
                    loadFavorites()
                    onDone()
                }
                .onFailure { _error.value = it.userMessage() }

            _loading.value = false
        }
    }

    fun deleteFavorite(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.deleteFavorite(id)
                .onSuccess {
                    loadFavorites()   // обновляем список
                    onDone()
                }
                .onFailure {
                    _error.value = it.userMessage()
                }

            _loading.value = false
        }
    }

    fun removeFavoriteLocally(fav: FavoriteLocationResponse) {
        recentlyDeleted = fav
        _favorites.value = _favorites.value.filterNot { it.id == fav.id }
    }

    fun confirmDeleteFavorite() {
        val fav = recentlyDeleted ?: return

        viewModelScope.launch {
            repo.deleteFavorite(fav.id)
                .onFailure { _error.value = it.userMessage() }
        }

        recentlyDeleted = null
    }

    fun undoDelete() {
        val fav = recentlyDeleted ?: return
        _favorites.value = listOf(fav) + _favorites.value
        recentlyDeleted = null
    }
}