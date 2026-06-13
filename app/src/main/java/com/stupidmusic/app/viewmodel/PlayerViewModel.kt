package com.stupidmusic.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stupidmusic.app.data.model.PlayerState
import com.stupidmusic.app.data.model.Track
import com.stupidmusic.app.extractor.YoutubeExtractor
import com.stupidmusic.app.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
    application: Application
) : AndroidViewModel(application) {

    val state: StateFlow<PlayerState> = controller.state

    init {
        // Init NewPipe extractor
        YoutubeExtractor.init(application)
        controller.init()
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                controller.updatePosition()
            }
        }
    }

    fun play(track: Track, queue: List<Track> = emptyList()) {
        if (queue.isNotEmpty()) controller.setQueue(queue)
        controller.setLoading(track)
        viewModelScope.launch {
            val url = YoutubeExtractor.getAudioUrl(track.videoId)
            if (url != null) {
                controller.playUrl(track, url)
            } else {
                controller.setError("Не удалось загрузить. Попробуй другой трек.")
            }
        }
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
