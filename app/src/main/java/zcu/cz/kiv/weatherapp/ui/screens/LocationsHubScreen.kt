package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SearchBar
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.SnackbarDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsHubScreen(
    appViewModel: AppViewModel,
    viewModel: LocationsViewModel,
    onLocationClick: (Location) -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    val favorites by viewModel.favorites.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isLoggedIn = authViewModel.isLoggedIn()


    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!, duration = SnackbarDuration.Short)
        }
    }

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Локации") }
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
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                if (results.isNotEmpty()) {
                    results.forEach { item ->
                        SearchResultRow(
                            item = item,
                            onAdd = {
                                viewModel.addFavorite(item) {
                                    query = ""
                                    active = false
                                }
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
                item {
                    SectionTitle("Текущая локация")
                }
                item {
                    CurrentLocationCard(
                        onClick = onUseCurrentLocation
                    )
                }

                item {
                    SectionTitle("Сохранённые")
                }

                items(favorites, key = { it.id }) { fav ->
                    FavoriteLocationItem(
                        fav = fav,
                        onClick = {
                            onLocationClick(fav.toLocation())
                        },
                        onDelete = {
                            viewModel.deleteFavorite(fav.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun CurrentLocationCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Текущая локация", style = MaterialTheme.typography.titleMedium)
                Text("Использовать GPS", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun FavoriteLocationItem(
    fav: FavoriteLocationResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    fav.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOfNotNull(fav.state, fav.country).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: GeoLocationDto,
    onAdd: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.displayName) },
        supportingContent = { Text(listOfNotNull(item.state, item.country).joinToString(" · ")) },
        trailingContent = {
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = "Добавить")
            }
        }
    )
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