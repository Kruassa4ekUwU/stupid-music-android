package com.stupidmusic.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stupidmusic.app.data.model.Track
import com.stupidmusic.app.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MusicRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            _query
                .debounce(600)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { search(it) }
        }
    }

    fun onQuery(q: String) {
        _query.value = q
        if (q.isEmpty()) { _results.value = emptyList(); _error.value = null }
    }

    private suspend fun search(q: String) {
        _isLoading.value = true
        _error.value = null
        repo.search(q).fold(
            onSuccess = { _results.value = it },
            onFailure = { _error.value = it.message ?: "Ошибка поиска" }
        )
        _isLoading.value = false
    }
}
