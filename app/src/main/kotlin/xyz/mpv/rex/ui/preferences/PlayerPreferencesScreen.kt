package xyz.mpv.rex.ui.preferences

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import xyz.mpv.rex.R
import xyz.mpv.rex.preferences.PlayerPreferences
import xyz.mpv.rex.preferences.preference.collectAsState
import xyz.mpv.rex.presentation.Screen
import xyz.mpv.rex.presentation.components.GroupPosition
import xyz.mpv.rex.presentation.components.GroupedListColumn
import xyz.mpv.rex.ui.utils.LocalBackStack
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import xyz.mpv.rex.ui.preferences.components.SwitchPreference
import org.koin.compose.koinInject

@Serializable
object PlayerPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val context = LocalContext.current
    val preferences = koinInject<PlayerPreferences>()
    Scaffold(
      topBar = {
        TopAppBar(
          title = { 
            Text(
              text = stringResource(id = R.string.pref_player),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            ) 
          },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(
                Icons.AutoMirrored.Outlined.ArrowBack, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
              )
            }
          },
        )
      },
    ) { padding ->
      val navBarHeight = xyz.mpv.rex.ui.browser.LocalNavigationBarHeight.current
      ProvidePreferenceLocals {
        LazyColumn(
          state = rememberPreferenceLazyListState(),
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = navBarHeight + 16.dp),
        ) {
          // Seeking Section
          item {
            PreferenceSectionHeader(title = stringResource(R.string.pref_player_seeking_title))
          }

          item {
            val usePreciseSeeking by preferences.usePreciseSeeking.collectAsState()
            val showSeekBarWhenSeeking by preferences.showSeekBarWhenSeeking.collectAsState()
            val hideOsdText by preferences.hideOsdText.collectAsState()

            GroupedListColumn {
              GroupedPreferenceCard(
                position = GroupPosition.FIRST,
                highlightKey = R.string.pref_player_use_precise_seeking,
              ) {
                SwitchPreference(
                  value = usePreciseSeeking,
                  onValueChange = preferences.usePreciseSeeking::set,
                  title = { Text(stringResource(R.string.pref_player_use_precise_seeking)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_show_seekbar_when_seeking_title,
              ) {
                SwitchPreference(
                  value = showSeekBarWhenSeeking,
                  onValueChange = preferences.showSeekBarWhenSeeking::set,
                  title = { Text(stringResource(R.string.pref_player_show_seekbar_when_seeking_title)) },
                  summary = { Text(stringResource(R.string.pref_player_show_seekbar_when_seeking_summary)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.LAST,
                highlightKey = R.string.pref_player_hide_osd_text_title,
              ) {
                SwitchPreference(
                  value = hideOsdText,
                  onValueChange = preferences.hideOsdText::set,
                  title = { Text(stringResource(R.string.pref_player_hide_osd_text_title)) },
                  summary = { Text(stringResource(R.string.pref_player_hide_osd_text_summary)) },
                )
              }
            }
          }

          // Gestures Section
          item {
            PreferenceSectionHeader(title = stringResource(R.string.pref_player_gestures))
          }

          item {
            val pinchToZoomGesture by preferences.pinchToZoomGesture.collectAsState()
            val panAndZoomEnabled by preferences.panAndZoomEnabled.collectAsState()

            GroupedListColumn {
              GroupedPreferenceCard(
                position = GroupPosition.FIRST,
                highlightKey = R.string.pref_player_gestures_pinch_to_zoom,
              ) {
                SwitchPreference(
                  value = pinchToZoomGesture,
                  onValueChange = preferences.pinchToZoomGesture::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_pinch_to_zoom)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.LAST,
                highlightKey = R.string.pref_player_gestures_pan_and_zoom,
              ) {
                SwitchPreference(
                  value = panAndZoomEnabled,
                  onValueChange = preferences.panAndZoomEnabled::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_pan_and_zoom)) },
                  summary = { Text(stringResource(R.string.pref_player_gestures_pan_and_zoom_summary)) },
                )
              }
            }
          }
        }
      }
    }
  }
}
