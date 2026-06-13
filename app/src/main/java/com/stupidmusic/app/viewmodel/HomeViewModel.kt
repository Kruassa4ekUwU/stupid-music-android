package com.stupidmusic.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stupidmusic.app.data.model.Track
import com.stupidmusic.app.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MusicRepository
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Популярные запросы вместо Trending API
    private val popularQueries = listOf(
        "top hits 2024",
        "popular songs 2024",
        "best music 2024",
        "хиты 2024",
        "музыка 2024"
    )

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.search(popularQueries.random()).fold(
                onSuccess = { _tracks.value = it },
                onFailure = { _error.value = it.message ?: "Ошибка загрузки" }
            )
            _isLoading.value = false
        }
    }
}
