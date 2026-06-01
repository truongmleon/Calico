package com.calico.launcher.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.calico.launcher.model.Game
import com.calico.launcher.model.GameFile
import com.calico.launcher.model.GameFileType
import com.calico.launcher.model.Platform
import java.io.File
import java.security.MessageDigest

sealed interface GameLibraryLoadResult {
    data class Success(
        val games: List<Game>,
        val rootUri: Uri,
        val scannedFromFilePath: Boolean,
    ) : GameLibraryLoadResult

    data class InvalidRoot(
        val missingPaths: List<String>,
    ) : GameLibraryLoadResult

    data class Error(
        val message: String,
    ) : GameLibraryLoadResult
}

object GameLibraryLoader {
    private val scanner = RomLibraryScanner()

    fun load(
        context: Context,
        rootUri: Uri,
        favoriteOverrides: Map<Int, Boolean> = emptyMap(),
    ): GameLibraryLoadResult {
        val rootDocument = DocumentFile.fromTreeUri(context, rootUri)
            ?: return GameLibraryLoadResult.Error("Could not open the selected folder.")

        val missing = validateRoot(rootDocument)
        if (missing.isNotEmpty()) {
            return GameLibraryLoadResult.InvalidRoot(missing)
        }

        val rootPath = treeUriToPath(rootUri)
        val romsFile = rootPath?.let { File(it, "roms") }?.takeIf { it.isDirectory }
        val games = if (romsFile != null) {
            scanner.scan(romsFile).map { it.toGame(favoriteOverrides) }
        } else {
            val romsDocument = rootDocument.findFile("roms")
                ?: return GameLibraryLoadResult.Error("Could not read the roms folder.")
            DocumentRomLibraryScanner().scan(romsDocument).map { it.toGame(favoriteOverrides) }
        }

        return GameLibraryLoadResult.Success(
            games = games,
            rootUri = rootUri,
            scannedFromFilePath = romsFile != null,
        )
    }

    private fun validateRoot(root: DocumentFile): List<String> {
        val missing = mutableListOf<String>()
        if (!root.exists()) missing += "Emulation root"
        if (root.findFile("roms")?.isDirectory != true) missing += "roms/"
        if (root.findFile("media")?.isDirectory != true) missing += "media/"
        val metadata = root.findFile("metadata.db")
        if (metadata == null || !metadata.isFile) missing += "metadata.db"
        return missing
    }
}

