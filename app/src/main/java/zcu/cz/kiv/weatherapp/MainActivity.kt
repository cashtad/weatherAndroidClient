package zcu.cz.kiv.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import zcu.cz.kiv.weatherapp.ui.navigation.AppNavGraph
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val appViewModel: AppViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val locationsViewModel: LocationsViewModel = viewModel()
            val weatherViewModel: WeatherViewModel = viewModel()

            AppNavGraph(
                appViewModel = appViewModel,
                authViewModel = authViewModel,
                locationsViewModel = locationsViewModel,
                weatherViewModel = weatherViewModel
            )

        }
    }
}