package me.imjerry.framegrab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.imjerry.framegrab.ui.AppViewModel

@Composable
fun LoadingBox(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val isLoading = appViewModel.appIsLoading.collectAsState().value

    content()
    AnimatedVisibility(
        visible = isLoading,
        enter = slideIn { IntOffset(0, 200) } + fadeIn(tween(300)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LocalContentColor.current.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EllipsisLoading()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoadingBoxPreview() {
    
    Surface(
        Modifier.fillMaxSize()
    ) {
        LoadingBox {

            Text(text = "test")
        }
    }
}
