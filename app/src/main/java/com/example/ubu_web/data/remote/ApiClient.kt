package com.example.ubu_web.data.remote

import android.util.Log
import com.example.ubu_web.data.remote.dto.ApiResponse
import com.example.ubu_web.data.remote.dto.LocationPayload
import com.example.ubu_web.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "ApiClient"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun sendLocations(payload: LocationPayload): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val bodyJson = json.encodeToString(LocationPayload.serializer(), payload)
            val body = bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(Constants.SERVER_BASE_URL + Constants.ENDPOINT_LOCATIONS)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "HTTP ${response.code}")
                    return@withContext false
                }
                val responseBody = response.body?.string() ?: return@withContext false
                val apiResponse = json.decodeFromString(ApiResponse.serializer(), responseBody)
                Log.i(TAG, "✅ Servidor: ${apiResponse.message} (inserted=${apiResponse.inserted})")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ${e.message}")
            false
        }
    }
}