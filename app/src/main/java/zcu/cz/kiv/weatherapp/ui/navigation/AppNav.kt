package zcu.cz.kiv.weatherapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import zcu.cz.kiv.weatherapp.ui.screens.LocationsHubScreen
import zcu.cz.kiv.weatherapp.ui.screens.WeatherDetailScreen
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel

sealed class Screen(val route: String) {
    object Hub : Screen("hub")
    object Weather : Screen("weather")
}

@Composable
fun AppNavGraph(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel,
    locationsViewModel: LocationsViewModel,
    weatherViewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Hub.route,
        modifier = modifier
    ) {
        composable(Screen.Hub.route) {
            LocationsHubScreen(
                appViewModel = appViewModel,
                viewModel = locationsViewModel,
                onLocationClick = { location ->
                    appViewModel.setLocation(location)
                    navController.navigate(Screen.Weather.route)
                },
                onUseCurrentLocation = {
                    // сюда привяжешь запрос геолокации и setLocation(...)
                }
            )
        }

        composable(Screen.Weather.route) {
            WeatherDetailScreen(
                appViewModel = appViewModel,
                weatherViewModel = weatherViewModel,
                onBack = { navController.popBackStack() },
                onToggleSave = { /* опционально: добавить/убрать из избранного */ }
            )
        }
    }
}