package com.example.fakio.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadResponse(
    @Json(name = "message") val message: String,
    @Json(name = "filename") val filename: String
)