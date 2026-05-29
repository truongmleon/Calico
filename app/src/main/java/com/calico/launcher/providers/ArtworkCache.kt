package com.calico.launcher.providers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Persists resolved artwork URLs to disk so we don't make repeated API calls on every launch.
 * Keyed by game ID. Stores iconUrl and heroUrl per game.
 */
class ArtworkCache(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun load(gameId: Int): CachedArtwork? {
        val iconUrl = prefs.getString(iconKey(gameId), null)
        val heroUrl = prefs.getString(heroKey(gameId), null)
        val logoUrl = prefs.getString(logoKey(gameId), null)
        return if (iconUrl != null || heroUrl != null || logoUrl != null) {
            CachedArtwork(iconUrl = iconUrl, heroUrl = heroUrl, logoUrl = logoUrl)
        } else {
            null
        }
    }

    fun save(gameId: Int, artwork: GameArtwork) {
        val editor = prefs.edit()
        if (artwork.iconUrl != null) {
            editor.putString(iconKey(gameId), artwork.iconUrl)
        }
        if (artwork.heroUrl != null) {
            editor.putString(heroKey(gameId), artwork.heroUrl)
        }
        if (artwork.logoUrl != null) {
            editor.putString(logoKey(gameId), artwork.logoUrl)
        }
        editor.apply()
        Log.d(TAG, "Cached artwork for gameId=$gameId icon=${artwork.iconUrl} hero=${artwork.heroUrl} logo=${artwork.logoUrl}")
    }

    fun clear() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Artwork cache cleared")
    }

    private fun iconKey(gameId: Int) = "icon_$gameId"
    private fun heroKey(gameId: Int) = "hero_$gameId"
    private fun logoKey(gameId: Int) = "logo_$gameId"

    private companion object {
        const val TAG = "ArtworkCache"
        const val FILE_NAME = "artwork_cache"
    }
}

data class CachedArtwork(
    val iconUrl: String?,
    val heroUrl: String?,
    val logoUrl: String?,
)
