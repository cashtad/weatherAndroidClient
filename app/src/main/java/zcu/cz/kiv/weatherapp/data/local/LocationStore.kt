package zcu.cz.kiv.weatherapp.data.local

import android.content.Context
import zcu.cz.kiv.weatherapp.data.model.Location

class LocationStore(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun save(location: Location) {
        prefs.edit()
            .putString("name", location.name)
            .putString("country", location.country)
            .putString("state", location.state)
            .putString("displayName", location.displayName)
            .putFloat("lat", location.lat.toFloat())
            .putFloat("lon", location.lon.toFloat())
            .putBoolean("isFromGps", location.isFromGps)
            .apply()
    }

    fun load(): Location? {
        val name = prefs.getString("name", null) ?: return null
        val lat = prefs.getFloat("lat", 0f)
        val lon = prefs.getFloat("lon", 0f)
        val isFromGps = prefs.getBoolean("isFromGps", false)
        return Location(
            name = name,
            country = prefs.getString("country", null),
            state = prefs.getString("state", null),
            lat = lat.toDouble(),
            lon = lon.toDouble(),
            displayName = prefs.getString("displayName", name) ?: name,
            isFromGps = isFromGps
        )
    }
}