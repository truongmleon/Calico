package com.calico.launcher.data

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

internal fun treeUriToPath(treeUri: Uri): String? {
    if (treeUri.scheme != "content" || treeUri.authority != "com.android.externalstorage.documents") {
        return null
    }
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
    val parts = documentId.split(':', limit = 2)
    if (parts.size != 2) return null
    return when (parts[0]) {
        "primary" -> File(Environment.getExternalStorageDirectory(), parts[1]).absolutePath
        else -> null
    }
}
