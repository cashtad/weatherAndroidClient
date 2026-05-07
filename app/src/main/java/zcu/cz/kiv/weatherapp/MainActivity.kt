package zcu.cz.kiv.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import zcu.cz.kiv.weatherapp.ui.navigation.AppNav
import zcu.cz.kiv.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme(darkTheme = true) {
                AppNav()
            }
        }
    }
}