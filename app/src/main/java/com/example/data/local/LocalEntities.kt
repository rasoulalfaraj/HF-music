package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class LocalSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val releaseYear: Int,
    val durationSeconds: Int,
    val youtubeVideoId: String,
    val audioUrl: String,
    val thumbnailUrl: String,
    val lyrics: String,
    val isOfflineDownloaded: Boolean,
    val isFavorite: Boolean,
    val playCount: Int,
    val downloadedAt: Long = 0L
)

@Entity(tableName = "playlists")
data class LocalPlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val colorHex: String,
    val shareCode: String,
    val songIdsJson: String, // Comma separated IDs
    val isCloudSynced: Boolean,
    val isSystemPlaylist: Boolean,
    val createdAt: Long
)
