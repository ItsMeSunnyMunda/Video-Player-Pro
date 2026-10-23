package xyz.mpv.rex.ui.preferences

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.mpv.rex.R
import xyz.mpv.rex.preferences.AdvancedPreferences
import xyz.mpv.rex.preferences.SettingsManager
import xyz.mpv.rex.presentation.Screen
import xyz.mpv.rex.presentation.components.GroupPosition
import xyz.mpv.rex.presentation.components.GroupedListColumn
import xyz.mpv.rex.ui.utils.LocalBackStack
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.koin.compose.koinInject

@Serializable
object AdvancedPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val backStack = LocalBackStack.current
    val preferences = koinInject<AdvancedPreferences>()
    val settingsManager = koinInject<SettingsManager>()
    val scope = rememberCoroutineScope()
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importStats by remember { mutableStateOf<SettingsManager.ImportStats?>(null) }
    var exportStats by remember { mutableStateOf<SettingsManager.ExportStats?>(null) }

    // Export settings launcher
    val exportLauncher =
      rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml"),
      ) { uri ->
        uri?.let {
          scope.launch {
            settingsManager.exportSettings(it).fold(
              onSuccess = { stats ->
                exportStats = stats
                showExportDialog = true
              },
              onFailure = { error ->
                Toast.makeText(
                  context,
                  context.getString(R.string.export_failed, error.message),
                  Toast.LENGTH_LONG,
                ).show()
              },
            )
          }
        }
      }

    // Import settings launcher
    val importLauncher =
      rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
      ) { uri ->
        uri?.let {
          scope.launch {
            settingsManager.importSettings(it).fold(
              onSuccess = { stats ->
                importStats = stats
                showImportDialog = true
              },
              onFailure = { error ->
                Toast.makeText(
                  context,
                  context.getString(R.string.import_failed, error.message),
                  Toast.LENGTH_LONG,
                ).show()
              },
            )
          }
        }
      }

    // Export results dialog
    if (showExportDialog && exportStats != null) {
      AlertDialog(
        onDismissRequest = { showExportDialog = false },
        title = { Text(stringResource(R.string.export_complete)) },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
          ) {
            Text(
              stringResource(R.string.export_complete_details, exportStats?.totalExported ?: 0)
            )
          }
        },
        confirmButton = {
          TextButton(onClick = { showExportDialog = false }) {
            Text(stringResource(R.string.generic_ok))
          }
        },
      )
    }

    // Import results dialog
    if (showImportDialog && importStats != null) {
      AlertDialog(
        onDismissRequest = { showImportDialog = false },
        title = { Text(stringResource(R.string.import_complete)) },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
          ) {
            Text(
              stringResource(
                R.string.import_complete_details,
                importStats?.imported ?: 0,
                importStats?.failed ?: 0,
                importStats?.version ?: "unknown",
              )
            )
          }
        },
        confirmButton = {
          TextButton(onClick = { showImportDialog = false }) {
            Text(stringResource(R.string.generic_ok))
          }
        },
      )
    }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(R.string.pref_advanced),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          navigationIcon = {
            IconButton(onClick = backStack::removeLastOrNull) {
              Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
              )
            }
          },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        val navBarHeight = xyz.mpv.rex.ui.browser.LocalNavigationBarHeight.current
        LazyColumn(
          state = rememberPreferenceLazyListState(),
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = navBarHeight + 16.dp),
        ) {
          // Backup & Restore Section
          item {
            PreferenceSectionHeader(title = stringResource(R.string.pref_advanced_section_backup_restore))
          }

          item {
            GroupedListColumn {
              GroupedPreferenceCard(
                position = GroupPosition.FIRST,
                highlightKey = listOf(R.string.pref_advanced, R.string.pref_export_settings_title),
              ) {
                Preference(
                  title = { Text(text = stringResource(R.string.pref_export_settings_title)) },
                  summary = {
                    Text(
                      text = stringResource(R.string.pref_export_settings_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  icon = {
                    Icon(
                      Icons.Outlined.FileUpload,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                    )
                  },
                  onClick = {
                    exportLauncher.launch(settingsManager.getDefaultExportFilename())
                  },
                )
              }

              GroupedPreferenceCard(
                position = GroupPosition.LAST,
                highlightKey = R.string.pref_import_settings_title,
              ) {
                Preference(
                  title = { Text(text = stringResource(R.string.pref_import_settings_title)) },
                  summary = {
                    Text(
                      text = stringResource(R.string.pref_import_settings_summary),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                  icon = {
                    Icon(
                      Icons.Outlined.FileDownload,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                    )
                  },
                  onClick = {
                    importLauncher.launch(arrayOf("text/xml", "application/xml", "*/*"))
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}
