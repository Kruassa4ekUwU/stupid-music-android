package com.stupidmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stupidmusic.app.ui.screens.HomeScreen
import com.stupidmusic.app.ui.screens.NowPlayingScreen
import com.stupidmusic.app.ui.screens.SearchScreen
import com.stupidmusic.app.ui.theme.StupidMusicTheme
import com.stupidmusic.app.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StupidMusicTheme {
                StupidMusicApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Главная")
    object Search : Screen("search", "Поиск")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StupidMusicApp() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

    var showNowPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playerViewModel.initPlayer()
    }

    val navItems = listOf(Screen.Home, Screen.Search)
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination

    // Full-screen NowPlaying sheet
    if (showNowPlaying) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            NowPlayingScreen(
                playerState = playerState,
                onTogglePlayPause = playerViewModel::togglePlayPause,
                onSeek = playerViewModel::seekTo,
                onDismiss = { showNowPlaying = false }
            )
        }
        return
    }

    Scaffold(
        bottomBar = {
            Column {
                MiniPlayerBar(
                    playerState = playerState,
                    onMiniPlayerClick = { showNowPlaying = true },
                    onTogglePlayPause = playerViewModel::togglePlayPause
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Home -> Icons.Rounded.Home
                                        Screen.Search -> Icons.Rounded.Search
                                    },
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    currentTrackId = playerState.currentTrack?.videoId,
                    onTrackClick = { playerViewModel.playTrack(it) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    currentTrackId = playerState.currentTrack?.videoId,
                    onTrackClick = { playerViewModel.playTrack(it) }
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    playerState: com.stupidmusic.app.data.model.PlayerState,
    onMiniPlayerClick: () -> Unit,
    onTogglePlayPause: () -> Unit
) {
    com.stupidmusic.app.ui.components.MiniPlayer(
        playerState = playerState,
        onClick = onMiniPlayerClick,
        onTogglePlayPause = onTogglePlayPause
    )
}
