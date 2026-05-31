package com.calico.launcher

import android.content.Context
import android.content.SharedPreferences

data class Bookmark(
    val id: Long,
    val name: String,
    val url: String,
    val emoji: String = "🌐",
)

/**
 * Persists the user's bookmark list to SharedPreferences.
 * Each entry is stored as a pipe-delimited line: id|||name|||url|||emoji
 */
class BookmarkStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calico_bookmarks", Context.MODE_PRIVATE)

    fun load(): List<Bookmark> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return raw.lines().mapNotNull { line ->
            val p = line.split(DELIM, limit = 4)
            if (p.size == 4) {
                Bookmark(
                    id = p[0].toLongOrNull() ?: return@mapNotNull null,
                    name = p[1],
                    url = p[2],
                    emoji = p[3],
                )
            } else null
        }
    }

    fun save(bookmarks: List<Bookmark>) {
        val encoded = bookmarks.joinToString("\n") {
            "${it.id}$DELIM${it.name}$DELIM${it.url}$DELIM${it.emoji}"
        }
        prefs.edit().putString(KEY, encoded).apply()
    }

    companion object {
        private const val KEY = "bookmarks_v1"
        private const val DELIM = "|||"
    }
}
