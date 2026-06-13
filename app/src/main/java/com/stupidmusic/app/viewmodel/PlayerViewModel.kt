package com.stupidmusic.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stupidmusic.app.data.model.PlayerState
import com.stupidmusic.app.data.model.Track
import com.stupidmusic.app.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController
) : ViewModel() {

    val state: StateFlow<PlayerState> = controller.state

    init {
        controller.init()
        // Poll position every second
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                controller.updatePosition()
            }
        }
    }

    fun play(track: Track, queue: List<Track> = emptyList()) {
        if (queue.isNotEmpty()) controller.setQueue(queue)
        controller.play(track)
    }

    fun togglePlayPause() = controller.togglePlayPause()
    fun seekTo(ms: Long) = controller.seekTo(ms)
    fun skipNext() = controller.skipNext()
    fun skipPrev() = controller.skipPrev()

    override fun onCleared() {
        controller.release()
    }
}
