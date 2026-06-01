package com.calico.launcher

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

class EmulationRootStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calico_emulation_root", Context.MODE_PRIVATE)

    fun load(): Uri? = prefs.getString(KEY_ROOT_URI, null)?.let(Uri::parse)

    fun save(rootUri: Uri) {
        prefs.edit().putString(KEY_ROOT_URI, rootUri.toString()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_ROOT_URI).apply()
    }

    private companion object {
        const val KEY_ROOT_URI = "emulation_root_uri"
    }
}
