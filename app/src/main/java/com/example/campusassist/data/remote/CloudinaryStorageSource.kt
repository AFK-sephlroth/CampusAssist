package com.example.campusassist.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.campusassist.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.FormBody
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryStorageSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CloudinaryStorage"
    }

    private val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val apiKey    = BuildConfig.CLOUDINARY_API_KEY
    private val apiSecret = BuildConfig.CLOUDINARY_API_SECRET
    private val folder    = "ticket_attachments"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // ── Upload ────────────────────────────────────────────────────────────────

    /**
     * Uploads the image at [uri] (content:// or file://) to Cloudinary.
     * Reads bytes via ContentResolver so file:// vs content:// doesn't matter.
     * Returns the permanent https:// secure_url on success.
     */
    suspend fun uploadImage(uri: Uri): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Uploading image: $uri")

        // Read image bytes directly — works for both content:// and file:// URIs
        val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Cannot open input stream for URI: $uri")

        Log.d(TAG, "Image bytes read: ${imageBytes.size}")

        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val publicId  = "$folder/${UUID.randomUUID()}"

        // Params must be sorted alphabetically for Cloudinary signature
        val signParams = "folder=$folder&public_id=$publicId&timestamp=$timestamp"
        val signature  = sha1("$signParams$apiSecret")

        Log.d(TAG, "Uploading to Cloudinary cloud: $cloudName")

        // Detect MIME type from URI
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val imageBody = imageBytes.toRequestBody(mimeType.toMediaType())

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file",       "upload.jpg", imageBody)
            .addFormDataPart("api_key",    apiKey)
            .addFormDataPart("timestamp",  timestamp)
            .addFormDataPart("public_id",  publicId)
            .addFormDataPart("folder",     folder)
            .addFormDataPart("signature",  signature)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response from Cloudinary")

        if (!response.isSuccessful) {
            Log.e(TAG, "Cloudinary upload failed (${response.code}): $body")
            throw Exception("Cloudinary upload failed (${response.code}): $body")
        }

        val secureUrl = JSONObject(body).getString("secure_url")
        Log.d(TAG, "Upload successful: $secureUrl")
        secureUrl
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    suspend fun deleteImage(secureUrl: String) = withContext(Dispatchers.IO) {
        if (!secureUrl.contains("cloudinary.com")) return@withContext

        runCatching {
            // Extract public_id: strip everything up to and including the version segment
            // e.g. https://res.cloudinary.com/cloud/image/upload/v1234/ticket_attachments/uuid.jpg
            //   → ticket_attachments/uuid
            val afterUpload = secureUrl.substringAfter("/upload/")
            val publicId = if (afterUpload.startsWith("v") && afterUpload[1].isDigit()) {
                afterUpload.substringAfter("/").substringBeforeLast(".")
            } else {
                afterUpload.substringBeforeLast(".")
            }

            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val signature = sha1("public_id=$publicId&timestamp=$timestamp$apiSecret")

            val requestBody = FormBody.Builder()
                .add("public_id", publicId)
                .add("timestamp", timestamp)
                .add("api_key",   apiKey)
                .add("signature", signature)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/destroy")
                .post(requestBody)
                .build()

            client.newCall(request).execute().close()
            Log.d(TAG, "Deleted image: $publicId")
        }.onFailure { Log.e(TAG, "Failed to delete image: $secureUrl", it) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun sha1(input: String): String =
        MessageDigest.getInstance("SHA-1")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