private class DocumentRomLibraryScanner(
    private val platforms: List<Platform> = DEFAULT_PLATFORMS,
) {
    fun scan(romsDirectory: DocumentFile): List<DocumentScannedGame> {
        if (!romsDirectory.isDirectory) return emptyList()

        return romsDirectory.listChildren()
            .filter { it.isDirectory }
            .mapNotNull { platformDirectory ->
                val platform = platforms.firstOrNull {
                    it.romFolderName.equals(platformDirectory.name, ignoreCase = true)
                } ?: return@mapNotNull null
                scanPlatform(platformDirectory, platform)
            }
            .flatten()
            .sortedWith(compareBy({ it.platform.name }, { it.title.lowercase() }))
    }

    private fun scanPlatform(platformDirectory: DocumentFile, platform: Platform): List<DocumentScannedGame> {
        val children = platformDirectory.listChildren()
        val looseGames = children
            .filter { it.isSupportedFile(platform) }
            .map { file ->
                DocumentScannedGame(
                    platform = platform,
                    title = file.displayName(),
                    files = listOf(
                        file.toScannedDocumentFile(
                            platform = platform,
                            fileType = GameFileType.Base,
                            isPrimary = true,
                            logicalPath = file.logicalPath(platformDirectory),
                        ),
                    ),
                )
            }

        val folderGames = children
            .filter { it.isDirectory }
            .mapNotNull { directory -> scanGameDirectory(platform, directory, platformDirectory) }

        return looseGames + folderGames
    }

    private fun scanGameDirectory(
        platform: Platform,
        gameDirectory: DocumentFile,
        platformDirectory: DocumentFile,
    ): DocumentScannedGame? {
        val files = mutableListOf<ScannedDocumentFile>()
        val children = gameDirectory.listChildren()
        val gameBasePath = gameDirectory.logicalPath(platformDirectory)

        children
            .filter { it.isSupportedFile(platform) }
            .mapTo(files) { file ->
                file.toScannedDocumentFile(
                    platform = platform,
                    fileType = GameFileType.Base,
                    isPrimary = false,
                    logicalPath = file.logicalPathFrom(gameBasePath),
                )
            }

        if (gameDirectory.isContentDirectory()) {
            files += gameDirectory.toScannedDocumentFile(
                platform = platform,
                fileType = GameFileType.Base,
                extension = DIRECTORY_EXTENSION,
                isPrimary = false,
                logicalPath = gameBasePath,
            )
        }

        children.filter { it.isDirectory }.forEach { child ->
            val type = child.fileTypeFromFolderName()
            if (type != null) {
                files += scanTypedDirectory(platform, child, type, gameBasePath)
            } else if (child.isContentDirectory()) {
                files += child.toScannedDocumentFile(
                    platform = platform,
                    fileType = GameFileType.Base,
                    extension = DIRECTORY_EXTENSION,
                    isPrimary = false,
                    logicalPath = child.logicalPathFrom(gameBasePath),
                )
            }
        }

        val launchableFiles = files.filter { it.fileType.isLaunchable }
        val discTotal = launchableFiles.count { it.fileType == GameFileType.Disc }.takeIf { it > 0 }
        val normalizedFiles = files.map { scannedFile ->
            if (scannedFile.fileType == GameFileType.Disc && discTotal != null) {
                scannedFile.copy(discTotal = discTotal)
            } else {
                scannedFile
            }
        }

        val primary = normalizedFiles.pickPrimaryFile() ?: return null
        return DocumentScannedGame(
            platform = platform,
            title = gameDirectory.name.orEmpty(),
            files = normalizedFiles.map { it.copy(isPrimary = it == primary) },
        )
    }

    private fun scanTypedDirectory(
        platform: Platform,
        directory: DocumentFile,
        fileType: GameFileType,
        gameBasePath: String,
    ): List<ScannedDocumentFile> {
        val typedBasePath = directory.logicalPathFrom(gameBasePath)
        val contentDirectories = directory.walkDirectories()
            .filter { it.isContentDirectory() }
            .map { contentDirectory ->
                val logicalPath = contentDirectory.logicalPathFrom(typedBasePath)
                contentDirectory.toScannedDocumentFile(
                    platform = platform,
                    fileType = fileType,
                    extension = DIRECTORY_EXTENSION,
                    version = contentDirectory.versionLabel()
                        ?: parentFolderVersionFromPath(logicalPath),
                    isPrimary = false,
                    logicalPath = logicalPath,
                )
            }
            .toList()

        val files = directory.walkFiles()
            .filter { it.isSupportedFile(platform) }
            .map { file ->
                val logicalPath = file.logicalPathFrom(typedBasePath)
                file.toScannedDocumentFile(
                    platform = platform,
                    fileType = fileType,
                    version = parentFolderVersionFromPath(logicalPath),
                    discNumber = if (fileType == GameFileType.Disc) file.discNumber() else null,
                    isPrimary = false,
                    logicalPath = logicalPath,
                )
            }
            .toList()

        return contentDirectories + files
    }

    private fun List<ScannedDocumentFile>.pickPrimaryFile(): ScannedDocumentFile? =
        firstOrNull { it.fileType == GameFileType.Base } ?: firstOrNull { it.fileType == GameFileType.Disc }
}

private data class DocumentScannedGame(
    val platform: Platform,
    val title: String,
    val files: List<ScannedDocumentFile>,
)

private data class ScannedDocumentFile(
    val uri: Uri,
    val logicalPath: String,
    val displayName: String,
    val fileType: GameFileType,
    val extension: String,
    val version: String? = null,
    val discNumber: Int? = null,
    val discTotal: Int? = null,
    val isPrimary: Boolean,
)

private fun ScannedGame.toGame(favoriteOverrides: Map<Int, Boolean>): Game {
    val primaryPath = primaryFile?.file?.absolutePath ?: title
    return buildGame(
        platform = platform,
        title = title,
        favoriteOverrides = favoriteOverrides,
        primaryKey = primaryPath,
        files = files.map { scannedFile ->
            GameFile(
                id = 0,
                gameId = 0,
                platformId = platform.id,
                fileType = scannedFile.fileType,
                path = scannedFile.file.absolutePath,
                name = scannedFile.file.name,
                extension = scannedFile.extension,
                contentUri = Uri.fromFile(scannedFile.file),
                version = scannedFile.version,
                discNumber = scannedFile.discNumber,
                discTotal = scannedFile.discTotal,
                isPrimary = scannedFile.isPrimary,
            )
        },
    )
}

private fun DocumentScannedGame.toGame(favoriteOverrides: Map<Int, Boolean>): Game {
    val primaryPath = files.firstOrNull { it.isPrimary }?.logicalPath ?: title
    return buildGame(
        platform = platform,
        title = title,
        favoriteOverrides = favoriteOverrides,
        primaryKey = primaryPath,
        files = files.map { scannedFile ->
            GameFile(
                id = 0,
                gameId = 0,
                platformId = platform.id,
                fileType = scannedFile.fileType,
                path = scannedFile.logicalPath,
                name = scannedFile.displayName,
                extension = scannedFile.extension,
                contentUri = scannedFile.uri,
                version = scannedFile.version,
                discNumber = scannedFile.discNumber,
                discTotal = scannedFile.discTotal,
                isPrimary = scannedFile.isPrimary,
            )
        },
    )
}

