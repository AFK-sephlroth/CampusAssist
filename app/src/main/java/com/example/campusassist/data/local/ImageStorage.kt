package com.example.campusassist.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies images from temporary content:// URIs (returned by the photo picker)
 * into the app's private files directory so they survive app restarts and
 * force-stops.
 *
 * Android revokes content:// URI permissions when the app process dies, so
 * any URI obtained from ActivityResultContracts.GetMultipleContents() must
 * be persisted as a real file before saving to the DB.
 */
@Singleton
class ImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val attachmentsDir: File
        get() = File(context.filesDir, "ticket_attachments").also { it.mkdirs() }

    /**
     * Copies the image at [uriString] into internal storage.
     * Returns the file:// URI string of the copy, or the original string
     * if it already points to an internal file (idempotent).
     */
    fun persistImage(uriString: String): String {
        // Already a persisted file path — nothing to do.
        if (uriString.startsWith("file://") || uriString.startsWith("/")) return uriString

        val uri = Uri.parse(uriString)
        val ext = context.contentResolver.getType(uri)
            ?.substringAfterLast('/')
            ?.let { ".$it" }
            ?: ".jpg"

        val destFile = File(attachmentsDir, "${UUID.randomUUID()}$ext")

        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return uriString   // couldn't open — fall back to original

        return destFile.toURI().toString()   // file:// URI
    }

    /**
     * Deletes a previously persisted image file.
     * Safe to call with non-file URIs (no-op).
     */
    fun deleteImage(uriString: String) {
        if (!uriString.startsWith("file://")) return
        runCatching { File(Uri.parse(uriString).path ?: return).delete() }
    }
}
