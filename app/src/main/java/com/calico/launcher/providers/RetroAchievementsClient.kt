package com.calico.launcher.providers

import com.calico.launcher.model.Game

data class RetroAchievementsSummary(
    val gameId: Int?,
    val gameTitle: String,
    val message: String,
    val sourceGameId: Int? = null,
    val platformName: String = "",
    val gameIconUrl: String? = null,
    val achievements: List<RetroAchievement> = emptyList(),
) {
    val earnedCount: Int = achievements.count { it.isUnlocked }
    val totalPoints: Int = achievements.sumOf { it.points }
    val earnedPoints: Int = achievements.filter { it.isUnlocked }.sumOf { it.points }
    val completionRatio: Float = if (achievements.isEmpty()) 0f else earnedCount.toFloat() / achievements.size
    val lastEarnedAt: String? = achievements.mapNotNull { it.earnedAt }.maxOrNull()
}

data class RetroAchievement(
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String? = null,
    val earnedAt: String? = null,
) {
    val isUnlocked: Boolean = earnedAt != null
}

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
                "?z=${credentials.retroAchievementsUsername.urlEncoded()}" +
                "&y=${credentials.retroAchievementsApiKey.urlEncoded()}" +
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

    suspend fun findAchievements(game: Game, credentials: ProviderCredentials): RetroAchievementsSummary {
        if (!credentials.hasRetroAchievements) {
            return RetroAchievementsSummary(
                gameId = null,
                gameTitle = game.name,
                message = "RetroAchievements credentials are missing.",
                sourceGameId = game.id,
                platformName = game.platform.name,
            )
        }

        val progress = findGameProgress(game, credentials)
        val gameId = progress.retroAchievementsId
            ?: return RetroAchievementsSummary(
                gameId = null,
                gameTitle = game.name,
                message = progress.sourceSummary,
                sourceGameId = game.id,
                platformName = game.platform.name,
            )

        val response = http.getJsonObject(
            "$BASE_URL/API_GetGameInfoAndUserProgress.php" +
                "?z=${credentials.retroAchievementsUsername.urlEncoded()}" +
                "&y=${credentials.retroAchievementsApiKey.urlEncoded()}" +
                "&u=${credentials.retroAchievementsUsername.urlEncoded()}" +
                "&g=$gameId",
        )
        response.optionalString("Error")?.let { error ->
            return RetroAchievementsSummary(
                gameId = gameId,
                gameTitle = game.name,
                message = "RetroAchievements failed: $error",
                sourceGameId = game.id,
                platformName = game.platform.name,
            )
        }

        val achievements = response.optJSONObject("Achievements")
        val parsedAchievements = buildList {
            if (achievements != null) {
                val keys = achievements.keys()
                while (keys.hasNext()) {
                    val achievement = achievements.optJSONObject(keys.next()) ?: continue
                    add(
                        RetroAchievement(
                            title = achievement.optString("Title", "Untitled achievement"),
                            description = achievement.optString("Description"),
                            points = achievement.optInt("Points"),
                            badgeUrl = achievement.optionalString("BadgeName")?.let { "$MEDIA_URL/Badge/$it.png" },
                            earnedAt = achievement.optionalString("DateEarnedHardcore")
                                ?: achievement.optionalString("DateEarned"),
                        ),
                    )
                }
            }
        }

        val gameTitle = response.optionalString("Title") ?: game.name
        return RetroAchievementsSummary(
            gameId = gameId,
            gameTitle = gameTitle,
            sourceGameId = game.id,
            platformName = game.platform.name,
            gameIconUrl = response.optionalString("ImageIcon")?.let { "$MEDIA_URL/Images/$it" },
            message = if (parsedAchievements.isEmpty()) {
                "RetroAchievements game $gameId is linked, but no achievements were returned."
            } else {
                "Found ${parsedAchievements.count { it.isUnlocked }}/${parsedAchievements.size} achievements."
            },
            achievements = parsedAchievements,
        )
    }

    suspend fun test(credentials: ProviderCredentials): String {
        if (!credentials.hasRetroAchievements) return "Missing RetroAchievements fields"
        val response = http.getJsonObject(
            "$BASE_URL/API_GetUserSummary.php" +
                "?z=${credentials.retroAchievementsUsername.urlEncoded()}" +
                "&y=${credentials.retroAchievementsApiKey.urlEncoded()}" +
                "&u=${credentials.retroAchievementsUsername.urlEncoded()}",
        )
        response.optionalString("Error")?.let { return "Failed: $it" }
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
