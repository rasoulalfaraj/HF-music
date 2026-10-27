package com.example.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val colorHex: String = "#9D4EDD",
    val shareCode: String = "",
    val songIds: List<String> = emptyList(),
    val isCloudSynced: Boolean = true,
    val isSystemPlaylist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
