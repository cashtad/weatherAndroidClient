package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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
                title = { Text(location?.name ?: "Погода") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (location?.isFromGps == true) {
                        return@TopAppBar
                    }
                    IconButton(
                        onClick = {
                            if (!isLoggedIn) {
                                scope.launch { snackbarHostState.showSnackbar("Войдите, чтобы сохранить") }
                                return@IconButton
                            }

                            val loc = location ?: return@IconButton


                            if (!isFavorite) {
                                locationsViewModel.addFavorite(
                                    GeoLocationDto(
                                        name = loc.name,
                                        country = loc.country,
                                        state = loc.state,
                                        lat = loc.lat,
                                        lon = loc.lon,
                                        displayName = loc.displayName
                                    )
                                ) {
                                    scope.launch { snackbarHostState.showSnackbar("Локация сохранена") }
                                }
                            } else {
                                val fav = favorites.firstOrNull {
                                    it.lat == loc.lat && it.lon == loc.lon
                                } ?: return@IconButton
                                locationsViewModel.deleteFavorite(fav.id) {
                                    scope.launch { snackbarHostState.showSnackbar("Локация убрана из сохранённых") }

                                }
                            }
                        }
                    ) {
                        Icon(
                            if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Сохранить"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = {
                location?.let { weatherViewModel.load(it) }
            },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    CurrentWeatherCard(state.current)
                }

                item { SectionHeader("Почасовой прогноз") }
                items(state.hourly ?: emptyList()) { hour ->
                    HourlyRow(hour)
                }

                item { SectionHeader("На 7 дней") }
                items(state.daily ?: emptyList()) { day ->
                    DailyRow(day)
                }

                item { SectionHeader("Подробности") }
                item {
                    DetailsGrid(state.current)
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(current: WeatherResponse.Current?) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Сейчас", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${current?.temp?.roundToInt() ?: "--"}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = current?.weather?.firstOrNull()?.description ?: "—",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ощущается: ${current?.feelsLike?.roundToInt() ?: "--"}°",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun HourlyRow(hour: WeatherResponse.Hourly) {
    ListItem(
        headlineContent = { Text(formatHour(hour.dt)) },
        supportingContent = { Text(hour.weather.firstOrNull()?.description ?: "") },
        trailingContent = { Text("${hour.temp.roundToInt()}°") }
    )
}

@Composable
private fun DailyRow(day: WeatherResponse.Daily) {
    ListItem(
        headlineContent = { Text(formatDay(day.dt)) },
        supportingContent = { Text(day.weather.firstOrNull()?.description ?: "") },
        trailingContent = {
            Text("${day.temp.min.roundToInt()}° / ${day.temp.max.roundToInt()}°")
        }
    )
}

@Composable
private fun DetailsGrid(current: WeatherResponse.Current?) {
    Column(Modifier.padding(16.dp)) {
        DetailItem("Влажность", "${current?.humidity ?: "--"}%")
        DetailItem("Давление", "${current?.pressure ?: "--"} hPa")
        DetailItem("Ветер", "${current?.windSpeed ?: "--"} м/с")
        DetailItem("Облачность", "${current?.clouds ?: "--"}%")
        DetailItem("UV индекс", "${current?.uvi ?: "--"}")
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatHour(epoch: Long): String =
    Instant.ofEpochSecond(epoch)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

private fun formatDay(epoch: Long): String =
    Instant.ofEpochSecond(epoch)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, d MMM"))