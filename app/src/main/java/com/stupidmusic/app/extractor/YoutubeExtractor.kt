package com.stupidmusic.app.extractor

import android.content.Context
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.StreamExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Headers.Companion.toHeaders
import java.util.concurrent.TimeUnit

object YoutubeExtractor {

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        NewPipe.init(OkHttpDownloader.instance)
        initialized = true
    }

    suspend fun getAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val service = ServiceList.YouTube
            val extractor: StreamExtractor = service.getStreamExtractor(url)
            extractor.fetchPage()

            val audioStreams = extractor.audioStreams
            if (audioStreams.isNullOrEmpty()) return@withContext null

            // Prefer m4a, then webm/opus
            val best = audioStreams
                .sortedByDescending { it.averageBitrate }
                .firstOrNull { it.format?.mimeType?.contains("m4a") == true || it.format?.mimeType?.contains("mp4") == true }
                ?: audioStreams.maxByOrNull { it.averageBitrate }

            best?.content
        } catch (e: Exception) {
            android.util.Log.e("YoutubeExtractor", "Failed for $videoId: ${e.message}")
            null
        }
    }
}

// OkHttp-based downloader for NewPipe
object OkHttpDownloader : Downloader() {

    val instance = this

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun execute(request: Request): Response {
        val requestBuilder = okhttp3.Request.Builder()
            .url(request.url())
            .method(request.httpMethod(), null)

        request.headers().forEach { (key, values) ->
            values.forEach { value -> requestBuilder.addHeader(key, value) }
        }

        // Add browser-like headers to avoid bot detection
        requestBuilder.addHeader("User-Agent",
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.210 Mobile Safari/537.36")
        requestBuilder.addHeader("Accept-Language", "en-US,en;q=0.9")

        val response = client.newCall(requestBuilder.build()).execute()
        val body = response.body?.string() ?: ""
        val headers = response.headers.toMultimap()

        return Response(
            response.code,
            response.message,
            headers,
            body,
            response.request.url.toString()
        )
    }
}
