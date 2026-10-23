package xyz.mpv.rex

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import xyz.mpv.rex.ui.browser.components.ResumeFloatingActionButton
import xyz.mpv.rex.ui.browser.components.GlobalFabState
import xyz.mpv.rex.ui.browser.videolist.VideoListScreen
import xyz.mpv.rex.ui.browser.filesystem.FileSystemDirectoryScreen
import xyz.mpv.rex.ui.browser.recentlyplayed.RecentlyPlayedScreen
import xyz.mpv.rex.utils.media.MediaUtils
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import xyz.mpv.rex.preferences.AppearancePreferences
import xyz.mpv.rex.preferences.preference.collectAsState
import xyz.mpv.rex.presentation.Screen
import xyz.mpv.rex.utils.update.UpdateViewModel
import xyz.mpv.rex.ui.browser.MainScreen
import xyz.mpv.rex.ui.theme.DarkMode
import xyz.mpv.rex.ui.theme.MpvexTheme
import xyz.mpv.rex.ui.utils.LocalBackStack
import xyz.mpv.rex.utils.permission.PermissionUtils
import xyz.mpv.rex.ui.browser.miniplayer.MiniPlayerStateManager
import xyz.mpv.rex.ui.browser.LocalNavigationBarHeight
import xyz.mpv.rex.ui.welcome.WelcomeScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import androidx.compose.runtime.staticCompositionLocalOf

val LocalUpdateViewModel = staticCompositionLocalOf<UpdateViewModel?> { null }

/**
 * Main entry point for the application
 */
class MainActivity : ComponentActivity() {
  private val appearancePreferences by inject<AppearancePreferences>()
  private val miniPlayerStateManager by inject<MiniPlayerStateManager>()
  
  // Create a coroutine scope tied to the activity lifecycle
  private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  // Register the ActivityResultLauncher at class level
  private val mediaAccessLauncher = registerForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    PermissionUtils.handleMediaAccessResult(result.resultCode)
  }

  override fun attachBaseContext(newBase: android.content.Context) {
    super.attachBaseContext(xyz.mpv.rex.utils.locale.LocaleHelper.wrapContext(newBase))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    
    enableEdgeToEdge()
    PermissionUtils.setMediaAccessLauncher(mediaAccessLauncher)
    if (!appearancePreferences.onboardingCompleted.get()) {
      appearancePreferences.onboardingCompleted.set(true)
    }

    setContent {
      MpvexTheme {
        Surface {
          Navigator()
        }
      }
    }
  }

  override fun onDestroy() {
    try {
      super.onDestroy()
    } catch (e: Exception) {
      Log.e("MainActivity", "Error during onDestroy", e)
    }
  }

  /**
   * Navigator that handles screen transitions and provides shared states
   */
  @Composable
  fun Navigator() {
    val context = LocalContext.current
    val backstack = rememberNavBackStack(MainScreen)

    @Suppress("UNCHECKED_CAST")
    val typedBackstack = backstack as NavBackStack<Screen>

    val updateViewModel: UpdateViewModel? = null
    val miniPlayerState by miniPlayerStateManager.state.collectAsState()
    val hideNavigationBar by MainScreen.shouldHideNavigationBar.collectAsState()
    val currentRoute = typedBackstack.lastOrNull()
    val isMainScreen = currentRoute == MainScreen
    val isWelcomeScreen = currentRoute == WelcomeScreen
    val isBrowserRoute = isMainScreen ||
        currentRoute is VideoListScreen ||
        currentRoute is FileSystemDirectoryScreen ||
        currentRoute is RecentlyPlayedScreen
    
    val totalNavigationBarHeight = 0.dp

    BackHandler(enabled = isMainScreen || isWelcomeScreen) {
      (context as? Activity)?.moveTaskToBack(true)
    }

    // Provide shared states to all screens
    CompositionLocalProvider(
      LocalBackStack provides typedBackstack,
      LocalUpdateViewModel provides updateViewModel,
      LocalNavigationBarHeight provides totalNavigationBarHeight
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
          backStack = typedBackstack,
          onBack = { typedBackstack.removeLastOrNull() },
          entryProvider = { route -> NavEntry(route) { route.Content() } },
          popTransitionSpec = {
            EnterTransition.None togetherWith ExitTransition.None
          },
          transitionSpec = {
            EnterTransition.None togetherWith ExitTransition.None
          },
          predictivePopTransitionSpec = {
            EnterTransition.None togetherWith ExitTransition.None
          },
        )

        ResumeFloatingActionButton(
          targetFilePath = GlobalFabState.targetFilePath,
          visible = isBrowserRoute && GlobalFabState.isVisible,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(end = 16.dp, bottom = totalNavigationBarHeight + 16.dp),
          onPlay = { path ->
            GlobalFabState.onPlay?.invoke(path) ?: MediaUtils.playFile(path, context, "recently_played_button")
          },
        )
      }
    }
  }
}
