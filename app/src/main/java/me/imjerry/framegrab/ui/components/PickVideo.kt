package me.imjerry.framegrab.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun PickVideo(
    onPickVideo: (Uri) -> Unit
) {
//    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
//        result.value = it
        onPickVideo(it!!)
    }

    Button(
        onClick = {
            launcher.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
    ) {
        Text(text = "Select Video")
    }

//    result.value?.let { videoUri ->
////        Text(text = "Video Path ${image.path.toString()}")
//        onPickVideo(videoUri)
//    }
}