package me.imjerry.framegrab.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.imjerry.framegrab.R
import me.imjerry.framegrab.ui.AppViewModel
import me.imjerry.framegrab.ui.VideoViewModel
import me.imjerry.framegrab.ui.components.IconButtonBackground
import java.io.File
import kotlin.time.Duration.Companion.seconds

@Composable
fun SelectFrameScreen(
    appViewModel: AppViewModel,
    videoViewModel: VideoViewModel,
    modifier: Modifier = Modifier,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    val context = LocalContext.current

    val videoUri = videoViewModel.currentUri.collectAsState().value!!
    val videoDuration = videoViewModel.videoDuration.collectAsState().value
    val sliderPosition = videoViewModel.sliderPosition.collectAsState().value
    val isVideoPlaying = videoViewModel.isPlaying.collectAsState().value

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
        val mm = MediaMetadataRetriever()
        mm.setDataSource(context, videoUri)
        mm.getFrameAtTime(
            ((sliderPosition * videoDuration) * 1000L).toLong(),
            MediaMetadataRetriever.OPTION_CLOSEST
        )?.let { bitmap ->
            val dir = context.externalCacheDir
            val fullPath = "$dir/$fileName"
            val outputStream = File(fullPath).outputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 32, outputStream)
            appViewModel.setIsLoading(false)
            shareImage(fullPath)
        }
    }

    fun handleOnShare() {
        videoViewModel.setPlayState(false)
        appViewModel.setIsLoading(true)
        CoroutineScope(dispatcher).launch { onShare() }
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .padding(top = 12.dp, bottom = 12.dp)
                .weight(0.3f)
        ) {
            ExoPlayerWrapper(viewModel = videoViewModel)
        }
        Surface(
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column() {
                Slider(
                    modifier = Modifier
                        .padding(start = 32.dp, end = 32.dp)
                        .pointerInput(Unit) {

                        },
                    value = sliderPosition,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.inverseSurface,
                    ),
                    onValueChange = { newSliderVal ->
                        videoViewModel.setSliderPosition(newSliderVal, isManualSeek = true)
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,

                    ) {
                    Box {
                        IconButton(onClick = {
                            videoViewModel.setPlayState(!isVideoPlaying)
                        }) {
                            Icon(
                                if (!isVideoPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = stringResource(R.string.play_pause)
                            )
                        }
                    }
                    IconButtonBackground(
                        icon = Icons.Default.Share,
                        contentDescription = stringResource(R.string.export),
                        tintColor = Color.Red
                    ) {
                        handleOnShare()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExoPlayerWrapper(
    viewModel: VideoViewModel
) {
    var isShown by remember { mutableStateOf(false) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    val context = LocalContext.current

    val exoPlayer = viewModel.player.collectAsState().value!!
    val isPlaying = viewModel.isPlaying.collectAsState().value

    LaunchedEffect(Unit) {
        delay(1000)
        isShown = true
    }

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