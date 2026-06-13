package com.stupidmusic.app.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.stupidmusic.app.data.model.PlayerState
import com.stupidmusic.app.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var controller: MediaController? = null

    fun init() {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.addListener(listener)
        }, MoreExecutors.directExecutor())
    }

    fun play(track: Track) {
        _state.value = _state.value.copy(currentTrack = track, isLoading = true, error = null)
        // Use youtube-nocookie for better compatibility
        val url = "https://www.youtube-nocookie.com/watch?v=${track.videoId}"
        controller?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekTo(ms: Long) { controller?.seekTo(ms) }

    fun skipNext() {
        val queue = _state.value.queue
        val cur = _state.value.currentTrack ?: return
        val idx = queue.indexOfFirst { it.videoId == cur.videoId }
        if (idx < queue.size - 1) play(queue[idx + 1])
    }

    fun skipPrev() {
        val queue = _state.value.queue
        val cur = _state.value.currentTrack ?: return
        val idx = queue.indexOfFirst { it.videoId == cur.videoId }
        if (idx > 0) play(queue[idx - 1])
    }

    fun setQueue(tracks: List<Track>) {
        _state.value = _state.value.copy(queue = tracks)
    }

    fun updatePosition() {
        controller?.let {
            _state.value = _state.value.copy(
                currentPositionMs = it.currentPosition.coerceAtLeast(0),
                durationMs = it.duration.coerceAtLeast(0)
            )
        }
    }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying, isLoading = false)
        }
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> _state.value = _state.value.copy(isLoading = true)
                Player.STATE_READY -> _state.value = _state.value.copy(
                    isLoading = false,
                    durationMs = controller?.duration?.coerceAtLeast(0) ?: 0
                )
                Player.STATE_ENDED -> skipNext()
                else -> {}
            }
        }
    }
}
