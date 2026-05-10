package zcu.cz.kiv.weatherapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import zcu.cz.kiv.weatherapp.ui.screens.AuthScreen
import zcu.cz.kiv.weatherapp.ui.screens.LocationsHubScreen
import zcu.cz.kiv.weatherapp.ui.screens.weather.WeatherDetailScreen
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.LocationsViewModel
import zcu.cz.kiv.weatherapp.ui.viewmodel.WeatherViewModel

sealed class Screen(val route: String) {
    object Hub : Screen("hub")
    object Weather : Screen("weather")
    object Auth : Screen("auth")
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
                authViewModel = authViewModel,
                viewModel = locationsViewModel,
                onLocationClick = { location ->
                    appViewModel.setLocation(location)
                    navController.navigate(Screen.Weather.route)
                },
                onLoginClick = { navController.navigate(Screen.Auth.route) }
            )
        }

        composable(Screen.Weather.route) {
            WeatherDetailScreen(
                appViewModel = appViewModel,
                weatherViewModel = weatherViewModel,
                authViewModel = authViewModel,
                locationsViewModel = locationsViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}