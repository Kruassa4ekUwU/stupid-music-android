package com.stupidmusic.app.data.api

import com.stupidmusic.app.data.model.SearchResult
import com.stupidmusic.app.data.model.TrendingItem
import com.stupidmusic.app.data.model.VideoDetail
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InvidiousApi {

    @GET("api/v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("sort_by") sortBy: String = "relevance",
        @Query("page") page: Int = 1
    ): List<SearchResult>

    @GET("api/v1/videos/{videoId}")
    suspend fun getVideo(
        @Path("videoId") videoId: String
    ): VideoDetail

    @GET("api/v1/trending")
    suspend fun getTrending(
        @Query("type") type: String = "music",
        @Query("region") region: String = "US"
    ): List<TrendingItem>
}
