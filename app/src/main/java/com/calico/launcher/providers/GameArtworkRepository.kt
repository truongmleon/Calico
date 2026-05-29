package com.calico.launcher.providers

import android.util.Log
import com.calico.launcher.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class GameArtworkRepository(
    private val steamGridDbClient: SteamGridDbClient = SteamGridDbClient(),
    private val screenScraperClient: ScreenScraperClient = ScreenScraperClient(),
    private val retroAchievementsClient: RetroAchievementsClient = RetroAchievementsClient(),
) {
    suspend fun loadArtwork(game: Game, credentials: ProviderCredentials): GameArtwork =
        coroutineScope {
            val requests = buildList {
                if (credentials.hasSteamGridDb) {
                    add(async { runProvider("SteamGridDB") { steamGridDbClient.findArtwork(game.name, credentials.steamGridDbApiKey) } })
                }
                if (credentials.hasScreenScraper) {
                    add(async { runProvider("ScreenScraper") { screenScraperClient.findArtwork(game, credentials) } })
                }
                if (credentials.hasRetroAchievements) {
                    add(async { runProvider("RetroAchievements") { retroAchievementsClient.findGameProgress(game, credentials) } })
                }
            }

            val results = requests.awaitAll().filterNotNull()
            merge(results)
        }

    suspend fun testConnections(credentials: ProviderCredentials): ProviderConnectionStatus =
        withContext(Dispatchers.IO) {
            ProviderConnectionStatus(
                steamGridDb = runConnectionTest("SteamGridDB") { steamGridDbClient.test(credentials.steamGridDbApiKey) },
                screenScraper = runConnectionTest("ScreenScraper") { screenScraperClient.test(credentials) },
                retroAchievements = runConnectionTest("RetroAchievements") { retroAchievementsClient.test(credentials) },
            )
        }

    suspend fun loadRetroAchievements(game: Game, credentials: ProviderCredentials): RetroAchievementsSummary =
        try {
            retroAchievementsClient.findAchievements(game, credentials)
        } catch (error: Exception) {
            Log.w("GameArtworkRepository", "RetroAchievements achievements request failed", error)
            RetroAchievementsSummary(
                gameId = null,
                gameTitle = game.name,
                message = error.message?.takeIf { it.isNotBlank() }?.let { "Failed: $it" } ?: "Failed",
                sourceGameId = game.id,
                platformName = game.platform.name,
            )
        }

    private suspend fun runConnectionTest(name: String, block: suspend () -> String): String =
        try {
            block()
        } catch (error: Exception) {
            Log.w("GameArtworkRepository", "$name test failed", error)
            error.message?.takeIf { it.isNotBlank() }?.let { "Failed: $it" } ?: "Failed"
        }

    private suspend fun <T> runProvider(name: String, block: suspend () -> T): T? =
        try {
            block()
        } catch (error: Exception) {
            Log.w("GameArtworkRepository", "$name request failed", error)
            null
        }

    private fun merge(results: List<GameArtwork>): GameArtwork {
        if (results.isEmpty()) return GameArtwork(sourceSummary = "No credentials configured")

        return GameArtwork(
            heroUrl = results.firstNotNullOfOrNull { it.heroUrl },
            iconUrl = results.firstNotNullOfOrNull { it.iconUrl },
            screenshotUrl = results.firstNotNullOfOrNull { it.screenshotUrl },
            retroAchievementsId = results.firstNotNullOfOrNull { it.retroAchievementsId },
            sourceSummary = results.joinToString(" | ") { it.sourceSummary },
        )
    }
}
