package com.calico.launcher.data

import java.io.File

data class ScanRoot(
    val root: File,
    val roms: File,
    val media: File,
    val database: File,
)

sealed interface ScanRootValidation {
    data class Valid(val scanRoot: ScanRoot) : ScanRootValidation
    data class Invalid(val missingPaths: List<String>) : ScanRootValidation
}

object ScanRootValidator {
    fun validate(rootPath: String): ScanRootValidation {
        val root = File(rootPath)
        val roms = File(root, "roms")
        val media = File(root, "media")
        val database = File(root, "metadata.db")

        val missing = buildList {
            if (!root.exists()) add(root.absolutePath)
            if (!roms.isDirectory) add(roms.absolutePath)
            if (!media.isDirectory) add(media.absolutePath)
            if (!database.isFile) add(database.absolutePath)
        }

        return if (missing.isEmpty()) {
            ScanRootValidation.Valid(ScanRoot(root, roms, media, database))
        } else {
            ScanRootValidation.Invalid(missing)
        }
    }
}
