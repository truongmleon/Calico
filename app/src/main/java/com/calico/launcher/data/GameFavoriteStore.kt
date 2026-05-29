package com.calico.launcher.data

import android.content.Context

class GameFavoriteStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadOverrides(): Map<Int, Boolean> =
        preferences.all.mapNotNull { (key, value) ->
            val gameId = key.removePrefix(KEY_PREFIX).toIntOrNull() ?: return@mapNotNull null
            val isFavorite = value as? Boolean ?: return@mapNotNull null
            gameId to isFavorite
        }.toMap()

    fun save(gameId: Int, isFavorite: Boolean): Boolean =
        preferences.edit()
            .putBoolean("$KEY_PREFIX$gameId", isFavorite)
            .commit()

    private companion object {
        const val PREFERENCES_NAME = "calico_game_favorites"
        const val KEY_PREFIX = "game_"
    }
}
