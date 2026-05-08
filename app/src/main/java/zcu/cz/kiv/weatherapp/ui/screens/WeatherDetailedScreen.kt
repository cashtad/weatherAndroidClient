package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    appViewModel: AppViewModel,
    weatherViewModel: WeatherViewModel,
    onBack: () -> Unit,
    onToggleSave: () -> Unit
) {
    val location by appViewModel.selectedLocation.collectAsState()
    val state by weatherViewModel.state.collectAsState()

    LaunchedEffect(location) {
        location?.let { weatherViewModel.load(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(location?.displayName ?: "Погода") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSave) {
                        Icon(Icons.Rounded.BookmarkBorder, contentDescription = "Сохранить")
                    }
                }
            )
        }
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