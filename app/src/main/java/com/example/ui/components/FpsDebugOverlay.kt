package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MaterialPalette
import kotlinx.coroutines.isActive

/**
 * Accurately measures real rendering frame rate (FPS) via frame timestamps.
 */
@Composable
fun rememberFpsCounter(): Pair<State<Int>, State<Float>> {
    val fpsState = remember { mutableIntStateOf(60) }
    val frameTimeState = remember { mutableFloatStateOf(16.6f) }

    LaunchedEffect(Unit) {
        var frameCount = 0
        var lastReportNanos = System.nanoTime()
        var lastFrameNanos = lastReportNanos

        while (isActive) {
            withFrameNanos { nowNanos ->
                frameCount++
                val singleFrameTimeMs = (nowNanos - lastFrameNanos) / 1_000_000f
                lastFrameNanos = nowNanos

                val elapsedNanos = nowNanos - lastReportNanos
                if (elapsedNanos >= 600_000_000L) { // update every 600ms for smooth readable metric
                    val currentFps = (frameCount * 1_000_000_000L / elapsedNanos).toInt()
                    fpsState.intValue = currentFps.coerceIn(1, 144)
                    frameTimeState.floatValue = (1000f / currentFps.coerceAtLeast(1))
                    frameCount = 0
                    lastReportNanos = nowNanos
                }
            }
        }
    }

    return Pair(fpsState, frameTimeState)
}

/**
 * Corner FPS Debug Overlay.
 * Displays real-time frame rate with performance health indicator.
 * Tap to expand frame time and grid performance details.
 */
@Composable
fun FpsDebugOverlay(
    palette: MaterialPalette,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val (fpsState, frameTimeState) = rememberFpsCounter()
    val fps = fpsState.value
    val frameTime = frameTimeState.value
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when {
        fps >= 55 -> Color(0xFF10B981) // High performance Green
        fps >= 30 -> Color(0xFFF59E0B) // Acceptable Amber
        else -> Color(0xFFEF4444)      // Low performance Red
    }

    Box(
        modifier = modifier
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surfaceContainerHigh.copy(alpha = 0.92f))
            .border(1.dp, palette.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("fps_debug_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Pulse Status Indicator Dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = "$fps FPS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = palette.secondary
            )

            AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${String.format("%.1f", frameTime)}ms",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = palette.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 60Hz",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
