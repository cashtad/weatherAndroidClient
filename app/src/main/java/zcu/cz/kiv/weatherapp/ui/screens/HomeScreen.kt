package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.ui.components.GradientBackground
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selected: Location?,
    onOpenLocations: () -> Unit
) {
    val vm: WeatherViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(selected) { selected?.let { vm.load(it) } }

    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(selected?.displayName ?: "Weather") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    actions = { TextButton(onClick = onOpenLocations) { Text("Locations") } }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                if (selected == null) {
                    Text("Select a location to see weather")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onOpenLocations) { Text("Choose location") }
                    return@Column
                }

                if (state.loading) {
                    CircularProgressIndicator()
                    return@Column
                }

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                WeatherContent(
                    title = selected.displayName,
                    current = state.current,
                    daily = state.daily
                )
            }
        }
    }
}