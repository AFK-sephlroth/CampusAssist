package com.example.campusassist.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.campusassist.data.remote.CloudinaryStorageSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudinary: CloudinaryStorageSource
) {
    companion object {
        private const val TAG = "ImageStorage"
    }

    private val attachmentsDir: File
        get() = File(context.filesDir, "ticket_attachments").also { it.mkdirs() }

    /**
     * Persists an image and returns a permanent https:// URL.
     *
     * For content:// URIs (photo picker): uploads directly to Cloudinary.
     * For file:// URIs (already local): uploads to Cloudinary.
     * For https:// URLs (already uploaded): returns as-is.
     *
     * Falls back to a local file:// copy if upload fails (offline).
     */
    suspend fun persistImage(uriString: String): String {
        Log.d(TAG, "persistImage called with: $uriString")

        // Already a Cloudinary URL — nothing to do
        if (uriString.startsWith("https://")) {
            Log.d(TAG, "Already an https URL, skipping upload")
            return uriString
        }

        val uri = Uri.parse(uriString)

        // Try uploading directly to Cloudinary
        return try {
            val downloadUrl = cloudinary.uploadImage(uri)
            Log.d(TAG, "Upload successful: $downloadUrl")
            // Clean up local file copy if it exists
            if (uriString.startsWith("file://")) {
                runCatching { File(uri.path ?: "").delete() }
            }
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Cloudinary upload failed, falling back to local copy", e)
            // Upload failed — copy to local storage as fallback
            copyToLocal(uriString) ?: uriString
        }
    }

    /**
     * Deletes both the remote Cloudinary copy and any local file.
     */
    suspend fun deleteImage(uriString: String) {
        when {
            uriString.startsWith("https://") -> cloudinary.deleteImage(uriString)
            uriString.startsWith("file://")  ->
                runCatching { File(Uri.parse(uriString).path ?: return).delete() }
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun copyToLocal(uriString: String): String? {
        if (uriString.startsWith("file://") || uriString.startsWith("/")) return uriString
        return try {
            val uri = Uri.parse(uriString)
            val ext = context.contentResolver.getType(uri)
                ?.substringAfterLast('/')
                ?.let { ".$it" } ?: ".jpg"
            val destFile = File(attachmentsDir, "${UUID.randomUUID()}$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            destFile.toURI().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy to local storage", e)
            null
        }
    }
}
