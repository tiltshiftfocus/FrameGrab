package me.imjerry.framegrab.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dashedBorder
import me.imjerry.framegrab.R

@Composable
fun PickVideo(
    onPickVideo: (Uri) -> Unit = {}
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null)
            onPickVideo(it)
    }

    fun launchPicker() {
        launcher.launch("video/*")
    }

    val mainColor: Color = MaterialTheme.colorScheme.inverseSurface

    Surface(
        color = mainColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier
                .size(300.dp, 500.dp)
                .dashedBorder(
                    4.dp,
                    mainColor.copy(alpha = 0.3f),
                    RoundedCornerShape(30.dp),
                    10.dp,
                    10.dp
                )
                .clickable { launchPicker() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                imageVector = Icons.Outlined.VideoFile,
                contentDescription = stringResource(R.string.pick_video),
                tint = mainColor.copy(alpha = 0.3f)
            )
            Text(text = stringResource(R.string.pick_video), color = mainColor.copy(alpha = 0.3f))
        }
    }
}

@Preview
@Composable
fun PickVideoPreview() {
    Surface(
        color = Color.White
    ) {
        Box(Modifier.background(Color.White)) {
            PickVideo()
        }
    }
}