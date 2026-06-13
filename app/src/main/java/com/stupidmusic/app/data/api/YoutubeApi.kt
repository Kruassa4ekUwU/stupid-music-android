package com.stupidmusic.app.data.api

import com.stupidmusic.app.data.model.YoutubeSearchResponse
import com.stupidmusic.app.data.model.YoutubeVideosResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApi {

    @GET("youtube/v3/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") categoryId: String = "10", // Music category
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): YoutubeSearchResponse

    @GET("youtube/v3/videos")
    suspend fun getVideos(
        @Query("id") ids: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("key") apiKey: String
    ): YoutubeVideosResponse
}
