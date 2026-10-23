package xyz.mpv.rex.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.mpv.rex.R
import xyz.mpv.rex.preferences.AppearancePreferences
import xyz.mpv.rex.preferences.PlayerButton
import xyz.mpv.rex.preferences.allPlayerButtons
import xyz.mpv.rex.preferences.preference.collectAsState
import xyz.mpv.rex.presentation.Screen
import xyz.mpv.rex.presentation.components.GroupPosition
import xyz.mpv.rex.presentation.components.GroupedListColumn
import xyz.mpv.rex.ui.utils.LocalBackStack
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import xyz.mpv.rex.ui.preferences.components.PlayerButtonChip
import org.koin.compose.koinInject

// Enum to identify which region we are editing
@Serializable
enum class ControlRegion {
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    PORTRAIT_BOTTOM,
    MORE_SHEET,
}

@Serializable
object PlayerControlsPreferencesScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val backstack = LocalBackStack.current
        val appearancePrefs = koinInject<AppearancePreferences>()

        val topRState by appearancePrefs.topRightControls.collectAsState()
        val bottomRState by appearancePrefs.bottomRightControls.collectAsState()
        val bottomLState by appearancePrefs.bottomLeftControls.collectAsState()
        val portraitBottomState by appearancePrefs.portraitBottomControls.collectAsState()
        val moreSheetState by appearancePrefs.moreSheetControls.collectAsState()

        val topRightButtons = remember(topRState) {
            appearancePrefs.parseButtons(topRState, mutableSetOf())
        }
        val bottomRightButtons = remember(bottomRState) {
            appearancePrefs.parseButtons(bottomRState, mutableSetOf())
        }
        val bottomLeftButtons = remember(bottomLState) {
            appearancePrefs.parseButtons(bottomLState, mutableSetOf())
        }
        val portraitBottomButtons = remember(portraitBottomState) {
            appearancePrefs.parseButtons(portraitBottomState, mutableSetOf()).filter { it != PlayerButton.MORE_OPTIONS }
        }
        val moreSheetButtons = remember(moreSheetState, topRState, bottomRState, bottomLState, portraitBottomState) {
            val manualOrder = appearancePrefs.parseButtons(moreSheetState, mutableSetOf()).filter { it != PlayerButton.MORE_OPTIONS }
            val landscapeSet = (topRState.split(',') + bottomRState.split(',') + bottomLState.split(','))
                .filter(String::isNotBlank)
                .mapNotNull { try { PlayerButton.valueOf(it) } catch (_: Exception) { null } }
                .toSet()
            val portraitSet = portraitBottomState.split(',')
                .filter(String::isNotBlank)
                .mapNotNull { try { PlayerButton.valueOf(it) } catch (_: Exception) { null } }
                .toSet()

            val intersection = landscapeSet.intersect(portraitSet)
            val orphans = allPlayerButtons.filter { it != PlayerButton.NONE && it != PlayerButton.MORE_OPTIONS && it !in intersection }
            val orderedOrphans = manualOrder.filter { it in orphans }
            val remainingOrphans = orphans.filter { it !in orderedOrphans }
            orderedOrphans + remainingOrphans
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.pref_layout_title),
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
                    // Landscape Controls Section
                    item {
                        PreferenceSectionHeader(title = stringResource(R.string.pref_layout_landscape_controls_header))
                    }
                    item {
                        GroupedListColumn {
                            GroupedPreferenceCard(
                                position = GroupPosition.FIRST,
                                highlightKey = listOf(R.string.pref_layout_title, R.string.pref_layout_top_right_controls),
                            ) {
                                Column {
                                    PreferenceCategoryWithEditButton(
                                        title = stringResource(id = R.string.pref_layout_top_right_controls),
                                        onClick = {
                                            backstack.add(ControlLayoutEditorScreen(ControlRegion.TOP_RIGHT))
                                        },
                                    )
                                    PreferenceIconSummary(buttons = topRightButtons)
                                }
                            }

                            GroupedPreferenceCard(
                                position = GroupPosition.MIDDLE,
                                highlightKey = R.string.pref_layout_bottom_right_controls,
                            ) {
                                Column {
                                    PreferenceCategoryWithEditButton(
                                        title = stringResource(id = R.string.pref_layout_bottom_right_controls),
                                        onClick = {
                                            backstack.add(ControlLayoutEditorScreen(ControlRegion.BOTTOM_RIGHT))
                                        },
                                    )
                                    PreferenceIconSummary(buttons = bottomRightButtons)
                                }
                            }

                            GroupedPreferenceCard(
                                position = GroupPosition.LAST,
                                highlightKey = R.string.pref_layout_bottom_left_controls,
                            ) {
                                Column {
                                    PreferenceCategoryWithEditButton(
                                        title = stringResource(id = R.string.pref_layout_bottom_left_controls),
                                        onClick = {
                                            backstack.add(ControlLayoutEditorScreen(ControlRegion.BOTTOM_LEFT))
                                        },
                                    )
                                    PreferenceIconSummary(buttons = bottomLeftButtons)
                                }
                            }
                        }
                    }

                    // Portrait Controls Section
                    item {
                        PreferenceSectionHeader(title = stringResource(R.string.pref_layout_portrait_controls_header))
                    }
                    item {
                        GroupedListColumn {
                            GroupedPreferenceCard(
                                position = GroupPosition.ONLY,
                                highlightKey = R.string.pref_layout_portrait_bottom_controls,
                            ) {
                                Column {
                                    PreferenceCategoryWithEditButton(
                                        title = stringResource(id = R.string.pref_layout_portrait_bottom_controls),
                                        onClick = {
                                            backstack.add(ControlLayoutEditorScreen(ControlRegion.PORTRAIT_BOTTOM))
                                        },
                                    )
                                    PreferenceIconSummary(buttons = portraitBottomButtons)
                                }
                            }
                        }
                    }

                    item {
                        PreferenceSectionHeader(title = stringResource(R.string.pref_layout_more_sheet_controls_header))
                    }
                    item {
                        GroupedListColumn {
                            GroupedPreferenceCard(
                                position = GroupPosition.ONLY,
                                highlightKey = R.string.pref_layout_more_sheet_controls_title,
                            ) {
                                Column {
                                    PreferenceCategoryWithEditButton(
                                        title = stringResource(R.string.pref_layout_more_sheet_controls_title),
                                        onClick = {
                                            backstack.add(ControlLayoutEditorScreen(ControlRegion.MORE_SHEET))
                                        },
                                    )
                                    PreferenceIconSummary(buttons = moreSheetButtons)
                                }
                            }
                        }
                    }

                    item {
                        IconsLegend(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    /**
     * Custom composable for the category header with an Edit button.
     */
    @Composable
    private fun PreferenceCategoryWithEditButton(
        title: String,
        onClick: () -> Unit,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.generic_edit_content_description, title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    /**
     * Custom composable to show a row of icons for the summary.
     */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun PreferenceIconSummary(buttons: List<PlayerButton>) {
        FlowRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            if (buttons.isEmpty()) {
                Text(
                    stringResource(R.string.generic_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                buttons.forEach { button ->
                    PlayerButtonChip(
                        button = button,
                        enabled = true,
                        onClick = null,
                        badgeIcon = null,
                        badgeColor = null
                    )
                }
            }
        }
    }
}
