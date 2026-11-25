package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MaterialPalette
import com.example.model.SnakeColorMode
import com.example.model.SnakeColorPresets
import com.example.model.resolveSnakeHeadColor
import com.example.model.resolveSnakeSegmentColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnakeColorPickerCard(
    snakeColorMode: SnakeColorMode,
    customSnakeColorArgb: Int,
    palette: MaterialPalette,
    onSelectMode: (SnakeColorMode) -> Unit,
    onSelectCustomColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract HSV from customSnakeColorArgb for interactive spectrum sliders
    val hsvInitial = remember {
        FloatArray(3).also { AndroidColor.colorToHSV(customSnakeColorArgb, it) }
    }
    var hue by remember { mutableFloatStateOf(hsvInitial[0]) }
    var saturation by remember { mutableFloatStateOf(hsvInitial[1]) }
    var brightness by remember { mutableFloatStateOf(hsvInitial[2]) }

    // Keep HSV sliders synced when user taps a preset swatch
    LaunchedEffect(customSnakeColorArgb) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(customSnakeColorArgb, hsv)
        // Preserve hue if color is grayscale (saturation == 0)
        if (hsv[1] > 0.02f) {
            hue = hsv[0]
        }
        saturation = hsv[1]
        brightness = hsv[2]
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("snake_color_picker_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YILAN RENGİ ÖZELLEŞTİRME",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = palette.primary,
                        letterSpacing = 0.8.sp
                    )
                }

                // Current color badge when CUSTOM is active
                if (snakeColorMode == SnakeColorMode.CUSTOM) {
                    val hexStr = String.format("#%06X", 0xFFFFFF and customSnakeColorArgb)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.surfaceContainerHigh)
                            .border(1.dp, palette.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(Color(customSnakeColorArgb))
                                .border(1.dp, palette.outlineVariant, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = hexStr,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Mini Snake Preview Board
            SnakeLivePreview(
                snakeColorMode = snakeColorMode,
                customSnakeColorArgb = customSnakeColorArgb,
                palette = palette
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector Row (Gökkuşağı, Tema Rengi, Özel Renk)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SnakeColorMode.entries.forEach { mode ->
                    val isSelected = mode == snakeColorMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) palette.primary else palette.surfaceContainerHigh)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.primary else palette.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("snake_color_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) palette.onPrimary else palette.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hazır Renk Paleti",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Preset Color Swatches
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SnakeColorPresets.PRESETS.forEach { preset ->
                    val isSelected = snakeColorMode == SnakeColorMode.CUSTOM &&
                        customSnakeColorArgb == preset.colorArgb
                    val swatchColor = Color(preset.colorArgb)
                    val luminance = 0.299f * swatchColor.red + 0.587f * swatchColor.green + 0.114f * swatchColor.blue
                    val checkTint = if (luminance > 0.65f) Color.Black else Color.White

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onSelectCustomColor(preset.colorArgb) }
                            .testTag("snake_preset_${preset.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) palette.primary else palette.outlineVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "${preset.name} seçildi",
                                    tint = checkTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Hue Spectrum Picker
            Text(
                text = "Özel Renk Spektrumu",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            HueSpectrumBar(
                hue = hue,
                palette = palette,
                onHueChange = { newHue ->
                    hue = newHue
                    val effectiveSat = if (saturation < 0.2f) 0.9f else saturation
                    val effectiveVal = if (brightness < 0.25f) 0.95f else brightness
                    saturation = effectiveSat
                    brightness = effectiveVal
                    val newArgb = AndroidColor.HSVToColor(floatArrayOf(newHue, effectiveSat, effectiveVal))
                    onSelectCustomColor(newArgb)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Brightness / Tone Slider
            Text(
                text = "Parlaklık & Ton",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            BrightnessToneBar(
                hue = hue,
                saturation = saturation,
                brightness = brightness,
                palette = palette,
                onToneChange = { newSat, newVal ->
                    saturation = newSat
                    brightness = newVal
                    val newArgb = AndroidColor.HSVToColor(floatArrayOf(hue, newSat, newVal))
                    onSelectCustomColor(newArgb)
                }
            )
        }
    }
}

@Composable
private fun SnakeLivePreview(
    snakeColorMode: SnakeColorMode,
    customSnakeColorArgb: Int,
    palette: MaterialPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.boardBackground)
            .border(1.5.dp, palette.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .testTag("snake_color_live_preview")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 12
            val rows = 3
            val cellW = size.width / cols
            val cellH = size.height / rows
            val cellMin = minOf(cellW, cellH)

            // Subtle grid dots
            for (x in 0 until cols) {
                for (y in 0 until rows) {
                    drawCircle(
                        color = palette.gridLine,
                        radius = 1.2f,
                        center = Offset((x + 0.5f) * cellW, (y + 0.5f) * cellH)
                    )
                }
            }

            // Draw 7-segment mini snake moving right on middle row (y = 1)
            val headX = 8
            val rowY = 1
            val segCount = 6

            for (i in segCount downTo 1) {
                val gx = headX - i
                val left = gx * cellW
                val top = rowY * cellH
                val pad = 1.5f
                val segColor = resolveSnakeSegmentColor(
                    mode = snakeColorMode,
                    customColorArgb = customSnakeColorArgb,
                    palette = palette,
                    index = i - 1
                )

                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(left + pad, top + pad),
                    size = Size(cellW - pad * 2, cellH - pad * 2),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.32f),
                    radius = cellMin * 0.15f,
                    center = Offset(left + cellW / 2, top + cellH / 2)
                )
            }

            // Head
            val headLeft = headX * cellW
            val headTop = rowY * cellH
            val headColor = resolveSnakeHeadColor(snakeColorMode, customSnakeColorArgb, palette)
            drawRoundRect(
                color = headColor,
                topLeft = Offset(headLeft + 0.8f, headTop + 0.8f),
                size = Size(cellW - 1.6f, cellH - 1.6f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Eyes
            val eyeR = cellMin * 0.17f
            val pupilR = eyeR * 0.55f
            val e1 = Offset(headLeft + cellW * 0.72f, headTop + cellH * 0.28f)
            val e2 = Offset(headLeft + cellW * 0.72f, headTop + cellH * 0.72f)
            drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = eyeR * 1.15f, center = e1)
            drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = eyeR * 1.15f, center = e2)
            drawCircle(color = Color.White, radius = eyeR, center = e1)
            drawCircle(color = Color.White, radius = eyeR, center = e2)
            drawCircle(color = Color.Black, radius = pupilR, center = e1)
            drawCircle(color = Color.Black, radius = pupilR, center = e2)

            // Tongue
            drawLine(
                color = Color(0xFFEF4444),
                start = Offset(headLeft + cellW, headTop + cellH / 2),
                end = Offset(headLeft + cellW + cellMin * 0.38f, headTop + cellH / 2),
                strokeWidth = 2.5f
            )

            // Food ahead of snake
            val foodX = 10
            val fx = (foodX + 0.5f) * cellW
            val fy = (rowY + 0.5f) * cellH
            val fr = cellMin * 0.38f
            drawCircle(color = palette.foodRegular, radius = fr, center = Offset(fx, fy))
            drawCircle(color = Color.White.copy(alpha = 0.55f), radius = fr * 0.3f, center = Offset(fx - fr * 0.3f, fy - fr * 0.28f))
        }
    }
}

@Composable
private fun HueSpectrumBar(
    hue: Float,
    palette: MaterialPalette,
    onHueChange: (Float) -> Unit
) {
    val hueColors = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .border(1.5.dp, palette.outlineVariant, RoundedCornerShape(17.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onHueChange(fraction * 360f)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onHueChange(fraction * 360f)
                }
            }
            .testTag("snake_hue_spectrum_bar")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.horizontalGradient(hueColors)
            )
            val thumbX = (hue / 360f).coerceIn(0f, 1f) * size.width
            val thumbCenter = Offset(thumbX.coerceIn(14f, size.width - 14f), size.height / 2f)
            drawCircle(
                color = Color.White,
                radius = 11.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 12.5.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun BrightnessToneBar(
    hue: Float,
    saturation: Float,
    brightness: Float,
    palette: MaterialPalette,
    onToneChange: (Float, Float) -> Unit
) {
    // Left = dark saturated, Middle = pure vivid, Right = bright pastel
    val vividColor = remember(hue) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    }
    val darkColor = remember(hue) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 0.35f)))
    }
    val pastelColor = remember(hue) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 0.25f, 1f)))
    }

    // Map (saturation, brightness) to 0f..1f position on the bar
    val positionFraction = remember(saturation, brightness) {
        if (brightness < 0.99f) {
            ((brightness - 0.35f) / (1f - 0.35f) * 0.5f).coerceIn(0f, 0.5f)
        } else {
            (0.5f + ((1f - saturation) / 0.75f) * 0.5f).coerceIn(0.5f, 1f)
        }
    }

    fun fractionToTone(fraction: Float) {
        val f = fraction.coerceIn(0f, 1f)
        if (f <= 0.5f) {
            val v = 0.35f + (f / 0.5f) * 0.65f
            onToneChange(1f, v)
        } else {
            val s = 1f - ((f - 0.5f) / 0.5f) * 0.75f
            onToneChange(s, 1f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .border(1.5.dp, palette.outlineVariant, RoundedCornerShape(15.dp))
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    fractionToTone(offset.x / size.width.toFloat())
                }
            }
            .pointerInput(hue) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    fractionToTone(change.position.x / size.width.toFloat())
                }
            }
            .testTag("snake_brightness_tone_bar")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.horizontalGradient(listOf(darkColor, vividColor, pastelColor))
            )
            val thumbX = positionFraction * size.width
            val thumbCenter = Offset(thumbX.coerceIn(13f, size.width - 13f), size.height / 2f)
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 11.5.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
