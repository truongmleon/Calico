package com.calico.launcher.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.calico.launcher.model.MusicTrack
import java.io.File
import java.security.MessageDigest

object MusicLibraryLoader {
    private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "ogg", "flac", "m4a", "aac")

    fun load(context: Context, rootUri: Uri): List<MusicTrack> {
        val rootPath = treeUriToPath(rootUri)
        val musicFile = rootPath?.let { File(it, "music") }?.takeIf { it.isDirectory }
        if (musicFile != null) {
            return scanMusicDirectory(musicFile)
        }

        val rootDocument = DocumentFile.fromTreeUri(context, rootUri) ?: return emptyList()
        val musicDocument = rootDocument.findFile("music")?.takeIf { it.isDirectory } ?: return emptyList()
        return scanMusicDocument(musicDocument)
    }

    private fun scanMusicDirectory(directory: File): List<MusicTrack> =
        directory.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in AUDIO_EXTENSIONS }
            .map { file -> file.toMusicTrack() }
            .sortedBy { it.sortKey() }
            .toList()

    private fun scanMusicDocument(directory: DocumentFile): List<MusicTrack> =
        directory.walkAudioFiles()
            .map { file -> file.toMusicTrack() }
            .sortedBy { it.sortKey() }
            .toList()

    private fun File.toMusicTrack(): MusicTrack {
        val path = absolutePath
        val trackId = stableTrackId(path)
        val parentName = parentFile?.name?.takeUnless { it.equals("music", ignoreCase = true) }
        return MusicTrack(
            id = trackId,
            title = nameWithoutExtension,
            artist = parentName,
            filePath = path,
            contentUri = Uri.fromFile(this),
            durationSeconds = 0,
            isFavorite = false,
        )
    }

    private fun DocumentFile.toMusicTrack(): MusicTrack {
        val logicalPath = "/Emulation/music/${uri.lastPathSegment.orEmpty()}"
        val trackId = stableTrackId(logicalPath)
        val title = name?.substringBeforeLast('.').orEmpty()
        return MusicTrack(
            id = trackId,
            title = title,
            artist = null,
            filePath = logicalPath,
            contentUri = uri,
            durationSeconds = 0,
            isFavorite = false,
        )
    }

    private fun stableTrackId(key: String): Int {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(key.encodeToByteArray())
        val value = digest.take(4).fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xFF) }
        return if (value == 0) 1 else value
    }

    private fun MusicTrack.sortKey(): String =
        listOfNotNull(artist, title).joinToString(" ").lowercase()

    private fun DocumentFile.walkAudioFiles(): Sequence<DocumentFile> = sequence {
        if (!isDirectory) {
            if (isFile && name?.substringAfterLast('.', "")?.lowercase() in AUDIO_EXTENSIONS) {
                yield(this@walkAudioFiles)
            }
            return@sequence
        }
        listFiles()?.forEach { child ->
            yieldAll(child.walkAudioFiles())
        }
    }
}
