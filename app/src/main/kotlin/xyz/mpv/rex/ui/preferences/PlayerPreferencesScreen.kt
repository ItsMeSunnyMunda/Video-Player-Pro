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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import xyz.mpv.rex.R
import xyz.mpv.rex.preferences.PlayerPreferences
import xyz.mpv.rex.preferences.preference.collectAsState
import xyz.mpv.rex.presentation.Screen
import xyz.mpv.rex.presentation.components.GroupPosition
import xyz.mpv.rex.presentation.components.GroupedListColumn
import xyz.mpv.rex.ui.player.controls.components.sheets.toFixed
import xyz.mpv.rex.ui.utils.LocalBackStack
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SliderPreference
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
            val brightnessGesture by preferences.brightnessGesture.collectAsState()
            val volumeGesture by preferences.volumeGesture.collectAsState()
            val pinchToZoomGesture by preferences.pinchToZoomGesture.collectAsState()
            val panAndZoomEnabled by preferences.panAndZoomEnabled.collectAsState()
            val horizontalSwipeToSeek by preferences.horizontalSwipeToSeek.collectAsState()
            val swipeToSubtitleSeek by preferences.swipeToSubtitleSeek.collectAsState()
            val moveSubtitleByDragging by preferences.moveSubtitleByDragging.collectAsState()
            val pinchToResizeSubtitle by preferences.pinchToResizeSubtitle.collectAsState()
            val horizontalSwipeSensitivity by preferences.horizontalSwipeSensitivity.collectAsState()

            GroupedListColumn {
              GroupedPreferenceCard(
                position = GroupPosition.FIRST,
                highlightKey = R.string.pref_player_gestures_brightness,
              ) {
                SwitchPreference(
                  value = brightnessGesture,
                  onValueChange = preferences.brightnessGesture::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_brightness)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_volume,
              ) {
                SwitchPreference(
                  value = volumeGesture,
                  onValueChange = preferences.volumeGesture::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_volume)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_pinch_to_zoom,
              ) {
                SwitchPreference(
                  value = pinchToZoomGesture,
                  onValueChange = preferences.pinchToZoomGesture::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_pinch_to_zoom)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_pan_and_zoom,
              ) {
                SwitchPreference(
                  value = panAndZoomEnabled,
                  onValueChange = preferences.panAndZoomEnabled::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_pan_and_zoom)) },
                  summary = { Text(stringResource(R.string.pref_player_gestures_pan_and_zoom_summary)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_horizontal_swipe_to_seek,
              ) {
                SwitchPreference(
                  value = horizontalSwipeToSeek,
                  onValueChange = preferences.horizontalSwipeToSeek::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_horizontal_swipe_to_seek)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_swipe_to_subtitle_seek_title,
              ) {
                SwitchPreference(
                  value = swipeToSubtitleSeek,
                  onValueChange = preferences.swipeToSubtitleSeek::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_swipe_to_subtitle_seek_title)) },
                  summary = { Text(stringResource(R.string.pref_player_gestures_swipe_to_subtitle_seek_summary)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_move_subtitle_by_dragging_title,
              ) {
                SwitchPreference(
                  value = moveSubtitleByDragging,
                  onValueChange = preferences.moveSubtitleByDragging::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_move_subtitle_by_dragging_title)) },
                  summary = { Text(stringResource(R.string.pref_player_gestures_move_subtitle_by_dragging_summary)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.MIDDLE,
                highlightKey = R.string.pref_player_gestures_pinch_to_resize_subtitle_title,
              ) {
                SwitchPreference(
                  value = pinchToResizeSubtitle,
                  onValueChange = preferences.pinchToResizeSubtitle::set,
                  title = { Text(stringResource(R.string.pref_player_gestures_pinch_to_resize_subtitle_title)) },
                  summary = { Text(stringResource(R.string.pref_player_gestures_pinch_to_resize_subtitle_summary)) },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.LAST,
                highlightKey = R.string.pref_player_gestures_horizontal_swipe_sensitivity,
              ) {
                SliderPreference(
                  value = horizontalSwipeSensitivity,
                  onValueChange = { preferences.horizontalSwipeSensitivity.set(it.toFixed(3)) },
                  title = { Text(stringResource(R.string.pref_player_gestures_horizontal_swipe_sensitivity)) },
                  valueRange = 0.020f..0.1f,
                  summary = {
                    val sensitivityPercent = (horizontalSwipeSensitivity * 1000).toInt()
                    Text(
                      "Current: ${sensitivityPercent}/100 (${if (sensitivityPercent < 30) "Low" else if (sensitivityPercent < 55) "Medium" else "High"})",
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  onSliderValueChange = { preferences.horizontalSwipeSensitivity.set(it.toFixed(3)) },
                  sliderValue = horizontalSwipeSensitivity,
                )
              }
            }
          }

          // Controls Section
          item {
            PreferenceSectionHeader(title = stringResource(R.string.pref_player_controls))
          }

          item {
            val disableMediaButtons by preferences.disableMediaButtons.collectAsState()
            val allowGesturesInPanels by preferences.allowGesturesInPanels.collectAsState()

            GroupedListColumn {
              GroupedPreferenceCard(
                position = GroupPosition.FIRST,
                highlightKey = R.string.pref_player_controls_disable_media_buttons_title,
              ) {
                SwitchPreference(
                  value = disableMediaButtons,
                  onValueChange = preferences.disableMediaButtons::set,
                  title = { Text(stringResource(id = R.string.pref_player_controls_disable_media_buttons_title)) },
                  summary = {
                    Text(
                      text = stringResource(R.string.pref_player_controls_disable_media_buttons_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.LAST,
                highlightKey = R.string.pref_player_controls_allow_gestures_in_panels,
              ) {
                SwitchPreference(
                  value = allowGesturesInPanels,
                  onValueChange = preferences.allowGesturesInPanels::set,
                  title = {
                    Text(
                      text = stringResource(id = R.string.pref_player_controls_allow_gestures_in_panels),
                    )
                  },
                )
              }
            }
          }

          // Display Section
          item {
            PreferenceSectionHeader(title = stringResource(R.string.pref_player_display))
          }

          item {
            val reduceMotion by preferences.reduceMotion.collectAsState()

            GroupedListColumn {
              GroupedPreferenceCard(
                position = GroupPosition.ONLY,
                highlightKey = R.string.pref_player_display_reduce_player_animation,
              ) {
                SwitchPreference(
                  value = reduceMotion,
                  onValueChange = preferences.reduceMotion::set,
                  title = { Text(stringResource(R.string.pref_player_display_reduce_player_animation)) },
                )
              }
            }
          }
        }
      }
    }
  }
}
