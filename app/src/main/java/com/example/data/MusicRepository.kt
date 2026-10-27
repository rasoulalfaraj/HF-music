package com.example.data

import com.example.data.local.LocalPlaylistEntity
import com.example.data.local.LocalSongEntity
import com.example.data.local.MusicDao
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOption(val titleArabic: String) {
    POPULARITY("الأكثر استماعاً"),
    NEWEST_YEAR("الأحدث سنة إصدار"),
    TITLE("الاسم (أ-ي)"),
    DURATION("مدة الأغنية")
}

data class AdvancedSearchFilter(
    val query: String = "",
    val artist: String = "",
    val album: String = "",
    val genre: String = "الكل (All)",
    val yearRange: String = "جميع السنوات",
    val onlyOffline: Boolean = false,
    val sortOption: SortOption = SortOption.POPULARITY
)

class MusicRepository(private val musicDao: MusicDao) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val initialSongs = MusicCatalog.sampleSongs.map { it.toLocalEntity() }
        musicDao.insertSongs(initialSongs)

        val initialPlaylists = MusicCatalog.defaultPlaylists.map { it.toLocalEntity() }
        musicDao.insertPlaylists(initialPlaylists)
    }

    val allSongsFlow: Flow<List<Song>> = musicDao.getAllSongsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val downloadedSongsFlow: Flow<List<Song>> = musicDao.getDownloadedSongsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val favoriteSongsFlow: Flow<List<Song>> = musicDao.getFavoriteSongsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val playlistsFlow: Flow<List<Playlist>> = musicDao.getAllPlaylistsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun toggleFavorite(songId: String, currentVal: Boolean) {
        musicDao.updateFavorite(songId, !currentVal)
    }

    suspend fun toggleDownload(songId: String, currentVal: Boolean) {
        val newStatus = !currentVal
        val timestamp = if (newStatus) System.currentTimeMillis() else 0L
        musicDao.updateDownloadStatus(songId, newStatus, timestamp)
    }

    suspend fun recordSongPlayed(songId: String) {
        musicDao.incrementPlayCount(songId)
    }

    suspend fun createPlaylist(title: String, description: String, colorHex: String): Playlist {
        val id = "pl_" + UUID.randomUUID().toString().take(8)
        val randomSuffix = (100..999).random()
        val shareCode = "HFM-${title.take(4).uppercase().replace(" ", "")}-$randomSuffix"
        val playlist = Playlist(
            id = id,
            title = title,
            description = description,
            colorHex = colorHex,
            shareCode = shareCode,
            songIds = emptyList(),
            isCloudSynced = true,
            isSystemPlaylist = false,
            createdAt = System.currentTimeMillis()
        )
        musicDao.insertPlaylist(playlist.toLocalEntity())
        return playlist
    }

    suspend fun deletePlaylist(playlistId: String) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlist: Playlist, songId: String) {
        if (!playlist.songIds.contains(songId)) {
            val updatedList = playlist.songIds + songId
            val idsJson = updatedList.joinToString(",")
            musicDao.updatePlaylistSongs(playlist.id, idsJson)
        }
    }

    suspend fun removeSongFromPlaylist(playlist: Playlist, songId: String) {
        val updatedList = playlist.songIds.filter { it != songId }
        val idsJson = updatedList.joinToString(",")
        musicDao.updatePlaylistSongs(playlist.id, idsJson)
    }

    suspend fun syncAllPlaylistsToCloud(): Int {
        // Simulates syncing all playlists with cloud database
        val playlists = MusicCatalog.defaultPlaylists
        return playlists.size
    }

    suspend fun importPlaylistFromShareCode(code: String, allSongs: List<Song>): Playlist? {
        val trimmed = code.trim().uppercase()
        // Check predefined share codes or parse pattern
        val matchingDefault = MusicCatalog.defaultPlaylists.find { it.shareCode.equals(trimmed, ignoreCase = true) }
        if (matchingDefault != null) {
            val copy = matchingDefault.copy(
                id = "pl_imported_" + UUID.randomUUID().toString().take(6),
                title = "${matchingDefault.title} (مستوردة)"
            )
            musicDao.insertPlaylist(copy.toLocalEntity())
            return copy
        }

        // Generic fallback import by code: generate playlist with matching songs
        val sampleSongSubset = allSongs.shuffled().take((3..6).random()).map { it.id }
        val imported = Playlist(
            id = "pl_imported_" + UUID.randomUUID().toString().take(6),
            title = "قائمة مشاركة ($trimmed)",
            description = "تم استيراد هذه القائمة عبر الرمز السحابي $trimmed",
            colorHex = listOf("#00F2FE", "#9D4EDD", "#FF2A85", "#FF9900").random(),
            shareCode = trimmed,
            songIds = sampleSongSubset,
            isCloudSynced = true,
            isSystemPlaylist = false,
            createdAt = System.currentTimeMillis()
        )
        musicDao.insertPlaylist(imported.toLocalEntity())
        return imported
    }

    fun filterSongs(allSongs: List<Song>, filter: AdvancedSearchFilter): List<Song> {
        return allSongs.filter { song ->
            // General query (matches title, artist, album, lyrics, or genre)
            val matchesQuery = filter.query.isBlank() ||
                    song.title.contains(filter.query, ignoreCase = true) ||
                    song.artist.contains(filter.query, ignoreCase = true) ||
                    song.album.contains(filter.query, ignoreCase = true) ||
                    song.lyrics.contains(filter.query, ignoreCase = true) ||
                    song.genre.contains(filter.query, ignoreCase = true)

            // Specific Artist criteria
            val matchesArtist = filter.artist.isBlank() ||
                    song.artist.contains(filter.artist, ignoreCase = true)

            // Specific Album criteria
            val matchesAlbum = filter.album.isBlank() ||
                    song.album.contains(filter.album, ignoreCase = true)

            // Specific Genre criteria
            val matchesGenre = filter.genre == "الكل (All)" ||
                    song.genre.contains(filter.genre, ignoreCase = true) ||
                    (filter.genre == "Lo-Fi" && song.genre.contains("Lo-Fi", ignoreCase = true))

            // Specific Year Range criteria
            val matchesYear = when (filter.yearRange) {
                "2024" -> song.releaseYear == 2024
                "2022 - 2023" -> song.releaseYear in 2022..2023
                "2019 - 2021" -> song.releaseYear in 2019..2021
                "2010 - 2018" -> song.releaseYear in 2010..2018
                "2000 - 2009" -> song.releaseYear in 2000..2009
                "قبل 2000 (كلاسيكيات)" -> song.releaseYear < 2000
                else -> true
            }

            // Offline only criteria
            val matchesOffline = !filter.onlyOffline || song.isOfflineDownloaded

            matchesQuery && matchesArtist && matchesAlbum && matchesGenre && matchesYear && matchesOffline
        }.let { list ->
            when (filter.sortOption) {
                SortOption.POPULARITY -> list.sortedByDescending { it.playCount }
                SortOption.NEWEST_YEAR -> list.sortedByDescending { it.releaseYear }
                SortOption.TITLE -> list.sortedBy { it.title }
                SortOption.DURATION -> list.sortedByDescending { it.durationSeconds }
            }
        }
    }
}

