package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdvancedSearchFilter
import com.example.data.MusicCatalog
import com.example.data.MusicRepository
import com.example.data.SortOption
import com.example.data.local.MusicDatabase
import com.example.model.Playlist
import com.example.model.Song
import com.example.playback.MusicPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val titleArabic: String) {
    HOME("الرئيسية"),
    SEARCH("البحث المتقدم"),
    LIBRARY("قوائمي"),
    OFFLINE("بدون إنترنت"),
    CLOUD("المزامنة والمشاركة")
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicDatabase.getDatabase(application)
    val repository = MusicRepository(database.musicDao())
    val playerManager = MusicPlayerManager(application)

    val allSongs: StateFlow<List<Song>> = repository.allSongsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MusicCatalog.sampleSongs
    )

    val downloadedSongs: StateFlow<List<Song>> = repository.downloadedSongsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MusicCatalog.sampleSongs.filter { it.isOfflineDownloaded }
    )

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MusicCatalog.sampleSongs.filter { it.isFavorite }
    )

    val playlists: StateFlow<List<Playlist>> = repository.playlistsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MusicCatalog.defaultPlaylists
    )

    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    // Advanced Multi-Criteria Search State
    private val _advancedFilter = MutableStateFlow(AdvancedSearchFilter())
    val advancedFilter: StateFlow<AdvancedSearchFilter> = _advancedFilter.asStateFlow()

    // Calculated filtered songs
    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, _advancedFilter) { songs, filter ->
        repository.filterSongs(songs, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MusicCatalog.sampleSongs
    )

    // Dialog & UI transient states
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    private val _playlistToShare = MutableStateFlow<Playlist?>(null)
    val playlistToShare: StateFlow<Playlist?> = _playlistToShare.asStateFlow()

    private val _showImportCodeDialog = MutableStateFlow(false)
    val showImportCodeDialog: StateFlow<Boolean> = _showImportCodeDialog.asStateFlow()

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast: StateFlow<String?> = _statusToast.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastCloudSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastCloudSyncTimestamp: StateFlow<Long> = _lastCloudSyncTimestamp.asStateFlow()

    fun switchTab(tab: AppNavTab) {
        _currentTab.value = tab
        if (_selectedPlaylist.value != null && tab != AppNavTab.LIBRARY) {
            _selectedPlaylist.value = null
        }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    // --- Search Filter Modifiers ---
    fun updateSearchQuery(query: String) {
        _advancedFilter.value = _advancedFilter.value.copy(query = query)
    }

    fun updateArtistFilter(artist: String) {
        _advancedFilter.value = _advancedFilter.value.copy(artist = artist)
    }

    fun updateAlbumFilter(album: String) {
        _advancedFilter.value = _advancedFilter.value.copy(album = album)
    }

    fun updateGenreFilter(genre: String) {
        _advancedFilter.value = _advancedFilter.value.copy(genre = genre)
    }

    fun updateYearRangeFilter(year: String) {
        _advancedFilter.value = _advancedFilter.value.copy(yearRange = year)
    }

    fun updateOnlyOfflineFilter(onlyOffline: Boolean) {
        _advancedFilter.value = _advancedFilter.value.copy(onlyOffline = onlyOffline)
    }

    fun updateSortOption(sortOption: SortOption) {
        _advancedFilter.value = _advancedFilter.value.copy(sortOption = sortOption)
    }

    fun resetFilters() {
        _advancedFilter.value = AdvancedSearchFilter()
    }

    val activeCriteriaCount: Int
        get() {
            var count = 0
            val f = _advancedFilter.value
            if (f.query.isNotBlank()) count++
            if (f.artist.isNotBlank()) count++
            if (f.album.isNotBlank()) count++
            if (f.genre != "الكل (All)") count++
            if (f.yearRange != "جميع السنوات") count++
            if (f.onlyOffline) count++
            return count
        }

    // --- Playback Actions ---
    fun playSong(song: Song, queue: List<Song> = listOf(song), isYouTube: Boolean = false) {
        playerManager.playSong(song, queue, isYouTube)
        viewModelScope.launch {
            repository.recordSongPlayed(song.id)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
            _statusToast.value = if (!song.isFavorite) "تمت الإضافة للمفضلة ❤️" else "تمت الإزالة من المفضلة"
        }
    }

    fun toggleDownload(song: Song) {
        viewModelScope.launch {
            repository.toggleDownload(song.id, song.isOfflineDownloaded)
            _statusToast.value = if (!song.isOfflineDownloaded)
                "تم حفظ '${song.title}' للاستماع بدون نت 📥"
            else
                "تم حذف التحميل دون اتصال"
        }
    }

    // --- Playlist & Cloud Actions ---
    fun openCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = true
    }

    fun closeCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = false
    }

    fun createPlaylist(title: String, description: String, colorHex: String) {
        viewModelScope.launch {
            val pl = repository.createPlaylist(title, description, colorHex)
            _showCreatePlaylistDialog.value = false
            _statusToast.value = "تم إنشاء قائمة '${pl.title}' بنجاح"
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
            _statusToast.value = "تم حذف قائمة التشغيل"
        }
    }

    fun promptAddToPlaylist(song: Song) {
        _songToAddToPlaylist.value = song
    }

    fun dismissAddToPlaylist() {
        _songToAddToPlaylist.value = null
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlist, song.id)
            _songToAddToPlaylist.value = null
            _statusToast.value = "تمت إضافة '${song.title}' إلى ${playlist.title}"
        }
    }

    fun removeSongFromPlaylist(playlist: Playlist, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlist, songId)
            // Refresh selected playlist if active
            _selectedPlaylist.value = _selectedPlaylist.value?.let {
                it.copy(songIds = it.songIds.filter { id -> id != songId })
            }
            _statusToast.value = "تمت إزالة الأغنية من القائمة"
        }
    }

    fun promptSharePlaylist(playlist: Playlist) {
        _playlistToShare.value = playlist
    }

    fun dismissSharePlaylist() {
        _playlistToShare.value = null
    }

    fun openImportDialog() {
        _showImportCodeDialog.value = true
    }

    fun closeImportDialog() {
        _showImportCodeDialog.value = false
    }

    fun importPlaylistFromCode(code: String) {
        viewModelScope.launch {
            val imported = repository.importPlaylistFromShareCode(code, allSongs.value)
            _showImportCodeDialog.value = false
            if (imported != null) {
                _statusToast.value = "تم استيراد قائمة '${imported.title}' بنجاح! 🎵"
                _selectedPlaylist.value = imported
                _currentTab.value = AppNavTab.LIBRARY
            } else {
                _statusToast.value = "تعذر العثور على قائمة بالرمز المدخل"
            }
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            kotlinx.coroutines.delay(1200) // Realistic cloud sync delay
            val count = repository.syncAllPlaylistsToCloud()
            _lastCloudSyncTimestamp.value = System.currentTimeMillis()
            _isCloudSyncing.value = false
            _statusToast.value = "تمت مزامنة $count قوائم مع السحابة بنجاح ☁️"
        }
    }

    fun clearToast() {
        _statusToast.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
