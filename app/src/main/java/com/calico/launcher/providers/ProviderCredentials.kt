package com.calico.launcher.providers

data class ProviderCredentials(
    val steamGridDbApiKey: String = "",
    val screenScraperDeveloperId: String = "",
    val screenScraperDeveloperPassword: String = "",
    val screenScraperUserId: String = "",
    val screenScraperUserPassword: String = "",
    val retroAchievementsUsername: String = "",
    val retroAchievementsApiKey: String = "",
) {
    val hasSteamGridDb: Boolean = steamGridDbApiKey.isNotBlank()
    val hasScreenScraper: Boolean =
        screenScraperDeveloperId.isNotBlank() &&
            screenScraperDeveloperPassword.isNotBlank() &&
            screenScraperUserId.isNotBlank() &&
            screenScraperUserPassword.isNotBlank()
    val hasRetroAchievements: Boolean =
        retroAchievementsUsername.isNotBlank() && retroAchievementsApiKey.isNotBlank()
}

data class ProviderConnectionStatus(
    val steamGridDb: String = "Not tested",
    val screenScraper: String = "Not tested",
    val retroAchievements: String = "Not tested",
)

data class GameArtwork(
    val heroUrl: String? = null,
    val iconUrl: String? = null,
    val screenshotUrl: String? = null,
    val retroAchievementsId: Int? = null,
    val sourceSummary: String = "Sample placeholder",
)
