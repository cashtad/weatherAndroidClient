package zcu.cz.kiv.weatherapp.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import zcu.cz.kiv.weatherapp.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

fun formatHour(epoch: Long): String =
    Instant.ofEpochSecond(epoch)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

fun formatDay(context: Context, epoch: Long): String {
    val date = Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault())
    val today = Instant.now().atZone(ZoneId.systemDefault()).dayOfYear

    return if (date.dayOfYear == today)
        context.getString(R.string.today)
    else
        date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
}

fun getUvDescription(context: Context, uvi: Double): String = when {
    uvi < 3 -> context.getString(R.string.uv_low)
    uvi < 6 -> context.getString(R.string.uv_moderate)
    uvi < 8 -> context.getString(R.string.uv_high)
    uvi < 11 -> context.getString(R.string.uv_very_high)
    else -> context.getString(R.string.uv_extreme)
}

fun getWindDirection(context: Context, deg: Int): String {
    val directions = context.resources.getStringArray(R.array.wind_directions)
    val index = ((deg / 45.0) + 0.5).toInt() % 8
    return directions[index]
}

@Composable
fun getUvColor(uvi: Double): Color = when {
    uvi < 3 -> Color(0xFF4CAF50)
    uvi < 6 -> Color(0xFFFFEB3B)
    uvi < 8 -> Color(0xFFFF9800)
    uvi < 11 -> Color(0xFFF44336)
    else -> Color(0xFF9C27B0)
}

