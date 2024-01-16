package me.imjerry.framegrab.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.imjerry.framegrab.R
import java.io.File
import kotlin.time.Duration.Companion.seconds

@Composable
fun SelectFrameScreen(
    player: ExoPlayer,
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var videoDuration by remember { mutableLongStateOf(0L) }

    var sliderPosition by remember { mutableFloatStateOf(0f) }

    var isVideoPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val mm = MediaMetadataRetriever()
        mm.setDataSource(context, videoUri)
        videoDuration =
            mm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: "0".toLong()
        sliderPosition = 0f
    }

    fun shareImage(imagePath: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.providers.MyFileProvider",
            File(imagePath)
        )
//        context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    fun onShare() {
        val fileName = "frame_${System.currentTimeMillis()}.png"
        try {
            val mm = MediaMetadataRetriever()
            mm.setDataSource(context, videoUri)
            mm.getFrameAtTime(
                ((sliderPosition * videoDuration) * 1000L).toLong(),
                MediaMetadataRetriever.OPTION_CLOSEST
            )?.let { bitmap ->
                val dir = context.externalCacheDir
                val fullPath = "$dir/$fileName"

                MainScope().launch {
                    val outputStream = File(fullPath).outputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 32, outputStream)

                    shareImage(fullPath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    fun updateSlider(position: Float) {
//        sliderPosition = (position / videoDuration)
//    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .weight(.3f)
                .padding(top = 12.dp)) {
//            ExoPlayerWrapperLocal(
//                uri = videoUri,
//                seekPosition = sliderPosition,
//                isVideoPlaying = isVideoPlaying,
//                updateSlider = { value -> updateSlider(value) },
//                onPlayStateChange = { state -> isVideoPlaying = state }
//            )
            ExoPlayerWrapper(exoPlayer = player)
        }
        Column() {
            Slider(
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isVideoPlaying = false
                        }
                    },
                value = sliderPosition,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                onValueChange = { newSliderVal ->
                    sliderPosition = newSliderVal
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = { isVideoPlaying = !isVideoPlaying }) {
                    Icon(
                        if (!isVideoPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = stringResource(R.string.play_pause)
                    )
                }
                IconButton(onClick = { onShare() }) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export))
                }
            }
        }
    }
}

@Composable
private fun ExoPlayerWrapper(
    exoPlayer: ExoPlayer
) {
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(),
        factory = {
            PlayerView(context).apply {
                playerView = this
                player = exoPlayer
                this.useController = false
            }
        }
    )
}

@Composable
private fun ExoPlayerWrapperLocal(
    uri: Uri,
    seekPosition: Float,
    isVideoPlaying: Boolean,
    updateSlider: (Float) -> Unit,
    onPlayStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                exoPlayer.setMediaItem(MediaItem.fromUri(uri))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false
                exoPlayer.volume = 0f
            }
    }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    if (isVideoPlaying) {
        LaunchedEffect(Unit) {
            while (true) {
                updateSlider(exoPlayer.currentPosition.toFloat())
                delay(1.seconds / 30)
            }
        }
    }

    LaunchedEffect(isVideoPlaying) {
        if (isVideoPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    LaunchedEffect(seekPosition) {
        if (!isVideoPlaying) {
            exoPlayer.seekTo((seekPosition * exoPlayer.duration).toLong())

        }
    }

    DisposableEffect(
        Unit
    ) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    onPlayStateChange(false)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(),
        factory = {
            PlayerView(context).apply {
                playerView = this
                player = exoPlayer
                this.useController = false
            }
        })


}