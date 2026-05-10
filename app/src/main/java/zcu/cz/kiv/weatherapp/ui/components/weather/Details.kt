package zcu.cz.kiv.weatherapp.ui.components.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import zcu.cz.kiv.weatherapp.ui.util.formatHour
import zcu.cz.kiv.weatherapp.ui.util.getUvColor
import zcu.cz.kiv.weatherapp.ui.util.getUvDescription
import zcu.cz.kiv.weatherapp.ui.util.getWindDirection
import kotlin.math.roundToInt


@Composable
fun WeatherDetailsGrid(current: WeatherResponse.Current) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                title = stringResource(R.string.feels_like),
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
                title = stringResource(R.string.uv_index),
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
                    text = getUvDescription(context, current.uvi),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                title = stringResource(R.string.wind),
                icon = Icons.Rounded.Air,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.wind_speed, current.windSpeed.roundToInt()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.wind_direction,
                        getWindDirection(context, current.windDeg)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                if (current.windGust != null) {
                    Text(
                        text = stringResource(R.string.wind_gust, current.windGust.roundToInt()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            DetailCard(
                title = stringResource(R.string.humidity),
                icon = Icons.Rounded.WaterDrop,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${current.humidity}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dew_point, current.dewPoint.roundToInt()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                title = stringResource(R.string.sun),
                icon = Icons.Rounded.LightMode,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(
                        R.string.sunrise,
                        current.sunrise?.let { formatHour(it) } ?: "--"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.sunset,
                        current.sunset?.let { formatHour(it) } ?: "--"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            DetailCard(
                title = stringResource(R.string.pressure),
                icon = Icons.Rounded.Compress,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = current.pressure.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.hpa),
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

