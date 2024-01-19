package me.imjerry.framegrab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingDialog(isShowingDialog: Boolean, dismissOnBackPress: Boolean = false, dismissOnClickOutside: Boolean = false) {
    if (isShowingDialog) {
        Dialog(
            onDismissRequest = { },
            DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside
            )
        ) {
            LoadingDialogContent()
        }
    }
}

@Composable
fun LoadingDialogContent() {
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(
                    Alignment.Center
                ),
            color = Color.Red
        )
    }
}

@Preview
@Composable
fun PreviewSomeDialogContent() {
    Scaffold(
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                LoadingDialogContent()
            }
        }
    )
}