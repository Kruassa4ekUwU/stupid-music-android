package com.stupidmusic.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class PipedStreams(
    @SerialName("audioStreams") val audioStreams: List<AudioStream> = emptyList(),
    @SerialName("title") val title: String = "",
    @SerialName("uploader") val uploader: String = "",
    @SerialName("thumbnailUrl") val thumbnailUrl: String = ""
) {
    val bestAudioUrl: String get() {
        val m4a = audioStreams.filter { it.mimeType.contains("m4a") || it.mimeType.contains("mp4") }
            .maxByOrNull { it.bitrate }
        val webm = audioStreams.filter { it.mimeType.contains("webm") || it.mimeType.contains("opus") }
            .maxByOrNull { it.bitrate }
        return m4a?.url ?: webm?.url ?: audioStreams.maxByOrNull { it.bitrate }?.url ?: ""
    }
}

@Serializable
data class AudioStream(
    @SerialName("url") val url: String = "",
    @SerialName("mimeType") val mimeType: String = "",
    @SerialName("bitrate") val bitrate: Int = 0,
    @SerialName("quality") val quality: String = ""
)

interface PipedApi {
    @GET("streams/{videoId}")
    suspend fun getStreams(@Path("videoId") videoId: String): PipedStreams
}
