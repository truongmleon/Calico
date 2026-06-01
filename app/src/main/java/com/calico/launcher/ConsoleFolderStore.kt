package com.calico.launcher

import android.content.Context
import android.content.SharedPreferences

class ConsoleFolderStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calico_console_folders", Context.MODE_PRIVATE)

    fun loadEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun saveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    private companion object {
        const val KEY_ENABLED = "console_folders_enabled"
    }
}
