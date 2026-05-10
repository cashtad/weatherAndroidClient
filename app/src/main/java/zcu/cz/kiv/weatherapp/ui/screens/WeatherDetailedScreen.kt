package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.R
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
                title = { Text(location?.name ?: "Погода", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (location?.isFromGps != true) {
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
                                            loc.name,
                                            loc.country,
                                            loc.state,
                                            loc.lat,
                                            loc.lon,
                                            loc.displayName
                                        )
                                    ) { scope.launch { snackbarHostState.showSnackbar("Локация сохранена") } }
                                } else {
                                    val fav =
                                        favorites.firstOrNull { it.lat == loc.lat && it.lon == loc.lon }
                                            ?: return@IconButton
                                    locationsViewModel.deleteFavorite(fav.id) {
                                        scope.launch { snackbarHostState.showSnackbar("Удалено из сохранённых") }
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
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = { location?.let { weatherViewModel.load(it) } },
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
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                Text(
                                    text = "Прогноз на 24 часа",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.hourly!!.take(24)) { hour ->
                                        HourlyForecastItem(hour)
                                    }
                                }
                            }
                        }
                    }
                }

                if (!state.daily.isNullOrEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "На неделю",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                state.daily!!.forEach { day ->
                                    DailyForecastRow(day)
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.1f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    state.current?.let { current ->
                        WeatherDetailsGrid(current)
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}


@Composable
fun CurrentWeatherHeader(current: WeatherResponse.Current?, todayMax: Double?, todayMin: Double?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val temp = current?.temp?.roundToInt()?.toString() ?: "--"
        val desc =
            current?.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "—"

        Text(
            text = "$temp°",
            fontSize = 80.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 80.sp
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (todayMax != null && todayMin != null) {
            Text(
                text = "Макс.: ${todayMax.roundToInt()}°, Мин.: ${todayMin.roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HourlyForecastItem(hour: WeatherResponse.Hourly) {
    val iconCode = hour.weather.firstOrNull()?.icon
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = formatHour(hour.dt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        WeatherIcon(
            iconCode = iconCode,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "${hour.temp.roundToInt()}°",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DailyForecastRow(day: WeatherResponse.Daily) {
    val iconCode = day.weather.firstOrNull()?.icon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatDay(day.dt),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        WeatherIcon(
            iconCode = iconCode,
            modifier = Modifier.size(40.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${day.temp.min.roundToInt()}°",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = "${day.temp.max.roundToInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeatherDetailsGrid(current: WeatherResponse.Current) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                title = "Ощущается как",
                icon = Icons.Rounded.Thermostat,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${current.feelsLike.roundToInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            DetailCard(
                title = "УФ-индекс",
                icon = Icons.Rounded.WbSunny,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = current.uvi.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (current.uvi / 11f).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    color = getUvColor(current.uvi),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = getUvDescription(current.uvi),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                title = "Ветер",
                icon = Icons.Rounded.Air,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${current.windSpeed.roundToInt()} м/с",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Направление: ${getWindDirection(current.windDeg)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (current.windGust != null) {
                    Text(
                        text = "Порывы до ${current.windGust.roundToInt()} м/с",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            DetailCard(
                title = "Влажность",
                icon = Icons.Rounded.WaterDrop,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${current.humidity}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Точка росы: ${current.dewPoint.roundToInt()}°",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                title = "Солнце",
                icon = Icons.Rounded.LightMode,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Рассвет: ${current.sunrise?.let { formatHour(it) } ?: "--"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Закат: ${current.sunset?.let { formatHour(it) } ?: "--"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            DetailCard(
                title = "Давление",
                icon = Icons.Rounded.Compress,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${current.pressure}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "гПа",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            content()
        }
    }
}


private fun formatHour(epoch: Long): String =
    Instant.ofEpochSecond(epoch)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

private fun formatDay(epoch: Long): String {
    val date = Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault())
    val today = Instant.now().atZone(ZoneId.systemDefault()).dayOfYear
    return if (date.dayOfYear == today) "Сегодня" else date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
}

private fun getWindDirection(deg: Int): String {
    val directions = arrayOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")
    val index = ((deg / 45.0) + 0.5).toInt() % 8
    return directions[index]
}

private fun getUvDescription(uvi: Double): String = when {
    uvi < 3 -> "Низкий"
    uvi < 6 -> "Умеренный"
    uvi < 8 -> "Высокий"
    uvi < 11 -> "Очень высокий"
    else -> "Экстремальный"
}

@Composable
private fun getUvColor(uvi: Double): Color = when {
    uvi < 3 -> Color(0xFF4CAF50)
    uvi < 6 -> Color(0xFFFFEB3B)
    uvi < 8 -> Color(0xFFFF9800)
    uvi < 11 -> Color(0xFFF44336)
    else -> Color(0xFF9C27B0)
}

@Composable
fun getWeatherIconRes(iconCode: String?): Int {
    return when (iconCode) {
        "01d" -> R.drawable.ic_01d
        "01n" -> R.drawable.ic_01n
        "02d" -> R.drawable.ic_02d
        "02n" -> R.drawable.ic_02n
        "03d" -> R.drawable.ic_03d
        "03n" -> R.drawable.ic_03n
        "04d" -> R.drawable.ic_04d
        "04n" -> R.drawable.ic_04n
        "09d" -> R.drawable.ic_09d
        "09n" -> R.drawable.ic_09n
        "10d" -> R.drawable.ic_10d
        "10n" -> R.drawable.ic_10n
        "11d" -> R.drawable.ic_11d
        "11n" -> R.drawable.ic_11n
        "13d" -> R.drawable.ic_13d
        "13n" -> R.drawable.ic_13n
        "50d" -> R.drawable.ic_50d
        "50n" -> R.drawable.ic_50n
        else -> R.drawable.ic_unknown
    }
}

@Composable
fun WeatherIcon(
    iconCode: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = getWeatherIconRes(iconCode)),
            contentDescription = "Иконка погоды",
            modifier = Modifier.fillMaxSize()
        )
    }
}