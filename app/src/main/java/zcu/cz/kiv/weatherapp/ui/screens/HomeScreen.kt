package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.ui.components.GradientBackground
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    selected: Location?,
    onOpenLocations: () -> Unit
) {
    val vm: WeatherViewModel = viewModel()
    val state by vm.state.collectAsState()

    val isRefreshing = state.loading
    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            selected?.let { vm.load(it) }
        }
    )

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
            Box(
                modifier = Modifier
                    .padding(padding)
                    .pullRefresh(refreshState)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selected == null) {
                        Text("Select a location to see weather")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onOpenLocations) { Text("Choose location") }
                        return@Column
                    }

                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    WeatherContent(
                        title = selected.displayName,
                        current = state.current,
                        daily = state.daily,
                        hourly = state.hourly
                    )
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = refreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}