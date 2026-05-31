package com.calico.launcher

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the bottom-screen wallpaper URI to SharedPreferences so it
 * survives app restarts.
 */
class WallpaperStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calico_wallpaper", Context.MODE_PRIVATE)

    fun load(): String? = prefs.getString(KEY, null)

    fun save(uri: String) {
        prefs.edit().putString(KEY, uri).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "wallpaper_uri_v1"
    }
}
