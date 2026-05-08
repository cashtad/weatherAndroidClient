package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.FavoriteLocationResponse
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    onSelectFavorite: (Location) -> Unit,
    onSelectSearch: (Location) -> Unit,
    onBack: () -> Unit
) {
    val vm: LocationsViewModel = viewModel()
    val favorites by vm.favorites.collectAsState()
    val results by vm.searchResults.collectAsState()
    val error by vm.error.collectAsState()

    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadFavorites() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locations") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.onSearchQueryChanged(it)
                },
                label = { Text("Search city") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (results.isNotEmpty()) {
                LazyColumn {
                    items(results) { item ->
                        LocationRow(item.displayName) {
                            onSelectSearch(
                                Location(
                                    name = item.name,
                                    country = item.country,
                                    state = item.state,
                                    lat = item.lat,
                                    lon = item.lon,
                                    displayName = item.displayName
                                )
                            )
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(favorites) { fav ->
                        LocationRow(fav.displayName) {
                            onSelectFavorite(
                                Location(
                                    name = fav.name,
                                    country = fav.country,
                                    state = fav.state,
                                    lat = fav.lat,
                                    lon = fav.lon,
                                    displayName = fav.displayName
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationRow(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Text(title, modifier = Modifier.padding(16.dp))
    }
}