private fun buildGame(
    platform: Platform,
    title: String,
    favoriteOverrides: Map<Int, Boolean>,
    primaryKey: String,
    files: List<GameFile>,
): Game {
    val gameId = stableGameId(platform.id, title, primaryKey)
    val gameFiles = files.mapIndexed { index, file ->
        file.copy(id = gameId * 100 + index, gameId = gameId)
    }
    val primary = gameFiles.firstOrNull { it.isPrimary } ?: gameFiles.first()
    return Game(
        id = gameId,
        platform = platform,
        name = title,
        sortTitle = title.lowercase(),
        description = null,
        releaseDate = null,
        developers = emptyList(),
        genres = emptyList(),
        durationSeconds = 0,
        lastPlayedAt = null,
        isFavorite = favoriteOverrides[gameId] ?: false,
        primaryFile = primary,
        files = gameFiles,
    )
}

private fun stableGameId(platformId: Int, title: String, primaryKey: String): Int {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest("$platformId|$title|$primaryKey".encodeToByteArray())
    val value = digest.take(4).fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xFF) }
    return if (value == 0) 1 else value
}

private fun DocumentFile.listChildren(): List<DocumentFile> =
    listFiles()?.toList().orEmpty()

private fun DocumentFile.displayName(): String =
    name?.substringBeforeLast('.').orEmpty()

private fun DocumentFile.fileExtension(): String =
    name?.substringAfterLast('.', "")?.lowercase().orEmpty()

private fun DocumentFile.isSupportedFile(platform: Platform): Boolean =
    !isDirectory && fileExtension() in platform.supportedExtensions

private fun DocumentFile.logicalPath(platformDirectory: DocumentFile): String =
    "/Emulation/roms/${platformDirectory.name.orEmpty()}/${name.orEmpty()}"

private fun DocumentFile.logicalPathFrom(basePath: String): String =
    "$basePath/${name.orEmpty()}"

private fun parentFolderVersionFromPath(logicalPath: String): String? =
    versionLabelFromName(logicalPath.substringBeforeLast('/').substringAfterLast('/'))

private fun versionLabelFromName(name: String): String? {
    val lowercaseName = name.lowercase()
    return name.takeIf {
        lowercaseName.startsWith("v") ||
            lowercaseName.startsWith("version") ||
            lowercaseName.startsWith("update")
    }
}

private fun DocumentFile.fileTypeFromFolderName(): GameFileType? =
    when (name?.lowercase()) {
        "base", "game" -> GameFileType.Base
        "updates", "update" -> GameFileType.Update
        "dlc", "addons", "add-ons" -> GameFileType.Dlc
        "discs", "disc" -> GameFileType.Disc
        "patches", "patch" -> GameFileType.Patch
        "saves", "save" -> GameFileType.Save
        "manuals", "manual" -> GameFileType.Manual
        else -> null
    }

private fun DocumentFile.versionLabel(): String? = versionLabelFromName(name.orEmpty())

private fun DocumentFile.discNumber(): Int? =
    DISC_NUMBER_PATTERN.find(displayName())
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()

private fun DocumentFile.isContentDirectory(): Boolean {
    if (!isDirectory) return false
    val childNames = listChildren().filter { it.isDirectory }.map { it.name.orEmpty().lowercase() }.toSet()
    return WIIU_CONTENT_DIRECTORIES.all { it in childNames }
}

private fun DocumentFile.walkFiles(): Sequence<DocumentFile> = sequence {
    if (!isDirectory) {
        if (isFile) yield(this@walkFiles)
        return@sequence
    }
    listChildren().forEach { child ->
        yieldAll(child.walkFiles())
    }
}

private fun DocumentFile.walkDirectories(): Sequence<DocumentFile> = sequence {
    if (!isDirectory) return@sequence
    yield(this@walkDirectories)
    listChildren().filter { it.isDirectory }.forEach { child ->
        yieldAll(child.walkDirectories())
    }
}

private fun DocumentFile.toScannedDocumentFile(
    platform: Platform,
    fileType: GameFileType,
    extension: String = fileExtension(),
    version: String? = null,
    discNumber: Int? = null,
    isPrimary: Boolean,
    logicalPath: String,
): ScannedDocumentFile =
    ScannedDocumentFile(
        uri = uri,
        logicalPath = logicalPath,
        displayName = name.orEmpty(),
        fileType = fileType,
        extension = extension,
        version = version,
        discNumber = discNumber,
        isPrimary = isPrimary,
    )

private val DISC_NUMBER_PATTERN = Regex("""(?:disc|disk)\s*([0-9]+)""", RegexOption.IGNORE_CASE)
private val WIIU_CONTENT_DIRECTORIES = setOf("code", "content", "meta")
private const val DIRECTORY_EXTENSION = "folder"
