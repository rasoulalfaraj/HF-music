package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.components.SongItemRow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OfflineEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentPlayingSong by viewModel.playerManager.currentSong.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // App Bar & Welcome Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Have Fun Music",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(YouTubeRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "YouTube Connected",
                                color = YouTubeRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "استمع، استكشف، ونظم أغانيك بدون إنترنت",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                // Cloud Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.clickable { viewModel.switchTab(AppNavTab.CLOUD) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "سحابة متصلة",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مزامنة",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Spotlight Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2E0854),
                                    Color(0xFF0D1B2A),
                                    Color(0xFF0F394C)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 90.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPink.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "مختارات اليوم المميزة ⭐",
                                color = NeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "استمتع بأقوى نغمات 2024",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "مكتبة ضخمة تجمع روائع اليوتيوب والأغاني المحملة",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    allSongs.firstOrNull()?.let {
                                        viewModel.playSong(it, allSongs, isYouTube = false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("hero_quick_play_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تشغيل سريع", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.switchTab(AppNavTab.SEARCH) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("بحث متقدم", color = NeonCyan, fontSize = 12.sp)
                            }
                        }
                    }

                    // Decorative music badge in right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Access Cards Row: Playlists, Offline, Favorites
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickCard(
                    title = "بدون إنترنت",
                    subtitle = "${downloadedSongs.size} أغنية محملة",
                    icon = Icons.Default.DownloadDone,
                    color = OfflineEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.switchTab(AppNavTab.OFFLINE) }
                )

                QuickCard(
                    title = "قوائمي",
                    subtitle = "${playlists.size} قوائم تشغيل",
                    icon = Icons.Default.Favorite,
                    color = NeonPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.switchTab(AppNavTab.LIBRARY) }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // YouTube Trending Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "YouTube",
                        tint = YouTubeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شائع على يوتيوب (YouTube Music)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Text(
                    text = "عرض الكل",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { viewModel.switchTab(AppNavTab.SEARCH) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Horizontal YouTube Trending Cards
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(allSongs.take(6)) { song ->
                    YouTubeTrendCard(
                        song = song,
                        onClick = {
                            viewModel.playSong(song, allSongs, isYouTube = true)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: All Songs / Top Hits
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أبرز الأغاني العالمية والعربية",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "تصفية متقدمة",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { viewModel.switchTab(AppNavTab.SEARCH) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Songs list items
        items(allSongs, key = { it.id }) { song ->
            val isCurrent = currentPlayingSong?.id == song.id
            SongItemRow(
                song = song,
                isCurrentPlaying = isCurrent,
                onPlayClick = { viewModel.playSong(song, allSongs, isYouTube = false) },
                onToggleFavorite = { viewModel.toggleFavorite(song) },
                onToggleDownload = { viewModel.toggleDownload(song) },
                onAddToPlaylistClick = { viewModel.promptAddToPlaylist(song) },
                onShareClick = {}
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun QuickCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            }
        }
    }
}

@Composable
fun YouTubeTrendCard(
    song: Song,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("trend_card_${song.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // YouTube pill overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(YouTubeRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("YouTube", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.releaseYear} • ${song.genre}",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan),
                    fontSize = 10.sp
                )
            }
        }
    }
}
