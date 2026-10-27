package com.example.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val releaseYear: Int,
    val durationSeconds: Int,
    val youtubeVideoId: String,
    val audioUrl: String,
    val thumbnailUrl: String,
    val lyrics: String = "",
    val isOfflineDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
