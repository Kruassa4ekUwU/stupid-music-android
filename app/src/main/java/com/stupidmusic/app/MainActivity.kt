package com.stupidmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.stupidmusic.app.ui.components.MiniPlayer
import com.stupidmusic.app.ui.components.NowPlayingScreen
import com.stupidmusic.app.ui.screens.HomeScreen
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
                AppRoot()
            }
        }
    }
}

sealed class Nav(val route: String, val label: String) {
    object Home : Nav("home", "Главная")
    object Search : Nav("search", "Поиск")
}

@Composable
fun AppRoot() {
    val playerVm: PlayerViewModel = hiltViewModel()
    val playerState by playerVm.state.collectAsState()
    val navController = rememberNavController()
    var showPlayer by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = showPlayer,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NowPlayingScreen(
            state = playerState,
            onCollapse = { showPlayer = false },
            onToggle = playerVm::togglePlayPause,
            onSeek = playerVm::seekTo,
            onNext = playerVm::skipNext,
            onPrev = playerVm::skipPrev
        )
        return@AnimatedVisibility
    }

    if (!showPlayer) {
        val navItems = listOf(Nav.Home, Nav.Search)
        val backStack by navController.currentBackStackEntryAsState()
        val current = backStack?.destination

        Scaffold(
            bottomBar = {
                Column {
                    MiniPlayer(
                        state = playerState,
                        onExpand = { showPlayer = true },
                        onToggle = playerVm::togglePlayPause,
                        onNext = playerVm::skipNext
                    )
                    NavigationBar {
                        navItems.forEach { nav ->
                            NavigationBarItem(
                                selected = current?.hierarchy?.any { it.route == nav.route } == true,
                                onClick = {
                                    navController.navigate(nav.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        when (nav) {
                                            Nav.Home -> Icons.Rounded.Home
                                            Nav.Search -> Icons.Rounded.Search
                                        },
                                        contentDescription = nav.label
                                    )
                                },
                                label = { Text(nav.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Nav.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Nav.Home.route) {
                    HomeScreen(
                        activeId = playerState.currentTrack?.videoId,
                        onTrackClick = { track, queue -> playerVm.play(track, queue) }
                    )
                }
                composable(Nav.Search.route) {
                    SearchScreen(
                        activeId = playerState.currentTrack?.videoId,
                        onTrackClick = { track, queue -> playerVm.play(track, queue) }
                    )
                }
            }
        }
    }
}
