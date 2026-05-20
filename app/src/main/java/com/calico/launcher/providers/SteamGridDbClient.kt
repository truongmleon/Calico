package com.calico.launcher.providers

import com.calico.launcher.data.HeroSelector
import com.calico.launcher.data.RemoteHero
import org.json.JSONObject

class SteamGridDbClient(
    private val http: HttpJsonClient = HttpJsonClient(),
) {
    suspend fun findArtwork(gameName: String, apiKey: String): GameArtwork {
        val gameId = searchGameId(gameName, apiKey) ?: return GameArtwork(sourceSummary = "SteamGridDB: no match")
        val hero = findHeroUrl(gameId, apiKey)
        val icon = findIconUrl(gameId, apiKey)

        return GameArtwork(
            heroUrl = hero,
            iconUrl = icon,
            sourceSummary = "SteamGridDB game $gameId",
        )
    }

    suspend fun test(apiKey: String): String {
        if (apiKey.isBlank()) return "Missing API key"
        val result = searchGameId("Chrono Trigger", apiKey)
        return if (result != null) "Connected" else "Connected, no Chrono Trigger result"
    }

    private suspend fun searchGameId(gameName: String, apiKey: String): Int? {
        val response = http.getJsonObject(
            "$BASE_URL/search/autocomplete/${gameName.urlEncoded()}",
            authHeaders(apiKey),
        )
        return response.childrenObjects("data").firstOrNull()?.optInt("id")?.takeIf { it > 0 }
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
        return response.childrenObjects("data")
            .firstNotNullOfOrNull { it.optionalImageUrl() }
    }

    private fun authHeaders(apiKey: String): Map<String, String> =
        mapOf("Authorization" to "Bearer $apiKey")

    private fun JSONObject.optionalImageUrl(): String? =
        optionalString("thumb") ?: optionalString("url")

    private companion object {
        const val BASE_URL = "https://www.steamgriddb.com/api/v2"
    }
}
