package zcu.cz.kiv.weatherapp.ui.util

import zcu.cz.kiv.weatherapp.data.model.Location

fun locationFromCoords(lat: Double, lon: Double): Location {
    return Location(
        name = "Current location",
        country = null,
        state = null,
        lat = lat,
        lon = lon,
        displayName = "Current location",
        isFromGps = true
    )
}