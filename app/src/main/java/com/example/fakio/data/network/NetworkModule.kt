package com.example.fakio.data.network

import com.example.fakio.data.remote.UploadService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    //TODO move to config file
    private const val SERVER_URL = "http://192.168.87.166:5000/"
    private const val TIMEOUT_SECONDS = 30L

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(request)
    }

    private val errorInterceptor = Interceptor { chain ->
        try {
            val response = chain.proceed(chain.request())
            if (!response.isSuccessful) {
            }
            response
        } catch (e: Exception) {
            throw e
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(errorInterceptor)
        .addInterceptor(loggingInterceptor) // Add this last so it logs the final request
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(SERVER_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val uploadService: UploadService = retrofit.create(UploadService::class.java)
}