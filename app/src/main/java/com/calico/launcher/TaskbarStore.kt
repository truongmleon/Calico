package com.calico.launcher

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the user's custom taskbar apps to SharedPreferences.
 * Stores a comma-separated list of package names.
 */
class TaskbarStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calico_taskbar", Context.MODE_PRIVATE)

    fun load(): List<String> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun save(packages: List<String>) {
        val encoded = packages.joinToString(",")
        prefs.edit().putString(KEY, encoded).apply()
    }

    companion object {
        private const val KEY = "taskbar_packages_v1"
    }
}
