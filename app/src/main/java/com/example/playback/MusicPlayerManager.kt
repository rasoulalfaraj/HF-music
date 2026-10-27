package com.example.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

class MusicPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isYouTubeMode = MutableStateFlow(false)
    val isYouTubeMode: StateFlow<Boolean> = _isYouTubeMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private var currentIndex = -1

    fun playSong(song: Song, newQueue: List<Song> = listOf(song), startInYouTubeMode: Boolean = false) {
        _queue.value = newQueue
        currentIndex = newQueue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }
        _currentSong.value = song
        _isYouTubeMode.value = startInYouTubeMode

        if (!startInYouTubeMode) {
            startAudioPlayback(song)
        } else {
            // For YouTube mode, pause native player so audio does not overlap
            releaseMediaPlayer()
            _isPlaying.value = true
            _durationMs.value = song.durationSeconds * 1000L
            _currentPositionMs.value = 0L
            startProgressTicker()
        }
    }

    private fun startAudioPlayback(song: Song) {
        releaseMediaPlayer()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(song.audioUrl))
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _durationMs.value = mp.duration.toLong()
                    startProgressTicker()
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback to simulated playback duration if URL is unreachable
                    _isPlaying.value = true
                    _durationMs.value = song.durationSeconds * 1000L
                    startProgressTicker()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            // Simulated playback for demo if remote sample is unavailable
            _isPlaying.value = true
            _durationMs.value = song.durationSeconds * 1000L
            startProgressTicker()
        }
    }

    fun togglePlayPause() {
        if (_isYouTubeMode.value) {
            _isPlaying.value = !_isPlaying.value
            return
        }

        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressTicker()
            }
        } ?: run {
            _currentSong.value?.let { startAudioPlayback(it) }
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        mediaPlayer?.let {
            if (it.isPlaying || it.currentPosition > 0) {
                it.seekTo(positionMs.toInt())
            }
        }
    }

    fun playNext() {
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return

        if (_repeatMode.value == RepeatMode.ONE) {
            _currentSong.value?.let { playSong(it, currentQueue, _isYouTubeMode.value) }
            return
        }

        val nextIndex = if (_isShuffleEnabled.value) {
            (currentQueue.indices).filter { it != currentIndex }.randomOrNull() ?: currentIndex
        } else {
            (currentIndex + 1) % currentQueue.size
        }

        currentIndex = nextIndex
        val nextSong = currentQueue[nextIndex]
        playSong(nextSong, currentQueue, _isYouTubeMode.value)
    }

    fun playPrevious() {
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return

        if (_currentPositionMs.value > 3000L) {
            seekTo(0)
            return
        }

        val prevIndex = if (currentIndex - 1 >= 0) currentIndex - 1 else currentQueue.size - 1
        currentIndex = prevIndex
        val prevSong = currentQueue[prevIndex]
        playSong(prevSong, currentQueue, _isYouTubeMode.value)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleYouTubeMode() {
        val newMode = !_isYouTubeMode.value
        _isYouTubeMode.value = newMode
        _currentSong.value?.let { song ->
            if (newMode) {
                releaseMediaPlayer()
                _isPlaying.value = true
            } else {
                startAudioPlayback(song)
            }
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (currentIndex < _queue.value.size - 1) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0
                }
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition.toLong()
                        _durationMs.value = mp.duration.toLong().coerceAtLeast(1L)
                    }
                } ?: run {
                    // Fallback ticker for YouTube mode or simulated preview
                    val newPos = _currentPositionMs.value + 500L
                    val total = _durationMs.value.coerceAtLeast(1000L)
                    if (newPos >= total) {
                        handleTrackCompletion()
                    } else {
                        _currentPositionMs.value = newPos
                    }
                }
                delay(500)
            }
        }
    }

    private fun releaseMediaPlayer() {
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun release() {
        releaseMediaPlayer()
    }
}
