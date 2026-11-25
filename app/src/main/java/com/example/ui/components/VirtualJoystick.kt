package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Direction
import com.example.model.MaterialPalette
import kotlin.math.abs
import kotlin.math.hypot

@Composable
fun VirtualJoystick(
    palette: MaterialPalette,
    onDirectionChange: (Direction) -> Unit,
    size: Dp = 148.dp,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var lastTriggeredDir by remember { mutableStateOf<Direction?>(null) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val maxRadius = (this.size.width / 2f) * 0.62f
                val deadzone = maxRadius * 0.24f

                detectDragGestures(
                    onDragStart = { startOffset ->
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val delta = startOffset - center
                        val dist = hypot(delta.x, delta.y)
                        knobOffset = if (dist > maxRadius) {
                            Offset(delta.x / dist * maxRadius, delta.y / dist * maxRadius)
                        } else {
                            delta
                        }
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        lastTriggeredDir = null
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        lastTriggeredDir = null
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val raw = knobOffset + dragAmount
                        val dist = hypot(raw.x, raw.y)
                        knobOffset = if (dist > maxRadius) {
                            Offset(raw.x / dist * maxRadius, raw.y / dist * maxRadius)
                        } else {
                            raw
                        }
                        if (dist > deadzone) {
                            val dir = if (abs(knobOffset.x) > abs(knobOffset.y)) {
                                if (knobOffset.x > 0f) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (knobOffset.y > 0f) Direction.DOWN else Direction.UP
                            }
                            if (dir != lastTriggeredDir) {
                                lastTriggeredDir = dir
                                onDirectionChange(dir)
                            }
                        }
                    }
                )
            }
            .testTag("virtual_joystick"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = this.size.minDimension / 2f - 4f

            // Outer glowing ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.surfaceContainerHigh.copy(alpha = 0.85f),
                        palette.surfaceContainer.copy(alpha = 0.65f)
                    ),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = palette.primary.copy(alpha = 0.55f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Crosshair guide ticks
            val tickLen = outerRadius * 0.2f
            drawLine(palette.outlineVariant.copy(alpha = 0.6f), Offset(center.x, 12f), Offset(center.x, 12f + tickLen), strokeWidth = 2f)
            drawLine(palette.outlineVariant.copy(alpha = 0.6f), Offset(center.x, this.size.height - 12f), Offset(center.x, this.size.height - 12f - tickLen), strokeWidth = 2f)
            drawLine(palette.outlineVariant.copy(alpha = 0.6f), Offset(12f, center.y), Offset(12f + tickLen, center.y), strokeWidth = 2f)
            drawLine(palette.outlineVariant.copy(alpha = 0.6f), Offset(this.size.width - 12f, center.y), Offset(this.size.width - 12f - tickLen, center.y), strokeWidth = 2f)

            // Thumb knob
            val knobCenter = center + knobOffset
            val knobRadius = outerRadius * 0.36f
            drawCircle(
                color = palette.primary.copy(alpha = 0.25f),
                radius = knobRadius * 1.25f,
                center = knobCenter
            )
            drawCircle(
                color = palette.primary,
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = knobRadius * 0.42f,
                center = Offset(knobCenter.x - knobRadius * 0.2f, knobCenter.y - knobRadius * 0.2f)
            )
        }
    }
}
