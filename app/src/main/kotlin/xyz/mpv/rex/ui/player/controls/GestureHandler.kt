package xyz.mpv.rex.ui.player.controls

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import `is`.xyz.mpv.MPVLib
import xyz.mpv.rex.preferences.GesturePreferences
import xyz.mpv.rex.preferences.PlayerPreferences
import xyz.mpv.rex.preferences.preference.collectAsState
import xyz.mpv.rex.ui.player.Panels
import xyz.mpv.rex.ui.player.PlayerUpdates
import xyz.mpv.rex.ui.player.PlayerViewModel
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Ultra-Light GestureHandler:
 * - Instant tap response: toggles player controls immediately on touch release (0ms delay).
 * - Smooth vertical gestures: Brightness (left) & Volume (right) with boost cap support.
 * - Hardware pinch-to-zoom & pan.
 * - Removed: double-tap engine, single-tap regions, subtitle drag/pinch, dynamic speed control.
 */
@Composable
fun GestureHandler(
  viewModel: PlayerViewModel,
  interactionSource: MutableInteractionSource,
  modifier: Modifier = Modifier,
) {
  val playerPreferences = koinInject<PlayerPreferences>()
  val gesturePreferences = koinInject<GesturePreferences>()
  val panelShown by viewModel.panelShown.collectAsState()
  val allowGesturesInPanels by playerPreferences.allowGesturesInPanels.collectAsState()
  val controlsShown by viewModel.controlsShown.collectAsState()
  val areControlsLocked by viewModel.areControlsLocked.collectAsState()
  val brightnessGesture by playerPreferences.brightnessGesture.collectAsState()
  val volumeGesture by playerPreferences.volumeGesture.collectAsState()
  val swapVolumeAndBrightness by playerPreferences.swapVolumeAndBrightness.collectAsState()
  val pinchToZoomGesture by playerPreferences.pinchToZoomGesture.collectAsState()
  val panAndZoomEnabled by playerPreferences.panAndZoomEnabled.collectAsState()
  val advancedZoomEnabled by viewModel.advancedZoomEnabled.collectAsState()
  val horizontalSwipeToSeek by playerPreferences.horizontalSwipeToSeek.collectAsState()
  val horizontalSwipeSensitivity by playerPreferences.horizontalSwipeSensitivity.collectAsState()

  val currentVolume by viewModel.currentVolume.collectAsState()
  val currentBrightness by viewModel.currentBrightness.collectAsState()
  var isVerticalGestureActive by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 16.dp)
      // 1. INSTANT SINGLE-TAP & VERTICAL SWIPE DETECTOR
      .pointerInput(areControlsLocked, brightnessGesture, volumeGesture, swapVolumeAndBrightness) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          val startPosition = down.position
          var startingY = 0f
          var originalVolume = currentVolume
          var originalBrightness = currentBrightness
          var lastVolumeValue = currentVolume
          var lastBrightnessValue = currentBrightness
          var isDrag = false
          var isVertical = false
          val volumeGestureSens = 0.05f
          val brightnessGestureSens = 0.003f

          do {
            val event = awaitPointerEvent()
            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break

            val deltaX = pointer.position.x - startPosition.x
            val deltaY = pointer.position.y - startPosition.y
            val dist = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()

            if (!isDrag && dist > 20f) {
              isDrag = true
              if (abs(deltaY) > abs(deltaX) * 1.5f && (brightnessGesture || volumeGesture) && !areControlsLocked) {
                val edgeExcludePx = 24.dp.toPx()
                val isNearEdge = startPosition.x < edgeExcludePx ||
                  startPosition.x > size.width - edgeExcludePx ||
                  startPosition.y < edgeExcludePx ||
                  startPosition.y > size.height - edgeExcludePx
                if (!isNearEdge) {
                  isVertical = true
                  isVerticalGestureActive = true
                  viewModel.isVerticalGestureActive.value = true
                  startingY = 0f
                  originalVolume = currentVolume
                  originalBrightness = currentBrightness
                  lastVolumeValue = currentVolume
                  lastBrightnessValue = currentBrightness
                }
              }
            }

            if (isVertical) {
              pointer.consume()
              val currentY = pointer.position.y
              val amount = currentY - startPosition.y

              val changeVolume: () -> Unit = {
                if (startingY == 0f) {
                  startingY = currentY
                }
                val newVolume = calculateNewVerticalGestureValue(
                  originalVolume,
                  startingY,
                  currentY,
                  volumeGestureSens,
                )
                if (newVolume != lastVolumeValue) {
                  viewModel.changeVolumeTo(newVolume)
                  lastVolumeValue = newVolume
                }
                viewModel.displayVolumeSlider()
              }

              val changeBrightness: () -> Unit = {
                if (startingY == 0f) startingY = currentY
                val newBrightness = calculateNewVerticalGestureValue(
                  originalBrightness,
                  startingY,
                  currentY,
                  brightnessGestureSens,
                )
                if (abs(newBrightness - lastBrightnessValue) > 0.001f) {
                  viewModel.changeBrightnessTo(newBrightness)
                  lastBrightnessValue = newBrightness
                }
                viewModel.displayBrightnessSlider()
              }

              if (swapVolumeAndBrightness) {
                if (startPosition.x > size.width / 2) changeBrightness() else changeVolume()
              } else {
                if (startPosition.x < size.width / 2) changeBrightness() else changeVolume()
              }
            }

            if (!pointer.pressed) {
              if (isVertical) {
                isVerticalGestureActive = false
                viewModel.isVerticalGestureActive.value = false
                isVertical = false
              } else if (!isDrag) {
                // Pure instantaneous single tap
                if (areControlsLocked) {
                  viewModel.showControls()
                } else {
                  if (panelShown != Panels.None && !allowGesturesInPanels) {
                    viewModel.panelShown.update { Panels.None }
                  }
                  if (controlsShown) {
                    viewModel.hideControls()
                  } else {
                    viewModel.showControls()
                  }
                }
              }
              break
            }
          } while (event.changes.any { it.pressed })
        }
      }
      // 2. PINCH TO ZOOM & PAN
      .pointerInput(pinchToZoomGesture, panAndZoomEnabled, areControlsLocked, isVerticalGestureActive, advancedZoomEnabled) {
        if (!pinchToZoomGesture || areControlsLocked || isVerticalGestureActive || advancedZoomEnabled) return@pointerInput

        awaitEachGesture {
          var zoom = 0f
          var gestureStarted = false
          var prevDist = 0f
          var prevMidX = 0f
          var prevMidY = 0f
          var localPanX = 0f
          var localPanY = 0f
          var bw = 0f
          var bh = 0f

          awaitFirstDown(requireUnconsumed = false)

          do {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }

            if (pressed.size == 2) {
              val p1 = pressed[0].position
              val p2 = pressed[1].position
              val dx = p2.x - p1.x
              val dy = p2.y - p1.y
              val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
              val midX = (p1.x + p2.x) / 2f
              val midY = (p1.y + p2.y) / 2f

              if (prevDist == 0f) {
                prevDist = dist
                zoom = MPVLib.getPropertyDouble("video-zoom")?.toFloat() ?: 0f
                localPanX = MPVLib.getPropertyDouble("video-pan-x")?.toFloat() ?: 0f
                localPanY = MPVLib.getPropertyDouble("video-pan-y")?.toFloat() ?: 0f
                val sw = size.width.toFloat()
                val sh = size.height.toFloat()
                val va = MPVLib.getPropertyDouble("video-params/aspect")?.toFloat() ?: (sw / sh)
                val sa = sw / sh
                if (va >= sa) { bw = sw; bh = sw / va } else { bw = sh * va; bh = sh }
                prevMidX = midX
                prevMidY = midY
              } else {
                if (!gestureStarted && abs(dist - prevDist) > 5f) {
                  gestureStarted = true
                  viewModel.playerUpdate.update { PlayerUpdates.VideoZoom }
                }

                if (gestureStarted) {
                  val zoomDelta = ln((dist / prevDist).toDouble()).toFloat() * 1.2f
                  zoom = (zoom + zoomDelta).coerceIn(-1f, 3f)
                  viewModel.setVideoZoom(zoom)

                  if (panAndZoomEnabled && bw > 0f && bh > 0f) {
                    val scale = 2f.pow(zoom)
                    val panSensitivity = 2.2f
                    localPanX += ((midX - prevMidX) * panSensitivity) / (bw * scale)
                    localPanY += ((midY - prevMidY) * panSensitivity) / (bh * scale)
                    val maxPan = ((scale - 1f) / (2f * scale)).coerceAtLeast(0f)
                    localPanX = localPanX.coerceIn(-maxPan, maxPan)
                    localPanY = localPanY.coerceIn(-maxPan, maxPan)
                    viewModel.setVideoPan(localPanX, localPanY)
                  }
                  prevDist = dist
                  prevMidX = midX
                  prevMidY = midY
                }
              }
              pressed.forEach { it.consume() }
            }
          } while (event.changes.any { it.pressed })
        }
      }
      // 3. HORIZONTAL SWIPE TO SEEK
      .pointerInput(horizontalSwipeToSeek, areControlsLocked, isVerticalGestureActive) {
        if (!horizontalSwipeToSeek || areControlsLocked || isVerticalGestureActive) return@pointerInput

        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          val startX = down.position.x
          val startY = down.position.y
          var isSeeking = false
          var initialPos = 0
          var wasPlayingBeforeGestureSeek = false

          do {
            val event = awaitPointerEvent()
            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break

            val dx = pointer.position.x - startX
            val dy = pointer.position.y - startY

            if (!isSeeking && abs(dx) > 40f && abs(dx) > abs(dy) * 2f && !isVerticalGestureActive) {
              isSeeking = true
              initialPos = MPVLib.getPropertyInt("time-pos") ?: 0
              wasPlayingBeforeGestureSeek = (MPVLib.getPropertyBoolean("pause") == false)
              if (wasPlayingBeforeGestureSeek) {
                runCatching { MPVLib.setPropertyBoolean("pause", true) }
              }
              viewModel.setGestureSeeking(true)
            }

            if (isSeeking) {
              pointer.consume()
              val seekDelta = (dx * horizontalSwipeSensitivity).toInt()
              val targetPos = (initialPos + seekDelta).coerceAtLeast(0)
              viewModel.playerUpdate.update {
                val deltaSign = if (seekDelta >= 0) "+" else "-"
                val deltaStr = `is`.xyz.mpv.Utils.prettyTime(abs(seekDelta))
                PlayerUpdates.HorizontalSeek(
                  currentTime = `is`.xyz.mpv.Utils.prettyTime(targetPos),
                  seekDelta = "$deltaSign$deltaStr"
                )
              }
              viewModel.seekTo(targetPos)
            }

            if (!pointer.pressed) {
              if (isSeeking) {
                val seekDelta = (dx * horizontalSwipeSensitivity).toInt()
                val targetPos = (initialPos + seekDelta).coerceAtLeast(0)
                viewModel.seekToImmediately(targetPos)
                if (wasPlayingBeforeGestureSeek) {
                  runCatching { MPVLib.setPropertyBoolean("pause", false) }
                }
                viewModel.setGestureSeeking(false)
                viewModel.playerUpdate.update { PlayerUpdates.None }
              }
              break
            }
          } while (event.changes.any { it.pressed })
        }
      }
  )
}

fun calculateNewVerticalGestureValue(originalValue: Int, startingY: Float, newY: Float, sensitivity: Float): Int {
  return originalValue + ((startingY - newY) * sensitivity).toInt()
}

fun calculateNewVerticalGestureValue(originalValue: Float, startingY: Float, newY: Float, sensitivity: Float): Float {
  return originalValue + ((startingY - newY) * sensitivity)
}
