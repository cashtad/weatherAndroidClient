package zcu.cz.kiv.weatherapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.location.LocationProvider
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.ui.components.CurrentLocationCard
import zcu.cz.kiv.weatherapp.ui.components.FavoriteLocationItem
import zcu.cz.kiv.weatherapp.ui.components.GuestLoginCard
import zcu.cz.kiv.weatherapp.ui.components.SearchResultRow
import zcu.cz.kiv.weatherapp.ui.components.SectionTitle
import zcu.cz.kiv.weatherapp.ui.util.locationFromCoords
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsHubScreen(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel,
    viewModel: LocationsViewModel,
    onLocationClick: (Location) -> Unit,
    onLoginClick: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val isLoggedIn = authViewModel.isLoggedIn()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val locationProvider = remember { LocationProvider(context) }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fine || coarse) {
            scope.launch {
                val loc = locationProvider.getCurrentLocation()
                if (loc != null) {
                    val location = locationFromCoords(loc.latitude, loc.longitude)
                    onLocationClick(location)
                } else {
                    snackbarHostState.showSnackbar("Не удалось получить геолокацию")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Разрешение на геолокацию не выдано")
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadFavorites()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!, duration = SnackbarDuration.Short)
        }
    }

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Локации") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.onSearchQueryChanged(it)
                },
                onSearch = { focusManager.clearFocus(); active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Поиск локации") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (loading) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                if (results.isNotEmpty()) {
                    results.forEach { item ->
                        SearchResultRow(
                            item = item,
                            onAdd = {
                                if (!isLoggedIn) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Войдите, чтобы сохранять локации"
                                        )
                                    }
                                    onLoginClick()
                                } else {
                                    viewModel.addFavorite(item) {
                                        query = ""
                                        active = false
                                    }
                                }
                            },
                            onClick = {
                                val location = Location(
                                    name = item.name,
                                    country = item.country,
                                    state = item.state,
                                    lat = item.lat,
                                    lon = item.lon,
                                    displayName = item.displayName
                                )
                                onLocationClick(location)
                                active = false
                                query = ""
                            }
                        )
                    }
                } else if (query.isNotBlank() && !loading) {
                    Text(
                        "Ничего не найдено",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { SectionTitle("Текущая локация") }
                item { CurrentLocationCard(onClick = { locationPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) }) }

                item { SectionTitle("Сохранённые") }

                if (!isLoggedIn) {
                    item { GuestLoginCard(onLogin = onLoginClick) }
                } else {
                    items(favorites, key = { it.id }) { fav ->
                        FavoriteLocationItem(
                            fav = fav,
                            onClick = { onLocationClick(fav.toLocation()) },
                            onDelete = { viewModel.deleteFavorite(fav.id) }
                        )
                    }
                }
            }
        }
    }
}


private fun FavoriteLocationResponse.toLocation(): Location {
    return Location(
        name = name,
        country = country,
        state = state,
        lat = lat,
        lon = lon,
        displayName = displayName
    )
}