package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.LocationRepository
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto

class LocationsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LocationRepository(app)

    private val _favorites = MutableStateFlow<List<FavoriteLocationResponse>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocationResponse>> = _favorites

    private var pendingDeleteJob: Job? = null

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
            .onFailure { _error.value = it.message }

        _loading.value = false
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.getFavorites()
                .onSuccess { _favorites.value = it }
                .onFailure { _error.value = it.message }

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
                .onFailure { _error.value = it.message }

            _loading.value = false
        }
    }

    fun deleteFavorite(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.deleteFavorite(id)
                .onSuccess {
                    loadFavorites()
                    onDone()
                }
                .onFailure {
                    _error.value = it.message
                }

            _loading.value = false
        }
    }

    fun deleteFavoriteWithUndo(fav: FavoriteLocationResponse) {
        confirmPendingDeletion()

        recentlyDeleted = fav
        _favorites.value = _favorites.value.filter { it.id != fav.id }

        pendingDeleteJob = viewModelScope.launch {
            delay(5000)
            confirmPendingDeletion()
        }
    }

    private fun confirmPendingDeletion() {
        val fav = recentlyDeleted ?: return
        recentlyDeleted = null

        viewModelScope.launch {
            repo.deleteFavorite(fav.id)
                .onFailure {
                    _error.value = "Failed to delete location on server"
                }
        }
    }

    fun undoDelete() {
        pendingDeleteJob?.cancel()
        val restored = recentlyDeleted ?: return

        _favorites.value = (_favorites.value + restored).sortedBy { it.name }
        recentlyDeleted = null
    }
}