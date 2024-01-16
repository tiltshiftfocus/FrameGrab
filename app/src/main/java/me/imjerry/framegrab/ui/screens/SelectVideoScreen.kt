package me.imjerry.framegrab.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.imjerry.framegrab.ui.components.PickVideo

@Composable
fun SelectVideoScreen(
    modifier: Modifier = Modifier,
    onVideoPicked: (Uri) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PickVideo(onPickVideo = { uri -> onVideoPicked(uri) })
    }
}
