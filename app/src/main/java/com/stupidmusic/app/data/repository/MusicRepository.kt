package com.stupidmusic.app.data.repository

import com.stupidmusic.app.BuildConfig
import com.stupidmusic.app.data.api.YoutubeApi
import com.stupidmusic.app.data.model.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val api: YoutubeApi
) {
    private val apiKey = BuildConfig.YOUTUBE_API_KEY

    suspend fun search(query: String): Result<List<Track>> = runCatching {
        val searchResp = api.search(query = "$query music", apiKey = apiKey)
        val ids = searchResp.items.map { it.id.videoId }.filter { it.isNotEmpty() }
        if (ids.isEmpty()) return@runCatching emptyList()

        val videosResp = api.getVideos(ids = ids.joinToString(","), apiKey = apiKey)
        val detailMap = videosResp.items.associateBy { it.id }

        searchResp.items.mapNotNull { item ->
            val videoId = item.id.videoId.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val detail = detailMap[videoId]
            Track(
                videoId = videoId,
                title = item.snippet.title,
                artist = item.snippet.channelTitle,
                thumbnailUrl = item.snippet.thumbnails.best,
                durationFormatted = detail?.contentDetails?.durationFormatted ?: "",
                durationSeconds = detail?.contentDetails?.durationSeconds ?: 0
            )
        }
    }

    suspend fun getTopMusic(): Result<List<Track>> = runCatching {
        val queries = listOf("top hits 2024", "popular music 2024", "best songs 2024")
        val query = queries.random()
        val searchResp = api.search(query = query, apiKey = apiKey)
        val ids = searchResp.items.map { it.id.videoId }.filter { it.isNotEmpty() }
        if (ids.isEmpty()) return@runCatching emptyList()

        val videosResp = api.getVideos(ids = ids.joinToString(","), apiKey = apiKey)
        val detailMap = videosResp.items.associateBy { it.id }

        searchResp.items.mapNotNull { item ->
            val videoId = item.id.videoId.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val detail = detailMap[videoId]
            Track(
                videoId = videoId,
                title = item.snippet.title,
                artist = item.snippet.channelTitle,
                thumbnailUrl = item.snippet.thumbnails.best,
                durationFormatted = detail?.contentDetails?.durationFormatted ?: "",
                durationSeconds = detail?.contentDetails?.durationSeconds ?: 0
            )
        }
    }
}
