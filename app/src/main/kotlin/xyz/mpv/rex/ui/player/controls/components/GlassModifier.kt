package xyz.mpv.rex.ui.player.controls.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ultra-light high-performance replacement for glass surface:
 * Uses hardware-accelerated Compose primitives instead of CPU-bound Path boolean operations.
 */
fun Modifier.glassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    outerShadowColor: Color = Color.Transparent,
    outerShadowBlur: Dp = 0.dp,
    outerShadowOffsetX: Dp = 0.dp,
    outerShadowOffsetY: Dp = 0.dp,
    innerHighlightColor: Color = Color.Transparent,
    innerHighlightBlur: Dp = 0.dp,
    innerHighlightOffsetX: Dp = 0.dp,
    innerHighlightOffsetY: Dp = 0.dp,
    innerShadowColor: Color = Color.Transparent,
    innerShadowBlur: Dp = 0.dp,
    innerShadowOffsetX: Dp = 0.dp,
    innerShadowOffsetY: Dp = 0.dp,
): Modifier {
    var mod = this.clip(shape)
    if (backgroundColor.alpha > 0f) {
        mod = mod.background(backgroundColor, shape)
    }
    if (borderWidth > 0.dp && borderColor.alpha > 0f) {
        mod = mod.border(borderWidth, borderColor, shape)
    }
    return mod
}
