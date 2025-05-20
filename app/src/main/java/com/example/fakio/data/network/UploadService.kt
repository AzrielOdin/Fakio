package com.example.fakio.data.remote

import com.example.fakio.data.network.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<UploadResponse>
}