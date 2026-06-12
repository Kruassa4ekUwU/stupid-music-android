package com.stupidmusic.app.data.repository

import com.stupidmusic.app.data.api.InvidiousApi
import com.stupidmusic.app.data.model.SearchResult
import com.stupidmusic.app.data.model.VideoDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val api: InvidiousApi
) {
    suspend fun search(query: String, page: Int = 1): Result<List<SearchResult>> = runCatching {
        api.search(query = query, page = page)
            .filter { it.type == "video" }
    }

    suspend fun getVideoDetail(videoId: String): Result<VideoDetail> = runCatching {
        api.getVideo(videoId)
    }

    suspend fun getTrending(): Result<List<SearchResult>> = runCatching {
        api.getTrending().map { it.toSearchResult() }
    }
}
