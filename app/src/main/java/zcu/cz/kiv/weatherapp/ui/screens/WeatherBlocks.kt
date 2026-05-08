package zcu.cz.kiv.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.ui.util.iconUrl
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeatherContent(
    title: String,
    current: WeatherResponse.Current?,
    daily: List<WeatherResponse.Daily>?,
    hourly: List<WeatherResponse.Hourly>?
) {
    current?.let { SummarySection(it) }

    Spacer(Modifier.height(12.dp))

    hourly?.let { HourlySection(it) }

    Spacer(Modifier.height(12.dp))

    daily?.let { DailySection(it) }

    Spacer(Modifier.height(12.dp))

    current?.let { DetailsSection(it) }
}

@Composable
fun SummarySection(current: WeatherResponse.Current) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = iconUrl(current.weather.firstOrNull()?.icon ?: "01d"),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text("Now", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${current.temp}°",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text("Feels like: ${current.feelsLike}°")
                }
            }
        }
    }
}

private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatHour(ts: Long): String =
    Instant.ofEpochSecond(ts).atZone(ZoneId.systemDefault()).format(hourFormatter)



@Composable
fun HourlySection(hourly: List<WeatherResponse.Hourly>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Next 24 hours", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyRow {
                items(hourly.take(24)) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = zcu.cz.kiv.weatherapp.ui.theme.CardGlass),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(formatHour(item.dt))
                            Text("${item.temp}°")
                        }
                    }
                }
            }
        }
    }
}

private fun dayOfWeek(ts: Long): String =
    Instant.ofEpochSecond(ts)
        .atZone(ZoneId.systemDefault())
        .dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())

@Composable
fun DailySection(daily: List<WeatherResponse.Daily>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("7 days", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            daily.take(7).forEach {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(dayOfWeek(it.dt))
                    Text("${it.temp.min}° / ${it.temp.max}°")
                }
            }
        }
    }
}

@Composable
fun DetailsSection(current: WeatherResponse.Current) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Details", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Wind")
                    Text("${current.windSpeed} m/s")
                }
                Column {
                    Text("Humidity")
                    Text("${current.humidity}%")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pressure")
                    Text("${current.pressure} hPa")
                }
                Column {
                    Text("UV")
                    Text("${current.uvi}")
                }
            }
        }
    }
}