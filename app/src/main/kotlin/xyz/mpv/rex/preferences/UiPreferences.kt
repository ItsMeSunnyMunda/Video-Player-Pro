package xyz.mpv.rex.preferences

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Consolidated UI settings for easier consumption in Composables and ViewModels
 */
@Immutable
data class UiSettings(
    val showVideoThumbnails: Boolean = true,
    val showSizeChip: Boolean = true,
    val showResolutionChip: Boolean = true,
    val showFramerateInResolution: Boolean = false,
    val showTotalDurationChip: Boolean = false,
    val showUnplayedOldVideoLabel: Boolean = false,
    val unplayedOldVideoDays: Int = 7,
    val showNetworkThumbnails: Boolean = false
)

/**
 * Provider for unified UI settings
 */
class UiPreferences(
    private val appearancePreferences: AppearancePreferences,
    private val browserPreferences: BrowserPreferences
) {
    /**
     * Observes all relevant UI settings as a single flow
     */
    fun observeUiSettings(): Flow<UiSettings> {
        return combine(
            browserPreferences.showVideoThumbnails.changes(),
            browserPreferences.showSizeChip.changes(),
            browserPreferences.showResolutionChip.changes(),
            browserPreferences.showFramerateInResolution.changes(),
            browserPreferences.showTotalDurationChip.changes(),
            appearancePreferences.showUnplayedOldVideoLabel.changes(),
            appearancePreferences.unplayedOldVideoDays.changes(),
            appearancePreferences.showNetworkThumbnails.changes()
        ) { values: Array<Any?> ->
            UiSettings(
                showVideoThumbnails = values[0] as Boolean,
                showSizeChip = values[1] as Boolean,
                showResolutionChip = values[2] as Boolean,
                showFramerateInResolution = values[3] as Boolean,
                showTotalDurationChip = values[4] as Boolean,
                showUnplayedOldVideoLabel = values[5] as Boolean,
                unplayedOldVideoDays = values[6] as Int,
                showNetworkThumbnails = values[7] as Boolean
            )
        }
    }
    
    /**
     * Returns current snapshot of UI settings
     */
    fun getUiSettings(): UiSettings {
        return UiSettings(
            showVideoThumbnails = browserPreferences.showVideoThumbnails.get(),
            showSizeChip = browserPreferences.showSizeChip.get(),
            showResolutionChip = browserPreferences.showResolutionChip.get(),
            showFramerateInResolution = browserPreferences.showFramerateInResolution.get(),
            showTotalDurationChip = browserPreferences.showTotalDurationChip.get(),
            showUnplayedOldVideoLabel = appearancePreferences.showUnplayedOldVideoLabel.get(),
            unplayedOldVideoDays = appearancePreferences.unplayedOldVideoDays.get(),
            showNetworkThumbnails = appearancePreferences.showNetworkThumbnails.get()
        )
    }
}
