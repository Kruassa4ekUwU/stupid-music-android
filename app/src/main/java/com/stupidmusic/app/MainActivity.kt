package com.stupidmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stupidmusic.app.ui.components.MiniPlayer
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
                MainApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Главная")
    object Search : Screen("search", "Поиск")
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsState()

    var showNowPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playerViewModel.initPlayer()
    }

    val navItems = listOf(Screen.Home, Screen.Search)
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination

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
                MiniPlayer(
                    playerState = playerState,
                    onClick = { showNowPlaying = true },
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
