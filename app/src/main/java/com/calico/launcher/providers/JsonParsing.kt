package com.calico.launcher.providers

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.optionalString(name: String): String? =
    optString(name).takeIf { it.isNotBlank() && it != "null" }

fun JSONObject.childrenObjects(name: String): List<JSONObject> {
    val value = opt(name) ?: return emptyList()
    return when (value) {
        is JSONArray -> value.objects()
        is JSONObject -> listOf(value)
        else -> emptyList()
    }
}

fun JSONArray.objects(): List<JSONObject> =
    buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let(::add)
        }
    }

fun JSONObject.findImageUrls(): List<String> =
    buildList {
        collectImageUrls(this@findImageUrls)
    }

private fun MutableList<String>.collectImageUrls(value: Any?) {
    when (value) {
        is JSONObject -> {
            value.keys().forEach { key ->
                collectImageUrls(value.opt(key))
            }
        }
        is JSONArray -> {
            for (index in 0 until value.length()) {
                collectImageUrls(value.opt(index))
            }
        }
        is String -> {
            if (value.startsWith("http") && value.looksLikeImage()) add(value)
        }
    }
}

private fun String.looksLikeImage(): Boolean {
    val clean = substringBefore("?").lowercase()
    return clean.endsWith(".png") ||
        clean.endsWith(".jpg") ||
        clean.endsWith(".jpeg") ||
        clean.endsWith(".webp")
}
