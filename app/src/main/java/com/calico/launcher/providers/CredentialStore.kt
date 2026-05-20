package com.calico.launcher.providers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialStore(context: Context) {
    private val preferences: SharedPreferences = createPreferences(context.applicationContext)

    fun load(): ProviderCredentials =
        ProviderCredentials(
            steamGridDbApiKey = preferences.getString(KEY_STEAMGRIDDB_API_KEY, "").orEmpty(),
            screenScraperDeveloperId = preferences.getString(KEY_SCREENSCRAPER_DEVELOPER_ID, "").orEmpty(),
            screenScraperDeveloperPassword = preferences.getString(KEY_SCREENSCRAPER_DEVELOPER_PASSWORD, "").orEmpty(),
            screenScraperUserId = preferences.getString(KEY_SCREENSCRAPER_USER_ID, "").orEmpty(),
            screenScraperUserPassword = preferences.getString(KEY_SCREENSCRAPER_USER_PASSWORD, "").orEmpty(),
            retroAchievementsUsername = preferences.getString(KEY_RETROACHIEVEMENTS_USERNAME, "").orEmpty(),
            retroAchievementsApiKey = preferences.getString(KEY_RETROACHIEVEMENTS_API_KEY, "").orEmpty(),
        )

    fun save(credentials: ProviderCredentials) {
        preferences.edit()
            .putString(KEY_STEAMGRIDDB_API_KEY, credentials.steamGridDbApiKey.trim())
            .putString(KEY_SCREENSCRAPER_DEVELOPER_ID, credentials.screenScraperDeveloperId.trim())
            .putString(KEY_SCREENSCRAPER_DEVELOPER_PASSWORD, credentials.screenScraperDeveloperPassword)
            .putString(KEY_SCREENSCRAPER_USER_ID, credentials.screenScraperUserId.trim())
            .putString(KEY_SCREENSCRAPER_USER_PASSWORD, credentials.screenScraperUserPassword)
            .putString(KEY_RETROACHIEVEMENTS_USERNAME, credentials.retroAchievementsUsername.trim())
            .putString(KEY_RETROACHIEVEMENTS_API_KEY, credentials.retroAchievementsApiKey.trim())
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun createPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (error: Exception) {
            Log.w("CredentialStore", "Encrypted storage unavailable; using private app storage.", error)
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    private companion object {
        const val FILE_NAME = "provider_credentials"
        const val KEY_STEAMGRIDDB_API_KEY = "steamgriddb_api_key"
        const val KEY_SCREENSCRAPER_DEVELOPER_ID = "screenscraper_developer_id"
        const val KEY_SCREENSCRAPER_DEVELOPER_PASSWORD = "screenscraper_developer_password"
        const val KEY_SCREENSCRAPER_USER_ID = "screenscraper_user_id"
        const val KEY_SCREENSCRAPER_USER_PASSWORD = "screenscraper_user_password"
        const val KEY_RETROACHIEVEMENTS_USERNAME = "retroachievements_username"
        const val KEY_RETROACHIEVEMENTS_API_KEY = "retroachievements_api_key"
    }
}
