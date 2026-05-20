package com.calico.launcher.providers

import com.calico.launcher.model.Game

class ScreenScraperClient(
    private val http: HttpJsonClient = HttpJsonClient(),
) {
    suspend fun findArtwork(game: Game, credentials: ProviderCredentials): GameArtwork {
        if (!credentials.hasScreenScraper) return GameArtwork(sourceSummary = "ScreenScraper: missing credentials")

        val response = http.getJsonObject(
            "$BASE_URL/jeuInfos.php" +
                "?devid=${credentials.screenScraperDeveloperId.urlEncoded()}" +
                "&devpassword=${credentials.screenScraperDeveloperPassword.urlEncoded()}" +
                "&softname=Calico" +
                "&ssid=${credentials.screenScraperUserId.urlEncoded()}" +
                "&sspassword=${credentials.screenScraperUserPassword.urlEncoded()}" +
                "&output=json" +
                "&romnom=${game.primaryFile.name.urlEncoded()}",
        )

        val urls = response.findImageUrls()
        return GameArtwork(
            heroUrl = urls.firstOrNull { it.contains("fanart", ignoreCase = true) || it.contains("screen", ignoreCase = true) },
            iconUrl = urls.firstOrNull { it.contains("wheel", ignoreCase = true) || it.contains("logo", ignoreCase = true) || it.contains("box", ignoreCase = true) },
            screenshotUrl = urls.firstOrNull { it.contains("screen", ignoreCase = true) } ?: urls.firstOrNull(),
            sourceSummary = "ScreenScraper",
        )
    }

    suspend fun test(credentials: ProviderCredentials): String {
        if (!credentials.hasScreenScraper) return "Missing ScreenScraper fields"
        val response = http.getJsonObject(
            "$BASE_URL/ssuserInfos.php" +
                "?devid=${credentials.screenScraperDeveloperId.urlEncoded()}" +
                "&devpassword=${credentials.screenScraperDeveloperPassword.urlEncoded()}" +
                "&softname=Calico" +
                "&ssid=${credentials.screenScraperUserId.urlEncoded()}" +
                "&sspassword=${credentials.screenScraperUserPassword.urlEncoded()}" +
                "&output=json",
        )
        return if (response.length() > 0) "Connected" else "Connected, empty response"
    }

    private companion object {
        const val BASE_URL = "https://www.screenscraper.fr/api2"
    }
}
