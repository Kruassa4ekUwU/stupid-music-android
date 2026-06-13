package com.stupidmusic.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// YouTube Search API response
@Serializable
data class YoutubeSearchResponse(
    @SerialName("items") val items: List<YoutubeSearchItem> = emptyList(),
    @SerialName("nextPageToken") val nextPageToken: String? = null
)

@Serializable
data class YoutubeSearchItem(
    @SerialName("id") val id: VideoId = VideoId(),
    @SerialName("snippet") val snippet: Snippet = Snippet()
)

@Serializable
data class VideoId(
    @SerialName("videoId") val videoId: String = ""
)

@Serializable
data class Snippet(
    @SerialName("title") val title: String = "",
    @SerialName("channelTitle") val channelTitle: String = "",
    @SerialName("thumbnails") val thumbnails: Thumbnails = Thumbnails(),
    @SerialName("publishedAt") val publishedAt: String = ""
)

@Serializable
data class Thumbnails(
    @SerialName("default") val default: ThumbUrl? = null,
    @SerialName("medium") val medium: ThumbUrl? = null,
    @SerialName("high") val high: ThumbUrl? = null,
    @SerialName("maxres") val maxres: ThumbUrl? = null
) {
    val best: String get() = maxres?.url ?: high?.url ?: medium?.url ?: default?.url ?: ""
}

@Serializable
data class ThumbUrl(
    @SerialName("url") val url: String = ""
)

// YouTube Videos API response (for duration)
@Serializable
data class YoutubeVideosResponse(
    @SerialName("items") val items: List<YoutubeVideoItem> = emptyList()
)

@Serializable
data class YoutubeVideoItem(
    @SerialName("id") val id: String = "",
    @SerialName("snippet") val snippet: Snippet = Snippet(),
    @SerialName("contentDetails") val contentDetails: ContentDetails = ContentDetails()
)

@Serializable
data class ContentDetails(
    @SerialName("duration") val duration: String = "" // ISO 8601 e.g. PT3M45S
) {
    val durationFormatted: String get() {
        val d = duration
        val hours = Regex("(\\d+)H").find(d)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val mins = Regex("(\\d+)M").find(d)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val secs = Regex("(\\d+)S").find(d)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return if (hours > 0) "%d:%02d:%02d".format(hours, mins, secs)
        else "%d:%02d".format(mins, secs)
    }
    val durationSeconds: Int get() {
        val d = duration
        val hours = Regex("(\\d+)H").find(d)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val mins = Regex("(\\d+)M").find(d)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val secs = Regex("(\\d+)S").find(d)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return hours * 3600 + mins * 60 + secs
    }
}

// Internal track model
data class Track(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationFormatted: String = "",
    val durationSeconds: Int = 0
) {
    val streamUrl: String get() = "https://www.youtube.com/watch?v=$videoId"
}

// Player state
data class PlayerState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
) {
    val progress: Float get() = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f
}
