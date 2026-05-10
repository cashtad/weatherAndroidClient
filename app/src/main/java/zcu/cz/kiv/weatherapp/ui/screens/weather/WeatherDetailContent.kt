package zcu.cz.kiv.weatherapp.ui.screens.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zcu.cz.kiv.weatherapp.ui.components.weather.CurrentWeatherHeader
import zcu.cz.kiv.weatherapp.ui.components.weather.DailySection
import zcu.cz.kiv.weatherapp.ui.components.weather.HourlySection
import zcu.cz.kiv.weatherapp.ui.components.weather.WeatherDetailTopBar
import zcu.cz.kiv.weatherapp.ui.components.weather.WeatherDetailsGrid
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailContent(
    state: WeatherUiState,
    locationName: String,
    isFavorite: Boolean,
    isFromGps: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Scaffold(
        topBar = {
            WeatherDetailTopBar(
                title = locationName,
                isFavorite = isFavorite,
                showFavoriteIcon = !isFromGps,
                onBack = onBack,
                onFavoriteClick = onFavoriteToggle
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = onRefresh,
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
                    item { HourlySection(state.hourly) }
                }

                if (!state.daily.isNullOrEmpty()) {
                    item { DailySection(state.daily) }
                }

                item { state.current?.let { WeatherDetailsGrid(it) } }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}