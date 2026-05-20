package com.calico.launcher.providers

import com.calico.launcher.model.Game

class RetroAchievementsClient(
    private val http: HttpJsonClient = HttpJsonClient(),
) {
    suspend fun findGameProgress(game: Game, credentials: ProviderCredentials): GameArtwork {
        if (!credentials.hasRetroAchievements) {
            return GameArtwork(sourceSummary = "RetroAchievements: missing credentials")
        }

        val systemId = RETRO_ACHIEVEMENTS_SYSTEM_IDS[game.platform.romFolderName]
            ?: return GameArtwork(sourceSummary = "RetroAchievements: unsupported platform")
        val games = http.getJsonArray(
            "$BASE_URL/API_GetGameList.php" +
                "?y=${credentials.retroAchievementsApiKey.urlEncoded()}" +
                "&i=$systemId" +
                "&f=1",
        )
        val match = games.objects().firstOrNull { item ->
            item.optString("Title").equals(game.name, ignoreCase = true)
        } ?: games.objects().firstOrNull { item ->
            item.optString("Title").contains(game.name, ignoreCase = true) ||
                game.name.contains(item.optString("Title"), ignoreCase = true)
        }

        val gameId = match?.optInt("ID")?.takeIf { it > 0 }
        val badgeName = match?.optionalString("ImageIcon")
        val iconUrl = badgeName?.let { "$MEDIA_URL/Images/$it" }

        return GameArtwork(
            iconUrl = iconUrl,
            retroAchievementsId = gameId,
            sourceSummary = gameId?.let { "RetroAchievements game $it" } ?: "RetroAchievements: no match",
        )
    }

    suspend fun test(credentials: ProviderCredentials): String {
        if (!credentials.hasRetroAchievements) return "Missing RetroAchievements fields"
        val response = http.getJsonObject(
            "$BASE_URL/API_GetUserSummary.php" +
                "?y=${credentials.retroAchievementsApiKey.urlEncoded()}" +
                "&u=${credentials.retroAchievementsUsername.urlEncoded()}",
        )
        return if (response.optionalString("User").isNullOrBlank()) {
            "Connected, user summary unavailable"
        } else {
            "Connected"
        }
    }

    private companion object {
        const val BASE_URL = "https://retroachievements.org/API"
        const val MEDIA_URL = "https://media.retroachievements.org"

        val RETRO_ACHIEVEMENTS_SYSTEM_IDS = mapOf(
            "genesis" to 1,
            "n64" to 2,
            "snes" to 3,
            "gb" to 4,
            "gba" to 5,
            "gbc" to 6,
            "nes" to 7,
            "ps1" to 12,
            "sms" to 11,
            "psp" to 41,
            "saturn" to 39,
            "dreamcast" to 40,
        )
    }
}
