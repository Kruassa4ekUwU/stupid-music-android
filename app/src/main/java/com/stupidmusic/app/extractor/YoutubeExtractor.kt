package com.stupidmusic.app.extractor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Extracts YouTube audio stream URLs directly using the internal Android YouTube API.
 * No third-party libraries, no external servers — works directly from the device.
 */
object YoutubeExtractor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // YouTube Android app API key (public, embedded in APK)
    private const val ANDROID_API_KEY = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w"
    private const val ANDROID_CLIENT_VERSION = "19.09.37"

    suspend fun getAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            // Use YouTube's internal /youtubei/v1/player endpoint
            // This is exactly what the official YouTube Android app uses
            val body = buildJsonObject {
                putJsonObject("context") {
                    putJsonObject("client") {
                        put("clientName", "ANDROID")
                        put("clientVersion", ANDROID_CLIENT_VERSION)
                        put("androidSdkVersion", 33)
                        put("hl", "en")
                        put("gl", "US")
                    }
                }
                put("videoId", videoId)
                put("contentCheckOk", true)
                put("racyCheckOk", true)
            }.toString()

            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/player?key=$ANDROID_API_KEY")
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("User-Agent", "com.google.android.youtube/19.09.37 (Linux; U; Android 13; en_US) gzip")
                .addHeader("X-YouTube-Client-Name", "3")
                .addHeader("X-YouTube-Client-Version", ANDROID_CLIENT_VERSION)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.e("YoutubeExtractor", "HTTP ${response.code}: ${responseBody.take(200)}")
                return@withContext null
            }

            val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
            val streamingData = jsonResponse["streamingData"]?.jsonObject ?: return@withContext null
            val formats = streamingData["adaptiveFormats"]?.jsonArray ?: return@withContext null

            // Find best audio-only stream
            val audioStreams = formats.mapNotNull { it.jsonObject }
                .filter { format ->
                    val mimeType = format["mimeType"]?.jsonPrimitive?.content ?: ""
                    // Audio only (no video)
                    (mimeType.contains("audio/mp4") || mimeType.contains("audio/webm")) &&
                    format["url"] != null
                }
                .sortedByDescending {
                    it["bitrate"]?.jsonPrimitive?.intOrNull ?: 0
                }

            val best = audioStreams.firstOrNull { 
                it["mimeType"]?.jsonPrimitive?.content?.contains("mp4") == true 
            } ?: audioStreams.firstOrNull()

            val url = best?.get("url")?.jsonPrimitive?.content
            Log.d("YoutubeExtractor", "Got URL for $videoId: ${url?.take(80)}")
            url

        } catch (e: Exception) {
            Log.e("YoutubeExtractor", "Error for $videoId: ${e.message}")
            null
        }
    }
}
