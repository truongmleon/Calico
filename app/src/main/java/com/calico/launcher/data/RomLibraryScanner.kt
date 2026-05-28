package com.calico.launcher.data

import com.calico.launcher.model.GameFileType
import com.calico.launcher.model.Platform
import java.io.File

data class ScannedGame(
    val platform: Platform,
    val title: String,
    val files: List<ScannedGameFile>,
) {
    val primaryFile: ScannedGameFile? = files.firstOrNull { it.isPrimary }
}

data class ScannedGameFile(
    val file: File,
    val fileType: GameFileType,
    val extension: String,
    val version: String? = null,
    val discNumber: Int? = null,
    val discTotal: Int? = null,
    val isPrimary: Boolean,
)

class RomLibraryScanner(
    private val platforms: List<Platform> = DEFAULT_PLATFORMS,
) {
    fun scan(romsDirectory: File): List<ScannedGame> {
        if (!romsDirectory.isDirectory) return emptyList()

        return platforms.flatMap { platform ->
            val platformDirectory = File(romsDirectory, platform.romFolderName)
            scanPlatform(platformDirectory, platform)
        }.sortedWith(compareBy({ it.platform.name }, { it.title.lowercase() }))
    }

    private fun scanPlatform(platformDirectory: File, platform: Platform): List<ScannedGame> {
        if (!platformDirectory.isDirectory) return emptyList()

        val children = platformDirectory.listFiles().orEmpty()
        val looseGames = children
            .filter { it.isFile && it.hasSupportedExtension(platform) }
            .map { file ->
                ScannedGame(
                    platform = platform,
                    title = file.nameWithoutExtension,
                    files = listOf(file.toScannedGameFile(platform, GameFileType.Base, isPrimary = true)),
                )
            }

        val folderGames = children
            .filter { it.isDirectory }
            .mapNotNull { directory -> scanGameDirectory(platform, directory) }

        return looseGames + folderGames
    }

    private fun scanGameDirectory(platform: Platform, gameDirectory: File): ScannedGame? {
        val files = mutableListOf<ScannedGameFile>()
        val children = gameDirectory.listFiles().orEmpty()

        children
            .filter { it.isFile && it.hasSupportedExtension(platform) }
            .mapTo(files) { file -> file.toScannedGameFile(platform, GameFileType.Base, isPrimary = false) }

        if (gameDirectory.isContentDirectory()) {
            files += gameDirectory.toScannedGameFile(platform, GameFileType.Base, extension = DIRECTORY_EXTENSION, isPrimary = false)
        }

        children.filter { it.isDirectory }.forEach { child ->
            val type = child.fileTypeFromFolderName()
            if (type != null) {
                files += scanTypedDirectory(platform, child, type)
            } else if (child.isContentDirectory()) {
                files += child.toScannedGameFile(
                    platform = platform,
                    fileType = GameFileType.Base,
                    extension = DIRECTORY_EXTENSION,
                    isPrimary = false,
                )
            }
        }

        val launchableFiles = files.filter { it.fileType.isLaunchable }
        val discTotal = launchableFiles.count { it.fileType == GameFileType.Disc }.takeIf { it > 0 }
        val normalizedFiles = files.map { scannedFile ->
            when {
                scannedFile.fileType == GameFileType.Disc && discTotal != null ->
                    scannedFile.copy(discTotal = discTotal)
                else -> scannedFile
            }
        }

        val primary = normalizedFiles.pickPrimaryFile() ?: return null
        return ScannedGame(
            platform = platform,
            title = gameDirectory.name,
            files = normalizedFiles.map { it.copy(isPrimary = it.file == primary.file) },
        )
    }

    private fun scanTypedDirectory(
        platform: Platform,
        directory: File,
        fileType: GameFileType,
    ): List<ScannedGameFile> {
        val contentDirectories = directory.walkTopDown()
            .filter { it.isDirectory && it.isContentDirectory() }
            .map { contentDirectory ->
                contentDirectory.toScannedGameFile(
                    platform = platform,
                    fileType = fileType,
                    extension = DIRECTORY_EXTENSION,
                    version = contentDirectory.versionLabel() ?: contentDirectory.parentFile?.versionLabel(),
                    isPrimary = false,
                )
            }
            .toList()

        val files = directory.walkTopDown()
            .filter { it.isFile && it.hasSupportedExtension(platform) }
            .map { file ->
                file.toScannedGameFile(
                    platform = platform,
                    fileType = fileType,
                    version = file.parentFile?.versionLabel(),
                    discNumber = if (fileType == GameFileType.Disc) file.discNumber() else null,
                    isPrimary = false,
                )
            }
            .toList()

        return contentDirectories + files
    }

    private fun List<ScannedGameFile>.pickPrimaryFile(): ScannedGameFile? =
        firstOrNull { it.fileType == GameFileType.Base } ?: firstOrNull { it.fileType == GameFileType.Disc }

    private fun File.toScannedGameFile(
        platform: Platform,
        fileType: GameFileType,
        extension: String = this.extension.lowercase(),
        version: String? = null,
        discNumber: Int? = null,
        isPrimary: Boolean,
    ): ScannedGameFile =
        ScannedGameFile(
            file = this,
            fileType = fileType,
            extension = extension,
            version = version,
            discNumber = discNumber,
            isPrimary = isPrimary,
        )

    private fun File.hasSupportedExtension(platform: Platform): Boolean =
        extension.lowercase() in platform.supportedExtensions

    private fun File.fileTypeFromFolderName(): GameFileType? =
        when (name.lowercase()) {
            "base", "game" -> GameFileType.Base
            "updates", "update" -> GameFileType.Update
            "dlc", "addons", "add-ons" -> GameFileType.Dlc
            "discs", "disc" -> GameFileType.Disc
            "patches", "patch" -> GameFileType.Patch
            "saves", "save" -> GameFileType.Save
            "manuals", "manual" -> GameFileType.Manual
            else -> null
        }

    private fun File.versionLabel(): String? {
        val lowercaseName = name.lowercase()
        return name.takeIf {
            lowercaseName.startsWith("v") ||
                lowercaseName.startsWith("version") ||
                lowercaseName.startsWith("update")
        }
    }

    private fun File.discNumber(): Int? =
        DISC_NUMBER_PATTERN.find(nameWithoutExtension)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()

    private fun File.isContentDirectory(): Boolean {
        if (!isDirectory) return false
        val childNames = listFiles().orEmpty().filter { it.isDirectory }.map { it.name.lowercase() }.toSet()
        return WIIU_CONTENT_DIRECTORIES.all { it in childNames }
    }

    private companion object {
        const val DIRECTORY_EXTENSION = "folder"
        val WIIU_CONTENT_DIRECTORIES = setOf("code", "content", "meta")
        val DISC_NUMBER_PATTERN = Regex("""(?:disc|disk)\s*([0-9]+)""", RegexOption.IGNORE_CASE)
    }
}
