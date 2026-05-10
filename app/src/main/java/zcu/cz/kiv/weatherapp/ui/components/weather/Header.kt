package zcu.cz.kiv.weatherapp.ui.components.weather

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.data.remote.dto.WeatherResponse
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherHeader(current: WeatherResponse.Current?, todayMax: Double?, todayMin: Double?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val temp = current?.temp?.roundToInt()?.toString() ?: "--"
        val desc = current?.weather?.firstOrNull()?.description ?: "—"

        Text("$temp°", fontSize = 80.sp, fontWeight = FontWeight.Light)
        Text(desc.replaceFirstChar { it.uppercase() })

        if (todayMax != null && todayMin != null) {
            Text(
                stringResource(
                    R.string.max_min_temp,
                    todayMax.roundToInt(),
                    todayMin.roundToInt()
                )
            )
        }
    }
}