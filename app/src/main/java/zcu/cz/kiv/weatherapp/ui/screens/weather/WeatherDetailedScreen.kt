package zcu.cz.kiv.weatherapp.ui.screens.weather

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel,
    locationsViewModel: LocationsViewModel,
    weatherViewModel: WeatherViewModel,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val location by appViewModel.selectedLocation.collectAsState()
    val state by weatherViewModel.state.collectAsState()
    val favorites by locationsViewModel.favorites.collectAsState()
    val isLoggedIn = authViewModel.isLoggedIn()

    val isFavorite = favorites.any { it.lat == location?.lat && it.lon == location?.lon }
    val loginToSaveText = stringResource(R.string.login_to_save)
    val locationSavedText = stringResource(R.string.location_saved)
    val locationRemovedText = stringResource(R.string.location_removed)

    LaunchedEffect(location) {
        location?.let { weatherViewModel.load(it) }
    }

    WeatherDetailContent(
        state = state,
        locationName = location?.name ?: stringResource(R.string.weather_title),
        isFavorite = isFavorite,
        isFromGps = location?.isFromGps == true,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRefresh = { location?.let { weatherViewModel.load(it) } },
        onFavoriteToggle = {
            if (!isLoggedIn) {
                scope.launch { snackbarHostState.showSnackbar(loginToSaveText) }
            } else {
                val loc = location ?: return@WeatherDetailContent
                if (!isFavorite) {
                    locationsViewModel.addFavorite(
                        GeoLocationDto(
                            loc.name, loc.country, loc.state,
                            loc.lat, loc.lon, loc.displayName
                        )
                    ) {
                        scope.launch { snackbarHostState.showSnackbar(locationSavedText) }
                    }
                } else {
                    val favId = favorites.find { it.lat == loc.lat && it.lon == loc.lon }?.id
                    favId?.let { id ->
                        locationsViewModel.deleteFavorite(id) {
                            scope.launch { snackbarHostState.showSnackbar(locationRemovedText) }
                        }
                    }
                }
            }
        }
    )
}