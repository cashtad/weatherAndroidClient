package zcu.cz.kiv.weatherapp.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import zcu.cz.kiv.weatherapp.data.model.Location
import zcu.cz.kiv.weatherapp.ui.screens.PreviewScreen
import zcu.cz.kiv.weatherapp.ui.viewmodel.AppViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import zcu.cz.kiv.weatherapp.ui.screens.HomeScreen
import zcu.cz.kiv.weatherapp.ui.screens.LocationsScreen
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val LOCATIONS = "locations"
    const val PREVIEW = "preview"
}

private fun previewRoute(loc: Location): String =
    "preview?name=${Uri.encode(loc.name)}&display=${Uri.encode(loc.displayName)}&lat=${loc.lat}&lon=${loc.lon}" +
            "&country=${Uri.encode(loc.country ?: "")}&state=${Uri.encode(loc.state ?: "")}"

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val appVm: AppViewModel = viewModel()
    val authVm: AuthViewModel = viewModel()

    val start = if (authVm.isLoggedIn()) Routes.HOME else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) { /* без изменений */ }
        composable(Routes.REGISTER) { /* без изменений */ }

        composable(Routes.HOME) {
            HomeScreen(
                selected = appVm.selectedLocation.collectAsState().value,
                onOpenLocations = { navController.navigate(Routes.LOCATIONS) }
            )
        }

        composable(Routes.LOCATIONS) {
            LocationsScreen(
                onSelectFavorite = {
                    appVm.setLocation(it)
                    navController.popBackStack()
                },
                onSelectSearch = { navController.navigate(previewRoute(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "preview?name={name}&display={display}&lat={lat}&lon={lon}&country={country}&state={state}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("display") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lon") { type = NavType.StringType },
                navArgument("country") { type = NavType.StringType },
                navArgument("state") { type = NavType.StringType }
            )
        ) { backStack ->
            val loc = Location(
                name = backStack.arguments?.getString("name") ?: "",
                displayName = backStack.arguments?.getString("display") ?: "",
                lat = backStack.arguments?.getString("lat")?.toDouble() ?: 0.0,
                lon = backStack.arguments?.getString("lon")?.toDouble() ?: 0.0,
                country = backStack.arguments?.getString("country"),
                state = backStack.arguments?.getString("state")
            )

            PreviewScreen(
                location = loc,
                onAdd = { appVm.setLocation(loc); navController.popBackStack(Routes.HOME, false) },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}