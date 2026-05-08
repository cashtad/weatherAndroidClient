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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadFavorites() }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    items(favorites, key = { it.id }) { fav ->

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    vm.removeFavoriteLocally(fav)

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Location deleted",
                                            actionLabel = "UNDO",
                                            duration = SnackbarDuration.Long
                                        )

                                        if (result == SnackbarResult.ActionPerformed) {
                                            vm.undoDelete()
                                        } else {
                                            vm.confirmDeleteFavorite()
                                        }
                                    }
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false, // только свайп влево
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Red)
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            }
                        ) {
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