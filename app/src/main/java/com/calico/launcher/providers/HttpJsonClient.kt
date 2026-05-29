package com.calico.launcher.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HttpJsonClient {
    suspend fun getJsonObject(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): JSONObject = withContext(Dispatchers.IO) {
        val connection = open(url, headers)
        val body = connection.readBody()
        JSONObject(body)
    }

    suspend fun getJsonArray(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): JSONArray = withContext(Dispatchers.IO) {
        val connection = open(url, headers)
        val body = connection.readBody()
        JSONArray(body)
    }

    private fun open(url: String, headers: Map<String, String>): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "CalicoLauncher/0.1")
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

        android.util.Log.d("HttpJsonClient", "Response code: ${connection.responseCode} for $url")

        if (connection.responseCode !in 200..299) {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            android.util.Log.e("HttpJsonClient", "HTTP ${connection.responseCode}: $error for $url")
            throw IllegalStateException("HTTP ${connection.responseCode}: $error")
        }
        return connection
    }

    private fun HttpURLConnection.readBody(): String =
        inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { it.readText() }
        }
}

fun String.urlEncoded(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

fun String.urlPathEncoded(): String = urlEncoded().replace("+", "%20")
