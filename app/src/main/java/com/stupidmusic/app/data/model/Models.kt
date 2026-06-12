package com.stupidmusic.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    @SerialName("videoId") val videoId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("authorId") val authorId: String = "",
    @SerialName("videoThumbnails") val thumbnails: List<Thumbnail> = emptyList(),
    @SerialName("lengthSeconds") val lengthSeconds: Int = 0,
    @SerialName("viewCount") val viewCount: Long = 0,
    @SerialName("type") val type: String = ""
) {
    val bestThumbnail: String
        get() = thumbnails.firstOrNull { it.quality == "maxresdefault" }?.url
            ?: thumbnails.firstOrNull { it.quality == "high" }?.url
            ?: thumbnails.firstOrNull()?.url
            ?: ""

    val durationFormatted: String
        get() {
            val minutes = lengthSeconds / 60
            val seconds = lengthSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

@Serializable
data class Thumbnail(
    @SerialName("quality") val quality: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0
)

@Serializable
data class VideoDetail(
    @SerialName("videoId") val videoId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("videoThumbnails") val thumbnails: List<Thumbnail> = emptyList(),
    @SerialName("adaptiveFormats") val adaptiveFormats: List<AdaptiveFormat> = emptyList(),
    @SerialName("formatStreams") val formatStreams: List<FormatStream> = emptyList(),
    @SerialName("lengthSeconds") val lengthSeconds: Int = 0,
    @SerialName("likeCount") val likeCount: Long = 0,
    @SerialName("viewCount") val viewCount: Long = 0
) {
    val bestAudioUrl: String
        get() {
            // Prefer audio-only adaptive formats, sorted by bitrate
            val audioOnly = adaptiveFormats
                .filter { it.type.startsWith("audio/") }
                .sortedByDescending { it.bitrate }
            return audioOnly.firstOrNull()?.url
                ?: formatStreams.firstOrNull()?.url
                ?: ""
        }

    val bestThumbnail: String
        get() = thumbnails.firstOrNull { it.quality == "maxresdefault" }?.url
            ?: thumbnails.firstOrNull { it.quality == "high" }?.url
            ?: thumbnails.firstOrNull()?.url
            ?: ""
}

@Serializable
data class AdaptiveFormat(
    @SerialName("url") val url: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("bitrate") val bitrate: Int = 0,
    @SerialName("encoding") val encoding: String = ""
)

@Serializable
data class FormatStream(
    @SerialName("url") val url: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("quality") val quality: String = ""
)

@Serializable
data class TrendingItem(
    @SerialName("videoId") val videoId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("videoThumbnails") val thumbnails: List<Thumbnail> = emptyList(),
    @SerialName("lengthSeconds") val lengthSeconds: Int = 0,
    @SerialName("viewCount") val viewCount: Long = 0
) {
    val bestThumbnail: String
        get() = thumbnails.firstOrNull { it.quality == "maxresdefault" }?.url
            ?: thumbnails.firstOrNull { it.quality == "high" }?.url
            ?: thumbnails.firstOrNull()?.url
            ?: ""

    fun toSearchResult() = SearchResult(
        videoId = videoId,
        title = title,
        author = author,
        thumbnails = thumbnails,
        lengthSeconds = lengthSeconds,
        viewCount = viewCount,
        type = "video"
    )
}

// Player state
data class PlayerState(
    val currentTrack: SearchResult? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)
