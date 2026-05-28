package com.calico.launcher.model

import android.net.Uri

enum class GameSort {
    Console,
    Name,
    LastPlayed,
    TotalHours,
    Favorites,
}

enum class GameFileType(val storageName: String, val isLaunchable: Boolean) {
    Base("base", true),
    Update("update", false),
    Dlc("dlc", false),
    Disc("disc", true),
    Patch("patch", false),
    Save("save", false),
    Manual("manual", false);

    companion object {
        fun fromStorageName(value: String): GameFileType =
            entries.firstOrNull { it.storageName == value.lowercase() } ?: Base
    }
}

data class Platform(
    val id: Int,
    val name: String,
    val romFolderName: String,
    val supportedExtensions: List<String>,
    val defaultEmulatorId: Int,
)

data class Emulator(
    val id: Int,
    val displayName: String,
    val packageName: String,
    val installUrl: String,
    val isEnabled: Boolean = true,
)

data class Game(
    val id: Int,
    val platform: Platform,
    val name: String,
    val sortTitle: String,
    val description: String?,
    val releaseDate: String?,
    val developers: List<String>,
    val genres: List<String>,
    val durationSeconds: Int,
    val lastPlayedAt: String?,
    val isFavorite: Boolean,
    val primaryFile: GameFile,
    val files: List<GameFile> = listOf(primaryFile),
    val heroUri: Uri? = null,
    val iconUri: Uri? = null,
) {
    val hoursPlayed: Int = durationSeconds / 3600
}

data class GameFile(
    val id: Int,
    val gameId: Int,
    val platformId: Int,
    val fileType: GameFileType,
    val path: String,
    val name: String,
    val extension: String,
    val contentUri: Uri? = null,
    val region: String? = null,
    val version: String? = null,
    val discNumber: Int? = null,
    val discTotal: Int? = null,
    val crc32: String? = null,
    val md5: String? = null,
    val sha1: String? = null,
    val isPrimary: Boolean = true,
    val isMissing: Boolean = false,
)

data class TaskbarItem(
    val id: Int,
    val name: String,
    val packageName: String?,
    val iconPath: String?,
    val itemType: String,
    val sortOrder: Int,
    val isEnabled: Boolean = true,
)

data class MusicTrack(
    val id: Int,
    val title: String,
    val artist: String?,
    val filePath: String?,
    val contentUri: Uri?,
    val durationSeconds: Int,
    val isFavorite: Boolean,
)

data class UiPreferences(
    val topWallpaperPath: String? = null,
    val bottomWallpaperPath: String? = null,
    val selectedGameId: Int? = null,
    val selectedSort: GameSort = GameSort.Console,
)
