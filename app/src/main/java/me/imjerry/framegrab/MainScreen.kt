package me.imjerry.framegrab

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.imjerry.framegrab.ui.AppViewModel
import me.imjerry.framegrab.ui.VideoViewModel
import me.imjerry.framegrab.ui.components.LoadingBox
import me.imjerry.framegrab.ui.screens.SelectFrameScreen
import me.imjerry.framegrab.ui.screens.SelectVideoScreen

enum class FrameGrabScreen(@StringRes var title: Int) {
    Start(title = R.string.app_name),
    SelectFrame(title = R.string.choose_frame),
    PreviewExport(title = R.string.preview_export);

    val navBarColor: Color
        @Composable
        @ReadOnlyComposable
        get() = when(this) {
            Start -> MaterialTheme.colorScheme.surface
            SelectFrame -> (if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
            else -> { MaterialTheme.colorScheme.surface }
        }
}


@Composable
fun FrameGrabAppBar(
    currentScreen: FrameGrabScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun FrameGrabApp(
    appViewModel: AppViewModel = viewModel(),
    viewModel: VideoViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = FrameGrabScreen.valueOf(
        backStackEntry?.destination?.route ?: FrameGrabScreen.Start.name
    )
    val context = LocalContext.current
    val view = LocalView.current

    LoadingBox(appViewModel = appViewModel) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FrameGrabAppBar(
                    canNavigateBack = navController.previousBackStackEntry != null,
                    scrollBehavior = scrollBehavior,
                    currentScreen = currentScreen,
                    navigateUp = { navController.navigateUp() }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = FrameGrabScreen.Start.name,
                modifier = Modifier.padding(innerPadding),
                exitTransition = { ExitTransition.None },
            ) {
                val window = (view.context as Activity).window
                composable(route = FrameGrabScreen.Start.name) {
//                    window.navigationBarColor = currentScreen.navBarColor.toArgb()
                    SelectVideoScreen(
                        onVideoPicked = { uri ->
                            viewModel.setUri(context, uri)
                            navController.navigate(FrameGrabScreen.SelectFrame.name)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(route = FrameGrabScreen.SelectFrame.name) {
//                    window.navigationBarColor = currentScreen.navBarColor.toArgb()
                    val currentUri = viewModel.currentUri.collectAsState()
                    if (currentUri.value != null) {
                        SelectFrameScreen(
                            appViewModel = appViewModel,
                            videoViewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}