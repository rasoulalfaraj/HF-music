package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM songs")
    fun getAllSongsFlow(): Flow<List<LocalSongEntity>>

    @Query("SELECT * FROM songs WHERE isOfflineDownloaded = 1")
    fun getDownloadedSongsFlow(): Flow<List<LocalSongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongsFlow(): Flow<List<LocalSongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): LocalSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<LocalSongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: LocalSongEntity)

    @Update
    suspend fun updateSong(song: LocalSongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET isOfflineDownloaded = :isDownloaded, downloadedAt = :timestamp WHERE id = :songId")
    suspend fun updateDownloadStatus(songId: String, isDownloaded: Boolean, timestamp: Long)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylistsFlow(): Flow<List<LocalPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: LocalPlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<LocalPlaylistEntity>)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("UPDATE playlists SET songIdsJson = :songIdsJson WHERE id = :playlistId")
    suspend fun updatePlaylistSongs(playlistId: String, songIdsJson: String)

    @Query("UPDATE playlists SET isCloudSynced = :synced WHERE id = :playlistId")
    suspend fun updatePlaylistSyncStatus(playlistId: String, synced: Boolean)
}
