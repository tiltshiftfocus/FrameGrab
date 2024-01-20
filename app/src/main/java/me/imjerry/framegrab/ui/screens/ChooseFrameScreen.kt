package me.imjerry.framegrab.ui.screens

import ComposableLifecycle
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val player = videoViewModel.player.collectAsState().value
    val videoUri = videoViewModel.currentUri.collectAsState().value!!
    val videoDuration = videoViewModel.videoDuration.collectAsState().value
    val sliderPosition = videoViewModel.sliderPosition.collectAsState().value
    val isVideoPlaying = videoViewModel.isPlaying.collectAsState().value

    var isControlShown by remember { mutableStateOf(false) }

    ComposableLifecycle(
        onPause = {
            if (isVideoPlaying) {
                videoViewModel.setPlayState(false)
            }
        }
    )


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
        try {
            val mm = MediaMetadataRetriever()
            mm.setDataSource(context, videoUri)
            mm.getFrameAtTime(
                player!!.currentPosition.toLong(),
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )?.let { bitmap ->
                val fileName = "${context.getString(R.string.app_name)}_${System.currentTimeMillis()}.png"
                val dir = context.externalCacheDir
                val fullPath = "$dir/$fileName"
                val outputStream = File(fullPath).outputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 32, outputStream)
                appViewModel.setIsLoading(false)
                shareImage(fullPath)
                mm.release()
            }
        } catch (e: Exception) {
            appViewModel.setIsLoading(false)
        }
    }

    fun handleOnShare() {
        if (isVideoPlaying) {
            videoViewModel.setPlayState(false)
        }
        appViewModel.setIsLoading(true)
        CoroutineScope(dispatcher).launch { onShare() }
    }

    LaunchedEffect(Unit) {
//        delay(300)
        isControlShown = true
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
            PlayerWrapper(videoViewModel = videoViewModel)
        }
        Surface(
            color = colorScheme.inverseSurface.copy(alpha = 0.1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AnimatedVisibility(
                visible = isControlShown,
                enter = slideInVertically { 3000 }
            ) {
                ChooseFrameControls(
                    videoViewModel = videoViewModel,
                    sliderPosition = sliderPosition,
                    isVideoPlaying = isVideoPlaying
                ) { handleOnShare() }
            }
        }
    }
}

@Composable
private fun ChooseFrameControls(
    videoViewModel: VideoViewModel = viewModel(),
    sliderPosition: Float = 0f,
    isVideoPlaying: Boolean = false,
    handleOnShare: () -> Unit = { }
) {
    Column {
        Slider(
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp)
                .pointerInput(Unit) {

                },
            value = sliderPosition,
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.secondary,
                activeTrackColor = colorScheme.secondary,
                inactiveTrackColor = colorScheme.inverseSurface,
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

@Composable
private fun PlayerWrapper(
    videoViewModel: VideoViewModel
) {
    val videoPlayer = videoViewModel.player.collectAsState().value
    val isPlaying = videoViewModel.isPlaying.collectAsState().value

    if (isPlaying && videoPlayer != null) {
        LaunchedEffect(Unit) {
            while (true) {
                videoViewModel.setSliderPosition(videoPlayer.currentPosition.toFloat() / videoViewModel.videoDuration.value)
                delay(1.seconds / 30)
            }
        }
    }

    AndroidView(
        factory = {
//            PlayerView(context).apply {
//                playerView = this
//                player = exoPlayer
//                this.useController = false
//            }
            videoViewModel.videoView.value!!
        }
    )

}

@Preview
@Composable
fun ChooseFrameControlsPreview() {
    AnimatedVisibility(
        visible = false,
        enter = slideInVertically { 3000 }
    ) {
        ChooseFrameControls()
    }
}