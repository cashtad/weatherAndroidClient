package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.AuthRepository
import zcu.cz.kiv.weatherapp.data.local.LocationStore
import zcu.cz.kiv.weatherapp.data.model.Location

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(app)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val store = LocationStore(app)

    private val _selectedLocation = MutableStateFlow(store.load())
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    fun setLocation(location: Location) {
        _selectedLocation.value = location
        store.save(location)
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repo.login(email, password)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Login failed" }
            _loading.value = false
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repo.register(email, password)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Register failed" }
            _loading.value = false
        }
    }

    fun isLoggedIn(): Boolean = repo.getToken() != null
}