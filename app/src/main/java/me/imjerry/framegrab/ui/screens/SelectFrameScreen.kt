package me.imjerry.framegrab.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import me.imjerry.framegrab.ui.VideoViewModel
import java.io.File
import kotlin.time.Duration.Companion.seconds

@Composable
fun SelectFrameScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val player = viewModel.player.collectAsState()
    val videoUri = viewModel.currentUri.collectAsState()
    val videoDuration = viewModel.videoDuration.collectAsState()
    val sliderPosition = viewModel.sliderPosition.collectAsState()
    val isVideoPlaying = viewModel.isPlaying.collectAsState()

    fun shareImage(imagePath: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.data.providers.MyFileProvider",
            File(imagePath)
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    fun onShare() {
        // TODO: use better filename
        val fileName = "frame_${System.currentTimeMillis()}.png"
        try {
            val mm = MediaMetadataRetriever()
            mm.setDataSource(context, videoUri.value)
            mm.getFrameAtTime(
                ((sliderPosition.value * videoDuration.value) * 1000L).toLong(),
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .weight(.3f)
                .padding(top = 12.dp)) {
            ExoPlayerWrapper(viewModel = viewModel)
        }
        Column() {
            Slider(
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .pointerInput(Unit) {

                    },
                value = sliderPosition.value,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                onValueChange = { newSliderVal ->
                    viewModel.setSliderPosition(newSliderVal, isManualSeek = true)
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = {
                    viewModel.setPlayState(!isVideoPlaying.value)
                }) {
                    Icon(
                        if (!isVideoPlaying.value) Icons.Default.PlayArrow else Icons.Default.Pause,
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
    viewModel: VideoViewModel
) {

    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    val context = LocalContext.current

    val exoPlayer = viewModel.player.collectAsState().value!!
    val isPlaying = viewModel.isPlaying.collectAsState().value

    if (isPlaying) {
        LaunchedEffect(Unit) {
            while (true) {
                viewModel.setSliderPosition(exoPlayer.currentPosition.toFloat() / viewModel.videoDuration.value)
                delay(1.seconds / 30)
            }
        }
    }

    AndroidView(

        factory = {
            PlayerView(context).apply {
                playerView = this
                player = exoPlayer
                this.useController = false
            }
        }
    )
}