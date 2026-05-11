package zcu.cz.kiv.weatherapp.ui.screens


import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.data.location.LocationProvider
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.ui.components.CurrentLocationCard
import zcu.cz.kiv.weatherapp.ui.components.FavoriteLocationItem
import zcu.cz.kiv.weatherapp.ui.components.GuestLoginCard
import zcu.cz.kiv.weatherapp.ui.components.SearchResultRow
import zcu.cz.kiv.weatherapp.ui.components.SectionTitle
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsHubScreen(
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

    val locationPermissionDeniedText = stringResource(R.string.location_permission_denied)
    val locationPermissionFailedText = stringResource(R.string.location_failed)
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fine || coarse) {
            scope.launch {
                val loc = locationProvider.getCurrentLocation()
                if (loc != null) {
                    val location = Location(
                        name = "Current location",
                        country = null,
                        state = null,
                        lat = loc.latitude,
                        lon = loc.longitude,
                        displayName = "Current location",
                        isFromGps = true
                    )
                    onLocationClick(location)
                } else {
                    snackbarHostState.showSnackbar(locationPermissionFailedText)
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(locationPermissionDeniedText)
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

    val horizontalPadding by animateDpAsState(
        targetValue = if (active) 0.dp else 16.dp,
        label = "searchBarPadding"
    )
    val verticalPadding by animateDpAsState(
        targetValue = if (active) 0.dp else 4.dp,
        label = "searchBarVerticalPadding"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.locations_title),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchBar(
                windowInsets = WindowInsets(0.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = {
                            query = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        onSearch = {
                            focusManager.clearFocus()
                            active = false
                        },
                        expanded = active,
                        onExpandedChange = { active = it },
                        placeholder = { Text(stringResource(R.string.search_location)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (active) {
                                IconButton(onClick = {
                                    if (query.isNotEmpty()) {
                                        query = ""
                                        viewModel.onSearchQueryChanged("")
                                    } else {
                                        active = false
                                        focusManager.clearFocus()
                                    }
                                }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                expanded = active,
                onExpandedChange = { active = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else if (results.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { item ->

                            val isAlreadySaved = favorites.any {
                                it.lat == item.lat && it.lon == item.lon
                            }
                            val loginToSaveText = stringResource(R.string.location_failed)
                            SearchResultRow(
                                item = item,
                                isFavorite = isAlreadySaved,
                                onAdd = {
                                    if (!isLoggedIn) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                loginToSaveText
                                            )
                                        }
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
                                        displayName = item.displayName,
                                        isFromGps = false
                                    )
                                    onLocationClick(location)
                                    active = false
                                    query = ""
                                }
                            )
                        }
                    }
                } else if (query.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_results),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            val undoText = stringResource(R.string.undo)
            val removedText = stringResource(R.string.location_removed)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { SectionTitle(stringResource(R.string.gps_section)) }

                item {
                    CurrentLocationCard(onClick = {
                        locationPermission.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    })
                }

                item { SectionTitle(stringResource(R.string.saved_locations)) }

                if (!isLoggedIn) {
                    item { GuestLoginCard(onLogin = onLoginClick) }
                } else {
                    if (favorites.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_saved_locations),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    } else {

                        items(favorites, key = { it.id }) { fav ->
                            FavoriteLocationItem(
                                fav = fav,
                                modifier = Modifier.animateItem(),
                                onClick = { onLocationClick(fav.toLocation()) },
                                onDelete = {
                                    viewModel.deleteFavoriteWithUndo(fav)

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = removedText,
                                            actionLabel = undoText,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun FavoriteLocationResponse.toLocation(): Location {
    return Location(
        name = name, country = country, state = state, lat = lat, lon = lon,
        displayName = displayName, isFromGps = false
    )
}