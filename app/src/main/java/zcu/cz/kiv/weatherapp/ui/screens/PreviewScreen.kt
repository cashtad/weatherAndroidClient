package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.ui.components.GradientBackground
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel

@Composable
fun PreviewScreen(
    location: Location,
    onAdd: () -> Unit,
    onCancel: () -> Unit
) {
    val weatherVm: WeatherViewModel = viewModel()
    val locationsVm: LocationsViewModel = viewModel()
    val state by weatherVm.state.collectAsState()

    LaunchedEffect(location) { weatherVm.load(location) }

    GradientBackground {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(location.displayName, style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(12.dp))

            if (state.loading) {
                CircularProgressIndicator()
            } else {
                WeatherContent(
                    title = location.displayName,
                    current = state.current,
                    daily = state.daily
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    locationsVm.addFavorite(
                        loc = GeoLocationDto(
                            name = location.name,
                            country = location.country,
                            state = location.state,
                            lat = location.lat,
                            lon = location.lon,
                            displayName = location.displayName
                        ),
                        onDone = onAdd
                    )
                }) { Text("Add") }
            }
        }
    }
}