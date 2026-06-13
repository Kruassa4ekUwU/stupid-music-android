package com.stupidmusic.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
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
import com.stupidmusic.app.ui.components.TrackRow
import com.stupidmusic.app.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    activeId: String?,
    onTrackClick: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier,
    vm: SearchViewModel = hiltViewModel()
) {
    val query by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Поиск",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Артист, трек, альбом...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        when {
            isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> item {
                Text(
                    text = "😢 ${error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            }
            query.isEmpty() -> item {
                Box(Modifier.fillMaxWidth().padding(vertical = 64.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Найди любой трек",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            results.isEmpty() && !isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) {
                    Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                item {
                    Text(
                        text = "Результаты",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(results, key = { it.videoId }) { track ->
                    TrackRow(
                        track = track,
                        isActive = track.videoId == activeId,
                        onClick = { onTrackClick(track, results) }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
