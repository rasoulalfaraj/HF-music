package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.playback.RepeatMode as PlayerRepeatMode
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.YouTubePlayerView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkDivider
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
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun NowPlayingScreen(
    viewModel: MusicViewModel,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.playerManager.currentSong.collectAsState()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val currentPosMs by viewModel.playerManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.playerManager.durationMs.collectAsState()
    val isYouTubeMode by viewModel.playerManager.isYouTubeMode.collectAsState()
    val isShuffle by viewModel.playerManager.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.playerManager.repeatMode.collectAsState()

    var showLyrics by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (currentSong == null) {
        onCollapseClick()
        return
    }

    val song = currentSong!!

    // Rotating vinyl animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val currentSeconds = (currentPosMs / 1000).toInt()
    val totalSeconds = (durationMs / 1000).toInt().coerceAtLeast(1)
    val formattedCurrent = "%d:%02d".format(currentSeconds / 60, currentSeconds % 60)
    val formattedTotal = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar: Collapse Button, Title, and More
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapseClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("collapse_now_playing_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "تصغير",
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "يتم التشغيل الآن",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Text(
                    text = song.album,
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "استمع إلى أغنية '${song.title}' لـ ${song.artist} على Have Fun Music! https://youtu.be/${song.youtubeVideoId}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الأغنية"))
                },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "مشاركة الأغنية",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mode Switcher: Audio Player vs Official YouTube Player
        TabRow(
            selectedTabIndex = if (isYouTubeMode) 1 else 0,
            containerColor = DarkSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (isYouTubeMode) 1 else 0]),
                    color = if (isYouTubeMode) YouTubeRed else NeonCyan
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
        ) {
            Tab(
                selected = !isYouTubeMode,
                onClick = { if (isYouTubeMode) viewModel.playerManager.toggleYouTubeMode() },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشغل الصوت", fontWeight = FontWeight.Bold)
                    }
                }
            )

            Tab(
                selected = isYouTubeMode,
                onClick = { if (!isYouTubeMode) viewModel.playerManager.toggleYouTubeMode() },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("يوتيوب الرسمي", fontWeight = FontWeight.Bold, color = if (isYouTubeMode) YouTubeRed else TextSecondary)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Content Area: Official YouTube Player OR Glowing Vinyl Artwork
        if (isYouTubeMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                YouTubePlayerView(
                    videoId = song.youtubeVideoId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Audio Artwork with Rotating Record
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl outer groove ring
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF1B1D28),
                                    Color(0xFF10121A),
                                    Color(0xFF25293A),
                                    Color(0xFF10121A)
                                )
                            )
                        )
                )

                // Rotating Album Artwork
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .rotate(if (isPlaying) rotationAngle else 0f),
                    contentScale = ContentScale.Crop
                )

                // Center Spindle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waveform Visualizer
            WaveformVisualizer(
                isPlaying = isPlaying,
                barCount = 24,
                maxHeight = 36.dp,
                barWidth = 4.dp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Song Title, Artist, and Badges
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = NeonCyan
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Metadata badges: Release Year, Genre, Offline badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = "سنة ${song.releaseYear}", color = TextSecondary, fontSize = 11.sp)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonPink.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = song.genre, color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (song.isOfflineDownloaded) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OfflineEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = "محملة بدون نت", color = OfflineEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seek Bar Progress Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentPosMs.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                onValueChange = { newPos ->
                    viewModel.playerManager.seekTo(newPos.toLong())
                },
                valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = DarkDivider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_seek_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formattedCurrent, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Text(text = formattedTotal, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Playback Controls Row: Shuffle, Previous, Play/Pause, Next, Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(
                onClick = { viewModel.playerManager.toggleShuffle() },
                modifier = Modifier.testTag("toggle_shuffle_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "تشغيل عشوائي",
                    tint = if (isShuffle) NeonCyan else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous
            IconButton(
                onClick = { viewModel.playerManager.playPrevious() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("skip_prev_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "السابق",
                    tint = TextPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Play / Pause Primary Button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonCyan, ElectricBlue)
                        )
                    )
                    .clickable { viewModel.playerManager.togglePlayPause() }
                    .testTag("main_play_pause_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Next
            IconButton(
                onClick = { viewModel.playerManager.playNext() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("skip_next_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "التالي",
                    tint = TextPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Repeat
            IconButton(
                onClick = { viewModel.playerManager.cycleRepeatMode() },
                modifier = Modifier.testTag("cycle_repeat_btn")
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        PlayerRepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "تكرار",
                    tint = if (repeatMode != PlayerRepeatMode.OFF) NeonCyan else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Secondary Action Icons: Favorite, Download, Add to playlist, Lyrics toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite
            IconButton(onClick = { viewModel.toggleFavorite(song) }) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "المفضلة",
                    tint = if (song.isFavorite) NeonPink else TextSecondary
                )
            }

            // Download
            IconButton(onClick = { viewModel.toggleDownload(song) }) {
                Icon(
                    imageVector = if (song.isOfflineDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = "تحميل بدون نت",
                    tint = if (song.isOfflineDownloaded) OfflineEmerald else TextSecondary
                )
            }

            // Add to Playlist
            IconButton(onClick = { viewModel.promptAddToPlaylist(song) }) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "إضافة لقائمة",
                    tint = NeonCyan
                )
            }

            // Lyrics toggle
            IconButton(onClick = { showLyrics = !showLyrics }) {
                Text(
                    text = "كلمات",
                    fontWeight = FontWeight.Bold,
                    color = if (showLyrics) NeonCyan else TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Lyrics Card Expandable
        AnimatedVisibility(visible = showLyrics) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "كلمات الأغنية 📜",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = song.lyrics.ifBlank { "كلمات هذه الأغنية غير متوفرة حالياً." },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            lineHeight = 24.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
