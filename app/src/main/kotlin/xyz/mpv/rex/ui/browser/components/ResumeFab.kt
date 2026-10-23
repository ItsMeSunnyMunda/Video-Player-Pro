package xyz.mpv.rex.ui.browser.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.mpv.rex.R
import xyz.mpv.rex.database.entities.RecentlyPlayedEntity
import java.io.File

import xyz.mpv.rex.utils.storage.FileFilterUtils

object FabPlaybackResolver {
  /**
   * Checks if a file exists, is a regular file, and is NOT hidden.
   * A file is considered hidden if its name starts with '.' or if any parent directory
   * segment starts with '.', or optionally if it is within a .nomedia boundary.
   */
  fun isPlayableAndVisible(filePath: String, includeNoMedia: Boolean = true): Boolean {
    if (filePath.isBlank()) return false
    val file = File(filePath)
    if (!file.exists() || !file.isFile) return false

    // Hidden check: filename starting with '.'
    if (file.name.startsWith(".")) return false

    // Hidden check: any parent directory starting with '.'
    var parent: File? = file.parentFile
    while (parent != null) {
      if (parent.name.startsWith(".")) return false
      parent = parent.parentFile
    }

    // Check .nomedia boundary if includeNoMedia is false
    if (!includeNoMedia && file.parentFile != null && FileFilterUtils.isWithinNoMediaBoundary(file.parentFile!!)) {
      return false
    }

    return true
  }

  /**
   * Resolves the single playable video file for the FAB:
   *
   * 1. If inside a folder (currentFolderPath != null):
   *    a) Direct files: Search recently played for videos directly in currentFolderPath.
   *       Skip deleted or '.' prefixed files until a valid one is found.
   *    b) Subfolders: If no direct played video, iterate through orderedSubfolderPaths
   *       (top-to-bottom order). In each non-hidden subfolder, find the most recently
   *       played video, skipping deleted or '.' prefixed files.
   *    c) If none found across folder and subfolders: Return null (FAB disappears).
   *
   * 2. If at root / global level (currentFolderPath == null):
   *    Search recently played list from newest to oldest, optionally constrained to
   *    orderedSubfolderPaths if provided, skipping deleted or '.' prefixed files.
   *    If none found: Return null (FAB disappears).
   */
  fun resolvePlayableFile(
    currentFolderPath: String?,
    orderedSubfolderPaths: List<String> = emptyList(),
    recentlyPlayed: List<RecentlyPlayedEntity>,
    includeNoMedia: Boolean = true,
  ): String? {
    if (recentlyPlayed.isEmpty()) return null

    // Global / Root level
    if (currentFolderPath == null) {
      val validFolders = orderedSubfolderPaths.map {
        val canonical = runCatching { File(it).canonicalPath }.getOrDefault(it)
        if (canonical.endsWith(File.separator)) canonical else canonical + File.separator
      }
      for (entity in recentlyPlayed) {
        if (!isPlayableAndVisible(entity.filePath, includeNoMedia)) continue
        if (validFolders.isNotEmpty()) {
          val entityCanon = runCatching { File(entity.filePath).canonicalPath }.getOrDefault(entity.filePath)
          if (!validFolders.any { entityCanon.startsWith(it) }) continue
        }
        return entity.filePath
      }
      return null
    }

    val normCurrent = runCatching { File(currentFolderPath).canonicalPath }.getOrDefault(currentFolderPath)

    // 1. Prioritize files located directly in the current folder
    for (entity in recentlyPlayed) {
      val parentPath = runCatching { File(entity.filePath).parentFile?.canonicalPath }.getOrNull()
      if (parentPath != null && parentPath == normCurrent) {
        if (isPlayableAndVisible(entity.filePath, includeNoMedia)) {
          return entity.filePath
        }
      }
    }

    // 2. Prioritize subfolders from top to bottom
    for (subfolderPath in orderedSubfolderPaths) {
      val subfolderFile = File(subfolderPath)
      if (subfolderFile.name.startsWith(".")) continue
      val normSub = runCatching { subfolderFile.canonicalPath }.getOrDefault(subfolderPath)
      val prefix = if (normSub.endsWith(File.separator)) normSub else normSub + File.separator

      // Find the most recently played valid file within this subfolder
      for (entity in recentlyPlayed) {
        val entityPath = runCatching { File(entity.filePath).canonicalPath }.getOrDefault(entity.filePath)
        if (entityPath.startsWith(prefix)) {
          if (isPlayableAndVisible(entity.filePath, includeNoMedia)) {
            return entity.filePath
          }
        }
      }
    }

    // 3. No played files in folder or subfolders -> disappear
    return null
  }
}

object GlobalFabState {
  var targetFilePath by mutableStateOf<String?>(null)
  var isVisible by mutableStateOf(false)
  var onPlay: ((String) -> Unit)? = null
  var cachedRecentlyPlayed: List<RecentlyPlayedEntity> = emptyList()

  fun update(filePath: String?, visible: Boolean, onPlayCallback: ((String) -> Unit)? = null) {
    targetFilePath = filePath
    isVisible = visible && !filePath.isNullOrEmpty()
    if (onPlayCallback != null) {
      onPlay = onPlayCallback
    }
  }

  fun hide() {
    isVisible = false
  }
}

/**
 * Attaches the current screen's playable file target and visibility to the global FAB
 * hosted in MainActivity.
 */
@Composable
fun BindResumeFab(
  targetFilePath: String?,
  visible: Boolean,
  onPlay: (String) -> Unit,
) {
  val currentOnPlay by rememberUpdatedState(onPlay)
  DisposableEffect(targetFilePath, visible) {
    GlobalFabState.update(targetFilePath, visible) { path ->
      currentOnPlay(path)
    }
    onDispose { }
  }
}

@Composable
fun ResumeFloatingActionButton(
  targetFilePath: String?,
  visible: Boolean,
  modifier: Modifier = Modifier,
  onPlay: (String) -> Unit,
) {
  AnimatedVisibility(
    visible = visible && !targetFilePath.isNullOrEmpty(),
    enter = fadeIn(animationSpec = tween(150)),
    exit = fadeOut(animationSpec = tween(150)),
    modifier = modifier,
  ) {
    FloatingActionButton(
      onClick = {
        targetFilePath?.let(onPlay)
      },
      shape = CircleShape,
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
      Icon(
        imageVector = Icons.Filled.PlayArrow,
        contentDescription = stringResource(R.string.play),
      )
    }
  }
}
