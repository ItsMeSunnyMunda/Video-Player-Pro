package xyz.mpv.rex.ui.preferences

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import xyz.mpv.rex.presentation.Screen

@Serializable
object MediaLibraryPreferencesScreen : Screen {
  @Composable
  override fun Content() {
    Text("Media & Library preferences have been removed.")
  }
}
