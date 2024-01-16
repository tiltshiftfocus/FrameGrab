package me.imjerry.framegrab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import me.imjerry.framegrab.ui.theme.FrameGrabTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clearCache()

        setContent {
            FrameGrabTheme {
                FrameGrabApp()
            }
        }
    }

    private fun clearCache() {
        applicationContext.externalCacheDir!!.deleteRecursively()
    }
}