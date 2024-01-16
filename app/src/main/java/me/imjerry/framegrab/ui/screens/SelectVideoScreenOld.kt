package me.imjerry.framegrab.ui.screens

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import me.imjerry.framegrab.ui.components.PickVideo
import java.io.File

@Composable
fun SelectVideoScreenOld(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val metadataRetriever = MediaMetadataRetriever()

    val videoUri = remember { mutableStateOf<Uri?>(null) }
    val videoDuration = remember { mutableStateOf(0) }

    val sliderPosition = remember { mutableStateOf(0f) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { prepare() }
    }
    var mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val videoView = remember { VideoView(context) }
    val seekBar = remember {
        SeekBar(context).apply {
            this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                    p0?.progress = progress
                    mediaPlayer.value?.seekTo(progress.toLong(), MediaPlayer.SEEK_CLOSEST)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }
    }

    fun onPickVideo(uri: Uri) {
        videoUri.value = uri
        metadataRetriever.setDataSource(context, uri)

        exoPlayer.setMediaItem(MediaItem.fromUri(uri))

        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { player ->
            mediaPlayer.value = player
            seekBar.max = player.duration
            videoDuration.value = player.duration
        }
        videoView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    fun onSave() {
        val fileName = "frame_${System.currentTimeMillis()}.png"
        try {
            metadataRetriever.getFrameAtTime(
                seekBar.progress * 1000L,
                MediaMetadataRetriever.OPTION_CLOSEST
            )?.let { bitmap ->
                val picturesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val fullPath = "$picturesDir/$fileName"

                val outputStream = File(fullPath).outputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

//                val values = ContentValues()
//                values.put(MediaStore.Images.Media.DATA, fullPath)
//                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PickVideo(onPickVideo = { uri -> onPickVideo(uri) })
        if (videoUri.value != null) {
            Box(Modifier.weight(.3f)) {
                ExoPlayerWrapperLocal(uri = videoUri.value!!)
//                VideoWrapperView(videoView = videoView)
//                AndroidView(
//                    modifier = Modifier
//                        .wrapContentSize()
//                        .fillMaxSize(),
//                    factory = {
//                        PlayerView(context).apply {
//                            player = exoPlayer
//                            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//                        }
//                    }
//                )
            }
            Column() {
//                Slider(
//                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
//                    value = sliderPosition.value,
//                    onValueChange = { newSliderVal ->
//                        sliderPosition.value = newSliderVal
//
//                        try {
//                            val position = videoDuration.value * newSliderVal
//                            mediaPlayer.value?.seekTo(position.toLong(), MediaPlayer.SEEK_CLOSEST)
//
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    },
//                    colors = SliderDefaults.colors(
//                        thumbColor = MaterialTheme.colorScheme.secondary,
//                        activeTrackColor = MaterialTheme.colorScheme.secondary,
//                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
//                    )
//                )
//                AndroidView(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp),
//                    factory = { seekBar }
//                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { onSave() }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoWrapperView(videoView: VideoView) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .padding(top = 12.dp, bottom = 12.dp),
        factory = { videoView }
    )
}

@Composable
private fun ExoPlayerWrapperLocal(uri: Uri) {
    val context = LocalContext.current

    val mediaItem = MediaItem.Builder()
        .setUri(uri)
        .build()
    val exoPlayer = remember(context, mediaItem) {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                exoPlayer.volume = 0f
            }
    }

    DisposableEffect(
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    this.useController = false
                }
            })
    ) {
        onDispose { exoPlayer.release() }
    }


}