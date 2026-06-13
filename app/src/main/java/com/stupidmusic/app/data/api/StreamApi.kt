package com.stupidmusic.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class StreamResponse(
    @SerialName("url") val url: String = "",
    @SerialName("videoId") val videoId: String = "",
    @SerialName("error") val error: String? = null
)

interface StreamApi {
    @GET("stream/{videoId}")
    suspend fun getStreamUrl(@Path("videoId") videoId: String): StreamResponse
}
