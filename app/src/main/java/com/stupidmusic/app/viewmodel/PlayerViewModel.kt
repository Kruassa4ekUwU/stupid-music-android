package com.stupidmusic.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stupidmusic.app.data.model.PlayerState
import com.stupidmusic.app.data.model.SearchResult
import com.stupidmusic.app.data.repository.MusicRepository
import com.stupidmusic.app.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val repository: MusicRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerController.playerState

    fun initPlayer() {
        playerController.initController()
    }

    fun playTrack(track: SearchResult) {
        viewModelScope.launch {
            repository.getVideoDetail(track.videoId).fold(
                onSuccess = { detail ->
                    val audioUrl = detail.bestAudioUrl
                    if (audioUrl.isNotEmpty()) {
                        playerController.play(track, audioUrl)
                    }
                },
                onFailure = {
                    // Fallback: try direct stream URL
                    val fallbackUrl = "https://www.youtube.com/watch?v=${track.videoId}"
                    playerController.play(track, fallbackUrl)
                }
            )
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()

    fun seekTo(position: Long) = playerController.seekTo(position)

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
