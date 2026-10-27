package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MiniPlayer
import com.example.ui.dialogs.AddToPlaylistDialog
import com.example.ui.dialogs.CreatePlaylistDialog
import com.example.ui.dialogs.ImportPlaylistDialog
import com.example.ui.dialogs.SharePlaylistDialog
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.OfflineScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.HaveFunMusicTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HaveFunMusicTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MusicViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    val currentSong by viewModel.playerManager.currentSong.collectAsState()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val currentPosMs by viewModel.playerManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.playerManager.durationMs.collectAsState()
    val isYouTubeMode by viewModel.playerManager.isYouTubeMode.collectAsState()

    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsState()
    val playlistToShare by viewModel.playlistToShare.collectAsState()
    val showImportCodeDialog by viewModel.showImportCodeDialog.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val statusToast by viewModel.statusToast.collectAsState()

    val context = LocalContext.current

    // Show toast messages when triggered
    LaunchedEffect(statusToast) {
        statusToast?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle Android system Back button cleanly
    BackHandler(enabled = isNowPlayingExpanded || selectedPlaylist != null || currentTab != AppNavTab.HOME) {
        if (isNowPlayingExpanded) {
            viewModel.setNowPlayingExpanded(false)
        } else if (selectedPlaylist != null) {
            viewModel.selectPlaylist(null)
        } else if (currentTab != AppNavTab.HOME) {
            viewModel.switchTab(AppNavTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Docked MiniPlayer floating directly above the bottom bar
                if (!isNowPlayingExpanded) {
                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPosMs,
                        durationMs = durationMs,
                        isYouTubeMode = isYouTubeMode,
                        onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                        onSkipNext = { viewModel.playerManager.playNext() },
                        onExpandClick = { viewModel.setNowPlayingExpanded(true) }
                    )
                }

                // Standard M3 Navigation Bar with 5 tabs
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.HOME,
                        onClick = { viewModel.switchTab(AppNavTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "الرئيسية"
                            )
                        },
                        label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.SEARCH,
                        onClick = { viewModel.switchTab(AppNavTab.SEARCH) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "البحث المتقدم"
                            )
                        },
                        label = { Text("البحث", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_search")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.LIBRARY,
                        onClick = { viewModel.switchTab(AppNavTab.LIBRARY) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "قوائمي"
                            )
                        },
                        label = { Text("قوائمي", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_library")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.OFFLINE,
                        onClick = { viewModel.switchTab(AppNavTab.OFFLINE) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = "بدون إنترنت"
                            )
                        },
                        label = { Text("بدون نت", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_offline")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.CLOUD,
                        onClick = { viewModel.switchTab(AppNavTab.CLOUD) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "المزامنة والمشاركة"
                            )
                        },
                        label = { Text("مزامنة", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_cloud")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Views
            when (currentTab) {
                AppNavTab.HOME -> HomeScreen(viewModel = viewModel)
                AppNavTab.SEARCH -> SearchScreen(viewModel = viewModel)
                AppNavTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                AppNavTab.OFFLINE -> OfflineScreen(viewModel = viewModel)
                AppNavTab.CLOUD -> CloudSyncScreen(viewModel = viewModel)
            }

            // Fullscreen Now Playing Overlay
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                NowPlayingScreen(
                    viewModel = viewModel,
                    onCollapseClick = { viewModel.setNowPlayingExpanded(false) }
                )
            }
        }
    }

    // Dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.closeCreatePlaylistDialog() },
            onConfirm = { title, desc, color ->
                viewModel.createPlaylist(title, desc, color)
            }
        )
    }

    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onSelectPlaylist = { pl ->
                viewModel.addSongToPlaylist(pl, song)
            },
            onDismiss = { viewModel.dismissAddToPlaylist() }
        )
    }

    playlistToShare?.let { pl ->
        SharePlaylistDialog(
            playlist = pl,
            onDismiss = { viewModel.dismissSharePlaylist() }
        )
    }

    if (showImportCodeDialog) {
        ImportPlaylistDialog(
            onDismiss = { viewModel.closeImportDialog() },
            onImport = { code ->
                viewModel.importPlaylistFromCode(code)
            }
        )
    }
}
