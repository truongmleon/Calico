package com.calico.launcher

import android.content.Context
import android.content.SharedPreferences

class ScreenLayoutStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calico_screen_layout", Context.MODE_PRIVATE)

    fun loadScreensSwapped(): Boolean = prefs.getBoolean(KEY_SCREENS_SWAPPED, false)

    fun saveScreensSwapped(swapped: Boolean) {
        prefs.edit().putBoolean(KEY_SCREENS_SWAPPED, swapped).apply()
    }

    private companion object {
        const val KEY_SCREENS_SWAPPED = "screens_swapped"
    }
}
