package com.example.fakio.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fakio.data.network.NetworkModule
import com.example.fakio.data.network.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class UploadRepository(private val context: Context) {

    suspend fun uploadImage(imageUri: Uri): Result<UploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val file = uriToFile(imageUri)
                Log.d("Fakio image upload", "File uri: $imageUri")
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = NetworkModule.uploadService.uploadImage(body)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use { it.copyTo(outputStream) }
        }

        return tempFile
    }
}