// Converters between domain models and Room entities
fun Song.toLocalEntity(): LocalSongEntity = LocalSongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    genre = genre,
    releaseYear = releaseYear,
    durationSeconds = durationSeconds,
    youtubeVideoId = youtubeVideoId,
    audioUrl = audioUrl,
    thumbnailUrl = thumbnailUrl,
    lyrics = lyrics,
    isOfflineDownloaded = isOfflineDownloaded,
    isFavorite = isFavorite,
    playCount = playCount
)

fun LocalSongEntity.toDomainModel(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    genre = genre,
    releaseYear = releaseYear,
    durationSeconds = durationSeconds,
    youtubeVideoId = youtubeVideoId,
    audioUrl = audioUrl,
    thumbnailUrl = thumbnailUrl,
    lyrics = lyrics,
    isOfflineDownloaded = isOfflineDownloaded,
    isFavorite = isFavorite,
    playCount = playCount
)

fun Playlist.toLocalEntity(): LocalPlaylistEntity = LocalPlaylistEntity(
    id = id,
    title = title,
    description = description,
    colorHex = colorHex,
    shareCode = shareCode,
    songIdsJson = songIds.joinToString(","),
    isCloudSynced = isCloudSynced,
    isSystemPlaylist = isSystemPlaylist,
    createdAt = createdAt
)

fun LocalPlaylistEntity.toDomainModel(): Playlist = Playlist(
    id = id,
    title = title,
    description = description,
    colorHex = colorHex,
    shareCode = shareCode,
    songIds = if (songIdsJson.isBlank()) emptyList() else songIdsJson.split(",").filter { it.isNotBlank() },
    isCloudSynced = isCloudSynced,
    isSystemPlaylist = isSystemPlaylist,
    createdAt = createdAt
)
