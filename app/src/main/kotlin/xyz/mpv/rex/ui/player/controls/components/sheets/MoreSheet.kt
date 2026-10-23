package xyz.mpv.rex.ui.player.controls.components.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import xyz.mpv.rex.ui.player.PlayerViewModel

@Composable
fun MoreSheet(
  onDismissRequest: () -> Unit,
  viewModel: PlayerViewModel,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(Unit) {
    onDismissRequest()
  }
}
