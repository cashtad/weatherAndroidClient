package zcu.cz.kiv.weatherapp.ui.components.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.data.remote.dto.GeoLocationDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailTopBar(
    title: String,
    isFavorite: Boolean,
    showFavoriteIcon: Boolean,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            if (showFavoriteIcon) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = null
                    )
                }
            }
        }
    )
}