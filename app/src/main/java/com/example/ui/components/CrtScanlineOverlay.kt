package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Authentic Retro CRT Scanline & Phosphor Vignette overlay.
 * Uses hardware-accelerated Canvas operations for 60fps performance on any device.
 */
@Composable
fun CrtScanlineOverlay(
    scanlineColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val lineSpacing = 4f

        // Draw horizontal scanlines
        var y = 0f
        while (y < height) {
            drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.2f
            )
            y += lineSpacing
        }

        // CRT screen edge vignette (subtle darkening towards corners)
        val vignette = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)),
            center = Offset(width / 2f, height / 2f),
            radius = (width.coerceAtLeast(height)) * 0.72f
        )
        drawRect(
            brush = vignette,
            size = Size(width, height)
        )
    }
}
