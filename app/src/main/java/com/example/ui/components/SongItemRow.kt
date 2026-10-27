package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.OfflineEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun SongItemRow(
    song: Song,
    isCurrentPlaying: Boolean,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDownload: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPlayClick() }
            .testTag("song_item_${song.id}"),
        color = if (isCurrentPlaying) DarkSurfaceVariant else DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with play overlay
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier.size(54.dp),
                    contentScale = ContentScale.Crop
                )

                if (isCurrentPlaying) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(DarkSurface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        WaveformVisualizer(
                            isPlaying = true,
                            barCount = 4,
                            maxHeight = 22.dp,
                            barWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info: Title, Artist & Badges (Genre, Year, Status)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isCurrentPlaying) NeonCyan else TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Badges row: Year, Genre, YouTube, Offline
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Release Year Badge
                    BadgeChip(text = "${song.releaseYear}", color = NeonCyan)

                    // Genre Badge
                    BadgeChip(text = song.genre, color = NeonPink)

                    // YouTube tag
                    if (song.youtubeVideoId.isNotBlank()) {
                        BadgeChip(text = "YouTube", color = YouTubeRed)
                    }

                    // Offline Download tag
                    if (song.isOfflineDownloaded) {
                        BadgeChip(text = "محملة", color = OfflineEmerald)
                    }
                }
            }

            // Duration
            Text(
                text = song.formattedDuration,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Offline Download Button
            IconButton(
                onClick = onToggleDownload,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("download_btn_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isOfflineDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = "تحميل للاستماع بدون نت",
                    tint = if (song.isOfflineDownloaded) OfflineEmerald else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("fav_btn_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "إضافة للمفضلة",
                    tint = if (song.isFavorite) NeonPink else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Overflow Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات إضافية",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("تشغيل الآن", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan) },
                        onClick = {
                            menuExpanded = false
                            onPlayClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("إضافة إلى قائمة تشغيل", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null, tint = NeonCyan) },
                        onClick = {
                            menuExpanded = false
                            onAddToPlaylistClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("مشاركة الأغنية", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = NeonCyan) },
                        onClick = {
                            menuExpanded = false
                            onShareClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
