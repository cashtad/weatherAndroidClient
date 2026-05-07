package zcu.cz.kiv.weatherapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import zcu.cz.kiv.weatherapp.data.local.LocationStore
import zcu.cz.kiv.weatherapp.data.model.Location

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = LocationStore(app)

    private val _selectedLocation = MutableStateFlow(store.load())
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    fun setLocation(location: Location) {
        _selectedLocation.value = location
        store.save(location)
    }
}