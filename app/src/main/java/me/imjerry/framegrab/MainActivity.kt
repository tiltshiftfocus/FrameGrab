package me.imjerry.framegrab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.imjerry.framegrab.ui.theme.FrameGrabTheme

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