package me.imjerry.framegrab.ui

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VideoViewModel(): ViewModel() {
    private val _currentUri = MutableStateFlow<Uri?>(null)
    val currentUri: StateFlow<Uri?> = _currentUri.asStateFlow()

    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player: StateFlow<ExoPlayer?> = _player.asStateFlow()

    private val _videoDuration = MutableStateFlow(0L)
    val videoDuration: StateFlow<Long> = _videoDuration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val playerListener = object: Player.Listener {
    }

    fun setUri(context: Context, uri: Uri) {
        _currentUri.update { uri }
        _videoDuration.update {
            val mm = MediaMetadataRetriever()
            mm.setDataSource(context, uri)
            mm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: "0".toLong()
        }
        setPlayer(context)
    }

    private fun setPlayer(context: Context) {
        if (_currentUri.value != null) {
            _player.value = ExoPlayer.Builder(context).build().also { player ->
                player.setMediaItem(MediaItem.fromUri(_currentUri.value!!))
                player.prepare()
                player.volume = 0f
                player.playWhenReady = false
                player.addListener(playerListener)
            }
        }
    }

    fun setPosition(position: Long) {
        _currentPosition.update { position }
    }

    fun setPlayState(newState: Boolean) {
        _isPlaying.update { newState }
    }

//    init {
//        viewModelScope.launch {
//            _player.value = ExoPlayer.Builder(application.applicationContext).build()
//        }
//    }

}