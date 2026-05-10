package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.ui.components.weather.CurrentWeatherHeader
import zcu.cz.kiv.weatherapp.ui.components.weather.DailySection
import zcu.cz.kiv.weatherapp.ui.components.weather.HourlySection
import zcu.cz.kiv.weatherapp.ui.components.weather.WeatherDetailsGrid
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

    val isFavorite = favorites.any {
        it.lat == location?.lat && it.lon == location?.lon
    }

    LaunchedEffect(location) {
        location?.let { weatherViewModel.load(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        location?.name ?: stringResource(R.string.weather_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (location?.isFromGps != true) {
                        val loginToSaveText = stringResource(R.string.login_to_save)
                        val locationSavedText = stringResource(R.string.location_saved)
                        val locationRemovedText = stringResource(R.string.location_removed)
                        IconButton(onClick = {
                            if (!isLoggedIn) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        loginToSaveText
                                    )
                                }
                                return@IconButton
                            }

                            val loc = location ?: return@IconButton

                            if (!isFavorite) {
                                locationsViewModel.addFavorite(
                                    GeoLocationDto(
                                        loc.name, loc.country, loc.state,
                                        loc.lat, loc.lon, loc.displayName
                                    )
                                ) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            locationSavedText
                                        )
                                    }
                                }
                            } else {
                                val fav = favorites.firstOrNull {
                                    it.lat == loc.lat && it.lon == loc.lon
                                } ?: return@IconButton

                                locationsViewModel.deleteFavorite(fav.id) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            locationRemovedText
                                        )
                                    }
                                }
                            }
                        }) {
                            Icon(
                                if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = stringResource(R.string.save)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = { location?.let { weatherViewModel.load(it) } },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    val todayDaily = state.daily?.firstOrNull()
                    CurrentWeatherHeader(
                        current = state.current,
                        todayMax = todayDaily?.temp?.max,
                        todayMin = todayDaily?.temp?.min
                    )
                }

                if (!state.hourly.isNullOrEmpty()) {
                    item { HourlySection(state.hourly!!) }
                }

                if (!state.daily.isNullOrEmpty()) {
                    item { DailySection(state.daily!!) }
                }

                item { state.current?.let { WeatherDetailsGrid(it) } }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}