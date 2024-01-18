package me.imjerry.framegrab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IconButtonBackground(
    icon: ImageVector,
    contentDescription: String,
    tintColor: Color,
    onClick: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp)) {
        Box(
            Modifier
                .background(color = tintColor.copy(alpha = 0.1f)),
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    tint = tintColor
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    IconButtonBackground(icon = Icons.Filled.Share, contentDescription = "", tintColor = Color.Red) {}
}