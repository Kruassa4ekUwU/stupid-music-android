package com.stupidmusic.app.data.repository

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stupidmusic.app.data.api.InvidiousApi
import com.stupidmusic.app.data.model.SearchResult
import com.stupidmusic.app.data.model.VideoDetail
import com.stupidmusic.app.data.network.InstanceManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val instanceManager: InstanceManager
) {
    // Build API for a specific instance URL
    private fun buildApi(baseUrl: String): InvidiousApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(InvidiousApi::class.java)

    // Try all instances until one works
    private suspend fun <T> withFallback(block: suspend (InvidiousApi) -> T): Result<T> {
        val tried = mutableSetOf<String>()
        repeat(InstanceManager.INSTANCES.size) {
            val instance = instanceManager.currentInstance
            if (instance in tried) {
                instanceManager.nextInstance()
                return@repeat
            }
            tried.add(instance)
            try {
                val api = buildApi(instance)
                val result = block(api)
                return Result.success(result)
            } catch (e: Exception) {
                Log.w("MusicRepository", "Instance $instance failed: ${e.message}, trying next...")
                instanceManager.nextInstance()
            }
        }
        return Result.failure(Exception("Все серверы недоступны. Попробуй позже или включи VPN."))
    }

    suspend fun search(query: String, page: Int = 1): Result<List<SearchResult>> =
        withFallback { api ->
            api.search(query = query, page = page).filter { it.type == "video" }
        }

    suspend fun getVideoDetail(videoId: String): Result<VideoDetail> =
        withFallback { api -> api.getVideo(videoId) }

    suspend fun getTrending(): Result<List<SearchResult>> =
        withFallback { api ->
            api.getTrending().map { it.toSearchResult() }
        }
}
