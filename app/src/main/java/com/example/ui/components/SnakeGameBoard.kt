package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SnakeGameState
import com.example.model.Direction
import com.example.model.FoodType
import com.example.model.GameStatus
import com.example.model.MaterialPalette
import com.example.model.Point
import com.example.model.SkinHeadStyle
import com.example.model.SnakeColorMode
import com.example.model.SnakeSkin
import com.example.model.SnakeSkinRepository
import com.example.model.VibrantSnakeColors
import com.example.model.resolveSnakeHeadColor
import com.example.model.resolveSnakeSegmentColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class EatParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val maxRadius: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 460L
)

data class EatShockwave(
    val x: Float,
    val y: Float,
    val color: Color,
    val maxRadius: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 420L
)

data class FloatingScorePopup(
    val x: Float,
    val y: Float,
    val text: String,
    val color: Color,
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 750L
)

@Composable
fun SnakeGameBoard(
    state: SnakeGameState,
    palette: MaterialPalette,
    snakeColorMode: SnakeColorMode = SnakeColorMode.SKIN,
    customSnakeColorArgb: Int = 0xFF10B981.toInt(),
    selectedSkinId: String = "neon",
    lowPerformanceMode: Boolean = false,
    enableSwipe: Boolean,
    onDirectionChange: (Direction) -> Unit,
    onTouchActivity: (Boolean) -> Unit = {},
    onBoardTap: () -> Unit = {},
    onBoardSizeMeasured: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val skin = remember(selectedSkinId) { SnakeSkinRepository.getById(selectedSkinId) }

    val infiniteTransition = rememberInfiniteTransition(label = "board_animations")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(440), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    val gemRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2400)),
        label = "gem_rotation"
    )
    val ambientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4800), RepeatMode.Reverse),
        label = "ambient_shift"
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(state.status) {
        if (state.status == GameStatus.GAME_OVER) {
            for (i in 0..5) {
                val target = if (i % 2 == 0) 10f * (1f - i / 6f) else -10f * (1f - i / 6f)
                shakeOffset.animateTo(target, tween(32))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    var dragAccumX by remember { mutableFloatStateOf(0f) }
    var dragAccumY by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 26f

    val activeParticles = remember { mutableStateListOf<EatParticle>() }
    val activeShockwaves = remember { mutableStateListOf<EatShockwave>() }
    val activePopups = remember { mutableStateListOf<FloatingScorePopup>() }
    var lastProcessedEventId by remember { mutableLongStateOf(0L) }
    var frameTicker by remember { mutableLongStateOf(0L) }

    val hasActiveEffects = activeParticles.isNotEmpty() ||
        activeShockwaves.isNotEmpty() ||
        activePopups.isNotEmpty() ||
        state.specialFood != null ||
        state.portalA != null ||
        state.doubleScoreUntilMs > System.currentTimeMillis() ||
        state.shieldUntilMs > System.currentTimeMillis()

    LaunchedEffect(hasActiveEffects) {
        while (
            activeParticles.isNotEmpty() ||
            activeShockwaves.isNotEmpty() ||
            activePopups.isNotEmpty() ||
            state.specialFood != null ||
            state.portalA != null ||
            state.doubleScoreUntilMs > System.currentTimeMillis() ||
            state.shieldUntilMs > System.currentTimeMillis()
        ) {
            withFrameNanos { now ->
                frameTicker = now
                val nowMs = System.currentTimeMillis()
                activeParticles.removeAll { nowMs - it.createdAt > it.durationMs }
                activeShockwaves.removeAll { nowMs - it.createdAt > it.durationMs }
                activePopups.removeAll { nowMs - it.createdAt > it.durationMs }
            }
        }
    }

    val gestureModifier = if (enableSwipe) {
        Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onBoardTap() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumX = 0f
                        dragAccumY = 0f
                        onTouchActivity(true)
                    },
                    onDragEnd = { onTouchActivity(false) },
                    onDragCancel = { onTouchActivity(false) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumX += dragAmount.x
                        dragAccumY += dragAmount.y

                        if (abs(dragAccumX) > dragThreshold || abs(dragAccumY) > dragThreshold) {
                            if (abs(dragAccumX) > abs(dragAccumY)) {
                                onDirectionChange(if (dragAccumX > 0) Direction.RIGHT else Direction.LEFT)
                            } else {
                                onDirectionChange(if (dragAccumY > 0) Direction.DOWN else Direction.UP)
                            }
                            dragAccumX = 0f
                            dragAccumY = 0f
                        }
                    }
                )
            }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .background(palette.boardBackground)
            .then(gestureModifier)
            .testTag("snake_game_board")
    ) {
        val targetCols = 20
        val targetRows = remember(maxWidth, maxHeight) {
            if (maxWidth > 0.dp && maxHeight > 0.dp) {
                (targetCols * (maxHeight / maxWidth)).roundToInt().coerceIn(18, 42)
            } else {
                26
            }
        }
        LaunchedEffect(targetCols, targetRows) {
            onBoardSizeMeasured(targetCols, targetRows)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTicker
            val nowMs = System.currentTimeMillis()
            val cellWidth = size.width / state.boardWidth
            val cellHeight = size.height / state.boardHeight
            val cellMin = minOf(cellWidth, cellHeight)

            // 1. Atmospheric Background
            if (!lowPerformanceMode) {
                val deepTint = lerp(palette.boardBackground, palette.primary, if (palette.isDark) 0.08f else 0.05f)
                drawRect(brush = Brush.verticalGradient(listOf(palette.boardBackground, deepTint, palette.boardBackground)))

                val orbAlpha = if (palette.isDark) 0.13f else 0.09f
                val orb1Center = Offset(size.width * (0.2f + 0.14f * ambientShift), size.height * (0.24f + 0.08f * ambientShift))
                drawCircle(
                    brush = Brush.radialGradient(listOf(palette.primary.copy(alpha = orbAlpha), Color.Transparent), orb1Center, size.width * 0.55f),
                    center = orb1Center,
                    radius = size.width * 0.55f
                )
            } else {
                drawRect(color = palette.boardBackground)
            }

            // 2. Micro-Checkerboard Tiles & Dots
            val tileTint = palette.gridLine.copy(alpha = if (palette.isDark) 0.24f else 0.34f)
            for (x in 0 until state.boardWidth) {
                for (y in 0 until state.boardHeight) {
                    if ((x + y) % 2 == 0) {
                        drawRect(color = tileTint, topLeft = Offset(x * cellWidth, y * cellHeight), size = Size(cellWidth, cellHeight))
                    }
                    if (!lowPerformanceMode) {
                        drawCircle(
                            color = palette.gridLine.copy(alpha = 0.6f),
                            radius = 1.15f,
                            center = Offset((x + 0.5f) * cellWidth, (y + 0.5f) * cellHeight)
                        )
                    }
                }
            }

            // Solid Wall Neon Frame if mode/level has solid walls
            if (state.hasSolidWalls) {
                drawRect(
                    color = palette.wallBorder.copy(alpha = 0.85f),
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // Particle / Shockwave / Floating Popup spawning
            val eatenEvent = state.lastEatenEvent
            if (eatenEvent != null && eatenEvent.id != lastProcessedEventId) {
                lastProcessedEventId = eatenEvent.id
                val originX = (eatenEvent.position.x + 0.5f) * cellWidth
                val originY = (eatenEvent.position.y + 0.5f) * cellHeight
                val ringColor = foodColor(eatenEvent.type, palette, nowMs)

                if (!lowPerformanceMode) {
                    val particleCount = if (eatenEvent.type == FoodType.REGULAR) 12 else 20
                    for (i in 0 until particleCount) {
                        val angle = (Random.nextFloat() * 2f * PI).toFloat()
                        val speed = Random.nextFloat() * 130f + 45f
                        activeParticles.add(
                            EatParticle(
                                x = originX,
                                y = originY,
                                vx = cos(angle) * speed,
                                vy = sin(angle) * speed,
                                color = if (i % 3 == 0) Color.White else ringColor,
                                maxRadius = Random.nextFloat() * 3.6f + 2.4f
                            )
                        )
                    }
                    activeShockwaves.add(
                        EatShockwave(
                            x = originX,
                            y = originY,
                            color = ringColor,
                            maxRadius = if (eatenEvent.type == FoodType.REGULAR) cellMin * 2.0f else cellMin * 3.1f
                        )
                    )
                }

                val popupLabel = eatenEvent.customLabel ?: buildString {
                    append("+${eatenEvent.pointsEarned}")
                    if (eatenEvent.comboCount > 1) append(" x${eatenEvent.comboCount}")
                }
                activePopups.add(
                    FloatingScorePopup(
                        x = originX.coerceIn(72f, size.width - 72f),
                        y = originY.coerceIn(72f, size.height - 60f),
                        text = popupLabel,
                        color = ringColor
                    )
                )
            }

            // Fever Edge Aura
            if (state.doubleScoreUntilMs > nowMs && !lowPerformanceMode) {
                val feverColor = VibrantSnakeColors.RAINBOW[((nowMs / 170L) % VibrantSnakeColors.RAINBOW.size).toInt()]
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(feverColor.copy(alpha = 0.22f), Color.Transparent, Color.Transparent, feverColor.copy(alpha = 0.22f))
                    )
                )
            }

            // 3. Linked Quantum Portals
            listOfNotNull(state.portalA, state.portalB).forEachIndexed { idx, p ->
                val cx = (p.x + 0.5f) * cellWidth
                val cy = (p.y + 0.5f) * cellHeight
                val portalColor = if (idx == 0) Color(0xFFA855F7) else Color(0xFF06B6D4)
                drawCircle(color = portalColor.copy(alpha = 0.28f), radius = cellMin * 0.65f * pulseScale, center = Offset(cx, cy))
                rotate(degrees = if (idx == 0) gemRotation else -gemRotation, pivot = Offset(cx, cy)) {
                    drawArc(
                        color = portalColor,
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(cx - cellMin * 0.44f, cy - cellMin * 0.44f),
                        size = Size(cellMin * 0.88f, cellMin * 0.88f),
                        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                    )
                }
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = cellMin * 0.18f, center = Offset(cx, cy))
            }

            // 4. Static Obstacles
            state.obstacles.forEach { obs ->
                val left = obs.x * cellWidth
                val top = obs.y * cellHeight
                drawRoundRect(
                    color = palette.obstacle,
                    topLeft = Offset(left + 1.5f, top + 1.5f),
                    size = Size(cellWidth - 3f, cellHeight - 3f),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.34f),
                    topLeft = Offset(left + 3f, top + 3f),
                    size = Size(cellWidth - 6f, cellHeight - 6f),
                    cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx()),
                    style = Stroke(width = 1.4f)
                )
            }

            // 5. Moving Patrol Obstacles
            state.movingObstacles.forEach { m ->
                val cx = (m.position.x + 0.5f) * cellWidth
                val cy = (m.position.y + 0.5f) * cellHeight
                val r = cellMin * 0.44f
                rotate(degrees = gemRotation * 0.8f, pivot = Offset(cx, cy)) {
                    drawRoundRect(
                        color = Color(0xFFFF5722),
                        topLeft = Offset(cx - r, cy - r),
                        size = Size(r * 2f, r * 2f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFFACC15),
                        topLeft = Offset(cx - r * 0.55f, cy - r * 0.55f),
                        size = Size(r * 1.1f, r * 1.1f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // 6. Regular Food (Glossy Apple)
            state.regularFood?.let { food ->
                val cx = (food.position.x + 0.5f) * cellWidth
                val cy = (food.position.y + 0.5f) * cellHeight
                val r = cellMin * 0.43f

                if (state.magnetUntilMs > nowMs || state.doubleScoreUntilMs > nowMs) {
                    drawCircle(
                        color = Color(0xFFA855F7).copy(alpha = 0.34f),
                        radius = r * 1.55f * pulseScale,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.2f)
                    )
                }

                drawCircle(color = palette.foodRegular.copy(alpha = 0.24f), radius = r * 1.38f * pulseScale, center = Offset(cx, cy))
                drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = r, center = Offset(cx, cy + 2.5f))
                drawCircle(color = palette.foodRegular, radius = r, center = Offset(cx, cy))
                drawCircle(color = lerp(palette.foodRegular, Color.White, 0.38f), radius = r * 0.55f, center = Offset(cx - r * 0.16f, cy - r * 0.16f))
                drawCircle(color = Color.White.copy(alpha = 0.72f), radius = r * 0.26f, center = Offset(cx - r * 0.32f, cy - r * 0.30f))
                drawLine(color = Color(0xFF78350F), start = Offset(cx, cy - r * 0.75f), end = Offset(cx + r * 0.12f, cy - r * 1.18f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawCircle(color = Color(0xFF10B981), radius = r * 0.32f, center = Offset(cx + r * 0.38f, cy - r * 0.95f))
            }

            // 7. Special Bonus & Power-Up Foods with Countdown Ring
            state.specialFood?.let { special ->
                val cx = (special.position.x + 0.5f) * cellWidth
                val cy = (special.position.y + 0.5f) * cellHeight
                val baseRadius = (cellMin * 0.44f) * pulseScale
                val remainingFraction = (1f - (nowMs - special.spawnTime).toFloat() / special.durationMs.toFloat()).coerceIn(0f, 1f)
                val specialColor = foodColor(special.type, palette, nowMs)

                val ringRadius = cellMin * 0.62f
                drawArc(
                    color = specialColor.copy(alpha = 0.22f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(cx - ringRadius, cy - ringRadius),
                    size = Size(ringRadius * 2f, ringRadius * 2f),
                    style = Stroke(width = 2.2f)
                )
                drawArc(
                    color = specialColor,
                    startAngle = -90f,
                    sweepAngle = 360f * remainingFraction,
                    useCenter = false,
                    topLeft = Offset(cx - ringRadius, cy - ringRadius),
                    size = Size(ringRadius * 2f, ringRadius * 2f),
                    style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                )

                when (special.type) {
                    FoodType.DIAMOND, FoodType.DOUBLE_SCORE -> {
                        drawCircle(color = specialColor.copy(alpha = 0.34f), radius = baseRadius * 1.45f, center = Offset(cx, cy))
                        rotate(degrees = gemRotation, pivot = Offset(cx, cy)) {
                            val path = Path().apply {
                                moveTo(cx, cy - baseRadius * 1.12f)
                                lineTo(cx + baseRadius * 0.92f, cy)
                                lineTo(cx, cy + baseRadius * 1.12f)
                                lineTo(cx - baseRadius * 0.92f, cy)
                                close()
                            }
                            drawPath(path = path, color = specialColor)
                            drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
                        }
                    }
                    FoodType.GOLDEN -> {
                        drawCircle(color = specialColor.copy(alpha = 0.34f), radius = baseRadius * 1.4f, center = Offset(cx, cy))
                        rotate(degrees = gemRotation * 0.5f, pivot = Offset(cx, cy)) {
                            val starPath = createStarPath(cx, cy, baseRadius * 1.1f, baseRadius * 0.48f, 5)
                            drawPath(path = starPath, color = specialColor)
                            drawPath(path = starPath, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = 1.6f))
                        }
                    }
                    FoodType.MYSTERY_BOX -> {
                        drawRoundRect(
                            color = Color(0xFFF59E0B),
                            topLeft = Offset(cx - baseRadius * 0.88f, cy - baseRadius * 0.88f),
                            size = Size(baseRadius * 1.76f, baseRadius * 1.76f),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(cx - baseRadius * 0.88f, cy - baseRadius * 0.88f),
                            size = Size(baseRadius * 1.76f, baseRadius * 1.76f),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            style = Stroke(width = 2f)
                        )
                        drawCircle(color = Color.White, radius = baseRadius * 0.28f, center = Offset(cx, cy))
                    }
                    else -> {
                        drawCircle(color = specialColor.copy(alpha = 0.34f), radius = baseRadius * 1.38f, center = Offset(cx, cy))
                        drawCircle(color = specialColor, radius = baseRadius * 0.92f, center = Offset(cx, cy))
                        drawCircle(color = Color.White.copy(alpha = 0.45f), radius = baseRadius * 0.34f, center = Offset(cx - baseRadius * 0.25f, cy - baseRadius * 0.25f))
                        drawLine(color = Color.White, start = Offset(cx - baseRadius * 0.42f, cy), end = Offset(cx + baseRadius * 0.42f, cy), strokeWidth = 2.4f, cap = StrokeCap.Round)
                        drawLine(color = Color.White, start = Offset(cx, cy - baseRadius * 0.42f), end = Offset(cx, cy + baseRadius * 0.42f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    }
                }
            }

            // 8. 3D-feel Snake with Selected Skin / Color
            drawEnhancedSnake(
                state = state,
                palette = palette,
                snakeColorMode = snakeColorMode,
                customSnakeColorArgb = customSnakeColorArgb,
                skin = skin,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                cellMin = cellMin,
                nowMs = nowMs,
                shieldActive = state.shieldUntilMs > nowMs
            )

            // 9. Shockwaves & Particles
            activeShockwaves.forEach { ring ->
                val progress = ((nowMs - ring.createdAt).toFloat() / ring.durationMs).coerceIn(0f, 1f)
                val radius = ring.maxRadius * progress
                val alpha = (1f - progress).coerceIn(0f, 1f) * 0.72f
                drawCircle(color = ring.color.copy(alpha = alpha), radius = radius, center = Offset(ring.x, ring.y), style = Stroke(width = 3.5f * (1f - progress * 0.6f)))
            }

            activeParticles.forEach { p ->
                val progress = ((nowMs - p.createdAt).toFloat() / p.durationMs).coerceIn(0f, 1f)
                val currentX = p.x + (p.vx * progress)
                val currentY = p.y + (p.vy * progress) + (16f * progress * progress)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val radius = p.maxRadius * (1f - progress * 0.45f)
                drawCircle(color = p.color.copy(alpha = alpha), radius = radius, center = Offset(currentX, currentY))
            }

            // 10. Floating Score Popups
            activePopups.forEach { popup ->
                val progress = ((nowMs - popup.createdAt).toFloat() / popup.durationMs).coerceIn(0f, 1f)
                val alpha = if (progress < 0.7f) 1f else ((1f - progress) / 0.3f).coerceIn(0f, 1f)
                val riseY = popup.y - (52f * progress)
                val measured = textMeasurer.measure(
                    text = popup.text,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                )
                val textX = (popup.x - measured.size.width / 2f).coerceIn(8f, size.width - measured.size.width - 8f)
                val textY = (riseY - measured.size.height / 2f).coerceIn(8f, size.height - measured.size.height - 8f)
                drawText(textLayoutResult = measured, color = popup.color.copy(alpha = alpha), topLeft = Offset(textX, textY))
            }
        }
    }
}

private fun foodColor(type: FoodType, palette: MaterialPalette, nowMs: Long): Color = when (type) {
    FoodType.REGULAR -> palette.foodRegular
    FoodType.GOLDEN -> palette.foodGolden
    FoodType.DIAMOND -> Color(0xFF38BDF8)
    FoodType.ENERGY_ORB -> Color(0xFFFACC15)
    FoodType.SHIELD -> Color(0xFF06B6D4)
    FoodType.SPEED_SLOW -> palette.foodSlow
    FoodType.MAGNET -> Color(0xFFA855F7)
    FoodType.DOUBLE_SCORE -> VibrantSnakeColors.RAINBOW[((nowMs / 150L) % VibrantSnakeColors.RAINBOW.size).toInt()]
    FoodType.MYSTERY_BOX -> Color(0xFFF97316)
}

private fun DrawScope.drawEnhancedSnake(
    state: SnakeGameState,
    palette: MaterialPalette,
    snakeColorMode: SnakeColorMode,
    customSnakeColorArgb: Int,
    skin: SnakeSkin,
    cellWidth: Float,
    cellHeight: Float,
    cellMin: Float,
    nowMs: Long,
    shieldActive: Boolean
) {
    if (state.snake.isEmpty()) return

    val headColor = resolveSnakeHeadColor(snakeColorMode, customSnakeColorArgb, palette, skin)
    val snakeSize = state.snake.size
    val body = state.snake.drop(1)
    val bodySize = body.size

    // Soft drop-shadow + connected organic bridges
    for (i in 0 until snakeSize - 1) {
        val a = state.snake[i]
        val b = state.snake[i + 1]
        if (abs(a.x - b.x) + abs(a.y - b.y) == 1) {
            val bridgeColor = if (i == 0) headColor else resolveSnakeSegmentColor(snakeColorMode, customSnakeColorArgb, palette, i - 1, bodySize, skin)
            val ax = (a.x + 0.5f) * cellWidth
            val ay = (a.y + 0.5f) * cellHeight
            val bx = (b.x + 0.5f) * cellWidth
            val by = (b.y + 0.5f) * cellHeight
            val strokeScale = if (i >= snakeSize - 2 && snakeSize >= 4) 0.58f else 0.78f
            drawLine(color = bridgeColor, start = Offset(ax, ay), end = Offset(bx, by), strokeWidth = cellMin * strokeScale, cap = StrokeCap.Round)
        }
    }

    for (index in bodySize - 1 downTo 0) {
        val seg = body[index]
        val left = seg.x * cellWidth
        val top = seg.y * cellHeight
        val tailFactor = when {
            index == bodySize - 1 && bodySize >= 2 -> 0.22f
            index == bodySize - 2 && bodySize >= 3 -> 0.12f
            else -> 0.05f
        }
        val padX = cellWidth * tailFactor + 0.8f
        val padY = cellHeight * tailFactor + 0.8f

        val segmentColor = resolveSnakeSegmentColor(snakeColorMode, customSnakeColorArgb, palette, index, bodySize, skin)
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.18f),
            topLeft = Offset(left + padX + 1.5f, top + padY + 2.5f),
            size = Size((cellWidth - padX * 2).coerceAtLeast(4f), (cellHeight - padY * 2).coerceAtLeast(4f)),
            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
        )
        drawRoundRect(
            color = segmentColor,
            topLeft = Offset(left + padX, top + padY),
            size = Size((cellWidth - padX * 2).coerceAtLeast(4f), (cellHeight - padY * 2).coerceAtLeast(4f)),
            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.34f),
            radius = cellMin * (0.16f - tailFactor * 0.34f).coerceAtLeast(0.06f),
            center = Offset(left + cellWidth * 0.44f, top + cellHeight * 0.42f)
        )
    }

    val head = state.snake.first()
    val headLeft = head.x * cellWidth
    val headTop = head.y * cellHeight
    val headCenter = Offset(headLeft + cellWidth / 2f, headTop + cellHeight / 2f)

    if (shieldActive) {
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.42f),
            radius = cellMin * 0.82f,
            center = headCenter,
            style = Stroke(width = 3.2f)
        )
    } else {
        drawCircle(
            brush = Brush.radialGradient(listOf(headColor.copy(alpha = 0.38f), Color.Transparent), headCenter, cellMin * 0.95f),
            center = headCenter,
            radius = cellMin * 0.95f
        )
    }

    drawRoundRect(
        color = headColor,
        topLeft = Offset(headLeft + 0.6f, headTop + 0.6f),
        size = Size(cellWidth - 1.2f, cellHeight - 1.2f),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawCircle(color = Color.White.copy(alpha = 0.25f), radius = cellMin * 0.22f, center = Offset(headLeft + cellWidth * 0.38f, headTop + cellHeight * 0.36f))

    // Skin head accent (Crown / Cyber Visor / Horns)
    if (snakeColorMode == SnakeColorMode.SKIN) {
        when (skin.headStyle) {
            SkinHeadStyle.CYBER_VISOR -> {
                drawRoundRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(headLeft + cellWidth * 0.22f, headTop + cellHeight * 0.22f),
                    size = Size(cellWidth * 0.56f, cellHeight * 0.56f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 2f)
                )
            }
            SkinHeadStyle.CROWNED -> {
                drawCircle(color = Color(0xFFFACC15), radius = cellMin * 0.16f, center = Offset(headCenter.x, headTop + cellHeight * 0.15f))
            }
            SkinHeadStyle.HORNED -> {
                drawCircle(color = Color(0xFFF97316), radius = cellMin * 0.12f, center = Offset(headLeft + cellWidth * 0.2f, headTop + cellHeight * 0.16f))
                drawCircle(color = Color(0xFFF97316), radius = cellMin * 0.12f, center = Offset(headLeft + cellWidth * 0.8f, headTop + cellHeight * 0.16f))
            }
            else -> {}
        }
    }

    val eyeRadius = cellMin * 0.18f
    val pupilRadius = eyeRadius * 0.56f
    val (eye1Offset, eye2Offset) = when (state.currentDirection) {
        Direction.RIGHT -> Pair(Offset(headLeft + cellWidth * 0.70f, headTop + cellHeight * 0.28f), Offset(headLeft + cellWidth * 0.70f, headTop + cellHeight * 0.72f))
        Direction.LEFT -> Pair(Offset(headLeft + cellWidth * 0.30f, headTop + cellHeight * 0.28f), Offset(headLeft + cellWidth * 0.30f, headTop + cellHeight * 0.72f))
        Direction.UP -> Pair(Offset(headLeft + cellWidth * 0.28f, headTop + cellHeight * 0.30f), Offset(headLeft + cellWidth * 0.72f, headTop + cellHeight * 0.30f))
        Direction.DOWN -> Pair(Offset(headLeft + cellWidth * 0.28f, headTop + cellHeight * 0.70f), Offset(headLeft + cellWidth * 0.72f, headTop + cellHeight * 0.70f))
    }

    listOf(eye1Offset, eye2Offset).forEach { eye ->
        drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = eyeRadius * 1.15f, center = eye)
        drawCircle(color = Color.White, radius = eyeRadius, center = eye)
        drawCircle(color = Color.Black, radius = pupilRadius, center = eye)
        drawCircle(color = Color.White, radius = pupilRadius * 0.4f, center = Offset(eye.x - pupilRadius * 0.35f, eye.y - pupilRadius * 0.35f))
    }

    if (state.status == GameStatus.RUNNING && (nowMs / 180L) % 3L != 0L) {
        val tongueLen = cellMin * 0.42f
        val (tStart, tEnd) = when (state.currentDirection) {
            Direction.RIGHT -> Pair(Offset(headLeft + cellWidth, headTop + cellHeight / 2f), Offset(headLeft + cellWidth + tongueLen, headTop + cellHeight / 2f))
            Direction.LEFT -> Pair(Offset(headLeft, headTop + cellHeight / 2f), Offset(headLeft - tongueLen, headTop + cellHeight / 2f))
            Direction.UP -> Pair(Offset(headLeft + cellWidth / 2f, headTop), Offset(headLeft + cellWidth / 2f, headTop - tongueLen))
            Direction.DOWN -> Pair(Offset(headLeft + cellWidth / 2f, headTop + cellHeight), Offset(headLeft + cellWidth / 2f, headTop + cellHeight + tongueLen))
        }
        drawLine(color = Color(0xFFEF4444), start = tStart, end = tEnd, strokeWidth = 2.8f, cap = StrokeCap.Round)
    }
}

private fun createStarPath(cx: Float, cy: Float, outerRadius: Float, innerRadius: Float, points: Int): Path {
    val path = Path()
    val angleStep = (PI / points).toFloat()
    var angle = (-PI / 2).toFloat()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += angleStep
    }
    path.close()
    return path
}
