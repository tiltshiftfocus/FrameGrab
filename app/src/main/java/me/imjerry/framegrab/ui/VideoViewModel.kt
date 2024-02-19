package me.imjerry.framegrab.ui

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VideoViewModel: ViewModel() {
    private val _currentUri = MutableStateFlow<Uri?>(null)
    val currentUri: StateFlow<Uri?> = _currentUri.asStateFlow()

    private val _videoView = MutableStateFlow<VideoView?>(null)
    val videoView: StateFlow<VideoView?> = _videoView.asStateFlow()

    private val _player = MutableStateFlow<MediaPlayer?>(null)
    val player: StateFlow<MediaPlayer?> = _player.asStateFlow()

    private val _videoDuration = MutableStateFlow(0L)
    val videoDuration: StateFlow<Long> = _videoDuration.asStateFlow()

    private val _sliderPosition = MutableStateFlow(0f)
    val sliderPosition: StateFlow<Float> = _sliderPosition.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

//    private val playerListener = object: Player.Listener {
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            if (playbackState == ExoPlayer.STATE_ENDED) {
//                setPlayState(false)
//            }
//        }
//    }

    fun setUri(context: Context, uri: Uri) {
        _currentUri.update { uri }
//        _videoDuration.update {
//            val mm = MediaMetadataRetriever()
//            mm.setDataSource(context, uri)
//            mm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//                ?.toLong() ?: "0".toLong()
//        }
        setPlayer(context)
    }

    private fun setVideoDuration(duration: Int) {
        _videoDuration.update{ duration.toLong() }
    }

    private fun setPlayer(context: Context) {
        if (_player.value != null) {
            reset()
        }
        if (_currentUri.value != null) {
//            _player.value = ExoPlayer.Builder(context).build().also { player ->
//                player.setMediaItem(MediaItem.fromUri(_currentUri.value!!))
//                player.prepare()
//                player.volume = 0f
//                player.playWhenReady = false
//                player.addListener(playerListener)
//            }

//            _player.value = MediaPlayer()
            _videoView.value = VideoView(context).apply {
                this.setVideoURI(_currentUri.value)
                this.setOnPreparedListener { mediaPlayer ->
                    setVideoDuration(mediaPlayer.duration)
                    mediaPlayer.setVolume(0f, 0f)
                    mediaPlayer.seekTo(sliderPosition.value.toLong(), MediaPlayer.SEEK_CLOSEST)
                    _player.value = mediaPlayer
                }
                this.setOnCompletionListener {
                    setPlayState(false)
                }
            }
        }
    }

    fun setSliderPosition(position: Float, isManualSeek: Boolean = false) {
        _sliderPosition.update { position }
        if (isManualSeek) {
//            _player.value!!.seekTo((position * _videoDuration.value).toLong())
            _player.value!!.seekTo(position.toLong(), MediaPlayer.SEEK_CLOSEST)
        }
    }

    fun setPlayState(newState: Boolean) {
        _isPlaying.update { newState }
        if (newState) {
            _player.value!!.start()
//            _player.value!!.play()
        } else {
            _player.value!!.pause()
        }
    }

    private fun reset() {
//        _player.value!!.removeListener(playerListener)
        _player.value!!.release()
        _sliderPosition.update { 0f }
        _isPlaying.update { false }
    }
}