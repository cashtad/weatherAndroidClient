package zcu.cz.kiv.weatherapp.ui.util

import zcu.cz.kiv.weatherapp.data.model.Location

fun locationFromCoords(lat: Double, lon: Double): Location {
    return Location(
        name = "Текущая локация",
        country = null,
        state = null,
        lat = lat,
        lon = lon,
        displayName = "Текущая локация",
        isFromGps = true
    )
}