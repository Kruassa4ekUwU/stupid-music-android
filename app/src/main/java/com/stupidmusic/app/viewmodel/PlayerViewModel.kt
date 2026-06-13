package com.stupidmusic.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stupidmusic.app.data.api.PipedApi
import com.stupidmusic.app.data.model.PlayerState
import com.stupidmusic.app.data.model.Track
import com.stupidmusic.app.di.AppModule
import com.stupidmusic.app.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
    private val json: Json,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    val state: StateFlow<PlayerState> = controller.state

    init {
        controller.init()
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                controller.updatePosition()
            }
        }
    }

    // Build Piped API for a specific instance
    private fun buildPipedApi(baseUrl: String): PipedApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PipedApi::class.java)

    fun play(track: Track, queue: List<Track> = emptyList()) {
        if (queue.isNotEmpty()) controller.setQueue(queue)
        controller.setLoading(track)
        viewModelScope.launch {
            val url = resolveAudioUrl(track.videoId)
            if (url != null) {
                controller.playUrl(track, url)
            } else {
                controller.setError("Не удалось загрузить трек. Попробуй другой.")
            }
        }
    }

    // Try each Piped instance until one works
    private suspend fun resolveAudioUrl(videoId: String): String? {
        for (instance in AppModule.PIPED_INSTANCES) {
            try {
                val api = buildPipedApi(instance)
                val streams = api.getStreams(videoId)
                val url = streams.bestAudioUrl
                if (url.isNotEmpty()) {
                    android.util.Log.d("PlayerVM", "Got stream from $instance")
                    return url
                }
            } catch (e: Exception) {
                android.util.Log.w("PlayerVM", "Instance $instance failed: ${e.message}")
            }
        }
        return null
    }

    fun togglePlayPause() = controller.togglePlayPause()
    fun seekTo(ms: Long) = controller.seekTo(ms)

    fun skipNext() {
        val s = state.value
        val queue = s.queue
        val cur = s.currentTrack ?: return
        val idx = queue.indexOfFirst { it.videoId == cur.videoId }
        if (idx < queue.size - 1) play(queue[idx + 1], queue)
    }

    fun skipPrev() {
        val s = state.value
        val queue = s.queue
        val cur = s.currentTrack ?: return
        val idx = queue.indexOfFirst { it.videoId == cur.videoId }
        if (idx > 0) play(queue[idx - 1], queue)
    }

    override fun onCleared() { controller.release() }
}
