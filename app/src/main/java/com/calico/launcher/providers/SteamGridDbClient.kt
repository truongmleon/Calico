package com.calico.launcher.providers

import android.util.Log
import com.calico.launcher.data.HeroSelector
import com.calico.launcher.data.RemoteHero
import com.calico.launcher.model.Game
import org.json.JSONObject

class SteamGridDbClient(
    private val http: HttpJsonClient = HttpJsonClient(),
) {
    suspend fun findArtwork(game: Game, apiKey: String): GameArtwork {
        val searchTerms = game.steamGridDbSearchTerms()
        Log.d(TAG, "findArtwork: searching for '${game.name}' with terms: $searchTerms")
        val gameId = searchTerms.firstNotNullOfOrNull { searchGameId(it, game.name, apiKey) }
            ?: run {
                Log.w(TAG, "findArtwork: no SteamGridDB match for '${game.name}'")
                return GameArtwork(sourceSummary = "SteamGridDB: no match for ${searchTerms.firstOrNull().orEmpty()}")
            }
        Log.d(TAG, "findArtwork: found gameId=$gameId for '${game.name}'")
        val hero = runCatching { findHeroUrl(gameId, apiKey) }.getOrNull()
        Log.d(TAG, "findArtwork: heroUrl=$hero for gameId=$gameId")
        // Prefer square grid art (512x512, 1024x1024) — these are proper NxN game art
        // Fall back to portrait grid, then small OS icons as last resort
        val tile = runCatching { findSquareGridUrl(gameId, apiKey) }.getOrNull()
            ?: runCatching { findPosterGridUrl(gameId, apiKey) }.getOrNull()
            ?: runCatching { findIconUrl(gameId, apiKey) }.getOrNull()
        Log.d(TAG, "findArtwork: iconUrl=$tile for gameId=$gameId ('${game.name}')")

        return GameArtwork(
            heroUrl = hero,
            iconUrl = tile,
            sourceSummary = "SteamGridDB game $gameId",
        )
    }

    suspend fun test(apiKey: String): String {
        if (apiKey.isBlank()) return "Missing API key"
        val result = searchGameId("Chrono Trigger", "Chrono Trigger", apiKey)
        return if (result != null) "Connected" else "Connected, no Chrono Trigger result"
    }

    private suspend fun searchGameId(gameName: String, originalTitle: String, apiKey: String): Int? {
        val response = http.getJsonObject(
            "$BASE_URL/search/autocomplete/${gameName.urlPathEncoded()}",
            authHeaders(apiKey),
        )
        val matches = response.childrenObjects("data")
        Log.d(TAG, "searchGameId: '$gameName' returned ${matches.size} results")
        val normalizedTitle = originalTitle.normalizedTitle()
        val normalizedSearch = gameName.normalizedTitle()
        return matches
            .sortedByDescending { match ->
                val matchName = match.optionalString("name").orEmpty().normalizedTitle()
                when {
                    matchName == normalizedTitle -> 5
                    matchName == normalizedSearch -> 4
                    matchName.contains(normalizedTitle) || normalizedTitle.contains(matchName) -> 3
                    matchName.contains(normalizedSearch) || normalizedSearch.contains(matchName) -> 2
                    else -> 1
                }
            }
            .firstNotNullOfOrNull { it.optInt("id").takeIf { id -> id > 0 } }
    }

    private suspend fun findHeroUrl(gameId: Int, apiKey: String): String? {
        val response = http.getJsonObject(
            "$BASE_URL/heroes/game/$gameId?styles=alternate,material&types=static",
            authHeaders(apiKey),
        )
        val candidates = response.childrenObjects("data").mapNotNull { item ->
            val url = item.optionalString("url") ?: return@mapNotNull null
            RemoteHero(
                url = url,
                width = item.optInt("width"),
                height = item.optInt("height"),
            )
        }
        return HeroSelector.chooseBest(candidates)?.url ?: candidates.firstOrNull()?.url
    }

    private suspend fun findIconUrl(gameId: Int, apiKey: String): String? {
        val response = http.getJsonObject("$BASE_URL/icons/game/$gameId", authHeaders(apiKey))
        val items = response.childrenObjects("data")
        Log.d(TAG, "findIconUrl: gameId=$gameId returned ${items.size} icon candidates")
        items.firstOrNull()?.let { first ->
            Log.d(TAG, "findIconUrl: first item url=${first.optionalString("url")} thumb=${first.optionalString("thumb")}")
        }
        return items.firstNotNullOfOrNull { it.optionalImageUrl(skipIco = true) }
    }


    private suspend fun findSquareGridUrl(gameId: Int, apiKey: String): String? =
        findGridUrl(gameId, apiKey, "512x512,1024x1024")

    private suspend fun findPosterGridUrl(gameId: Int, apiKey: String): String? =
        findGridUrl(gameId, apiKey, "600x900,342x482,660x930")

    private suspend fun findGridUrl(gameId: Int, apiKey: String, dimensions: String): String? {
        val response = http.getJsonObject(
            "$BASE_URL/grids/game/$gameId?types=static&dimensions=$dimensions",
            authHeaders(apiKey),
        )
        val grids = response.childrenObjects("data")
        return grids.firstNotNullOfOrNull { item ->
            val width = item.optInt("width")
            val height = item.optInt("height")
            item.optionalImageUrl(skipIco = true).takeIf {
                width == 512 && height == 512 ||
                    width == 1024 && height == 1024 ||
                    width == 600 && height == 900 ||
                    width == 342 && height == 482 ||
                    width == 660 && height == 930
            }
        } ?: grids.firstNotNullOfOrNull { it.optionalImageUrl(skipIco = true) }
    }

    private fun authHeaders(apiKey: String): Map<String, String> =
        mapOf("Authorization" to "Bearer $apiKey")

    private fun JSONObject.optionalImageUrl(skipIco: Boolean): String? =
        listOfNotNull(optionalString("url"), optionalString("thumb"))
            .firstOrNull { url ->
                url.startsWith("https://") &&
                    (!skipIco || !url.substringBefore("?").endsWith(".ico", ignoreCase = true))
            }

    private fun Game.steamGridDbSearchTerms(): List<String> =
        listOf(
            name,
            primaryFile.name.substringBeforeLast('.'),
            sortTitle,
        )
            .flatMap { value -> listOf(value, value.cleanedGameTitle()) }
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinctBy { it.lowercase() }

    private fun String.cleanedGameTitle(): String =
        replace(Regex("\\([^)]*\\)|\\[[^]]*]"), " ")
            .replace(Regex("\\b(usa|europe|japan|world|rev\\s*\\d+|v\\d+(\\.\\d+)*)\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("[_+.]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun String.normalizedTitle(): String =
        cleanedGameTitle()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private companion object {
        const val TAG = "SteamGridDbClient"
        const val BASE_URL = "https://www.steamgriddb.com/api/v2"
    }
}
