package com.stupidmusic.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stupidmusic.app.data.model.Track
import com.stupidmusic.app.ui.components.TrackCard
import com.stupidmusic.app.ui.components.TrackRow
import com.stupidmusic.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    activeId: String?,
    onTrackClick: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel()
) {
    val tracks by vm.tracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text(
                    text = "Stupid Music",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Музыка без цензуры 🎵",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when {
            isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            error != null -> item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("😢 ${error}", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { vm.load() }) { Text("Повторить") }
                }
            }
            tracks.isNotEmpty() -> {
                item {
                    Text(
                        text = "Популярное",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        tracks.take(8).forEach { track ->
                            TrackCard(
                                track = track,
                                isActive = track.videoId == activeId,
                                onClick = { onTrackClick(track, tracks) }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Все треки",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(tracks, key = { it.videoId }) { track ->
                    TrackRow(
                        track = track,
                        isActive = track.videoId == activeId,
                        onClick = { onTrackClick(track, tracks) }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
