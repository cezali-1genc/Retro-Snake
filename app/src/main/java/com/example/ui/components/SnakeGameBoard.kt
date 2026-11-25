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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.model.SnakeColorMode
import com.example.model.VibrantSnakeColors
import com.example.model.resolveSnakeHeadColor
import com.example.model.resolveSnakeSegmentColor
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
    snakeColorMode: SnakeColorMode = SnakeColorMode.RAINBOW,
    customSnakeColorArgb: Int = 0xFF10B981.toInt(),
    enableSwipe: Boolean,
    onDirectionChange: (Direction) -> Unit,
    onTouchActivity: (Boolean) -> Unit = {},
    onBoardTap: () -> Unit = {},
    onBoardSizeMeasured: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Pulse & rotation animations for bonus items and Fever glow
    val infiniteTransition = rememberInfiniteTransition(label = "food_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val gemRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400)
        ),
        label = "gem_rotation"
    )

    // Brief subtle screen shake on crash
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

    // Gesture swipe tracking
    var dragAccumX by remember { mutableFloatStateOf(0f) }
    var dragAccumY by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 26f // ultra responsive touch

    // Particle Explosion, Shockwave & Floating Popups System
    val activeParticles = remember { mutableStateListOf<EatParticle>() }
    val activeShockwaves = remember { mutableStateListOf<EatShockwave>() }
    val activePopups = remember { mutableStateListOf<FloatingScorePopup>() }
    var lastProcessedEventId by remember { mutableLongStateOf(0L) }
    var frameTicker by remember { mutableLongStateOf(0L) }

    val hasActiveEffects = activeParticles.isNotEmpty() ||
        activeShockwaves.isNotEmpty() ||
        activePopups.isNotEmpty() ||
        state.doubleScoreUntilMs > System.currentTimeMillis()

    LaunchedEffect(hasActiveEffects) {
        while (
            activeParticles.isNotEmpty() ||
            activeShockwaves.isNotEmpty() ||
            activePopups.isNotEmpty() ||
            state.doubleScoreUntilMs > System.currentTimeMillis()
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
                detectTapGestures(
                    onTap = { onBoardTap() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumX = 0f
                        dragAccumY = 0f
                        onTouchActivity(true)
                    },
                    onDragEnd = {
                        onTouchActivity(false)
                    },
                    onDragCancel = {
                        onTouchActivity(false)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumX += dragAmount.x
                        dragAccumY += dragAmount.y

                        if (abs(dragAccumX) > dragThreshold || abs(dragAccumY) > dragThreshold) {
                            if (abs(dragAccumX) > abs(dragAccumY)) {
                                if (dragAccumX > 0) {
                                    onDirectionChange(Direction.RIGHT)
                                } else {
                                    onDirectionChange(Direction.LEFT)
                                }
                            } else {
                                if (dragAccumY > 0) {
                                    onDirectionChange(Direction.DOWN)
                                } else {
                                    onDirectionChange(Direction.UP)
                                }
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

    // Frameless edge-to-edge infinite board (no border, no shadow box, no clipped frame)
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
                (targetCols * (maxHeight / maxWidth)).roundToInt().coerceIn(22, 44)
            } else {
                28
            }
        }
        LaunchedEffect(targetCols, targetRows) {
            onBoardSizeMeasured(targetCols, targetRows)
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTicker
            val nowMs = System.currentTimeMillis()

            val cellWidth = size.width / state.boardWidth
            val cellHeight = size.height / state.boardHeight
            val cellMin = minOf(cellWidth, cellHeight)

            // Spawn particle explosion, shockwave ring, and floating score popup on eat
            val eatenEvent = state.lastEatenEvent
            if (eatenEvent != null && eatenEvent.id != lastProcessedEventId) {
                lastProcessedEventId = eatenEvent.id
                val originX = (eatenEvent.position.x + 0.5f) * cellWidth
                val originY = (eatenEvent.position.y + 0.5f) * cellHeight

                val colorPalette = when (eatenEvent.type) {
                    FoodType.REGULAR -> listOf(Color(0xFFEF4444), Color(0xFFF87171), Color(0xFF10B981), Color(0xFFFFFFFF))
                    FoodType.GOLDEN -> listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFFEF08A), Color(0xFFFFFFFF))
                    FoodType.SPEED_SLOW -> listOf(Color(0xFF06B6D4), Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFFFFFFFF))
                    FoodType.BERRY -> listOf(Color(0xFFA855F7), Color(0xFFD946EF), Color(0xFFC084FC), Color(0xFFFFFFFF))
                    FoodType.RAINBOW_GEM -> VibrantSnakeColors.RAINBOW + Color.White
                }

                val particleCount = if (eatenEvent.type == FoodType.REGULAR) 14 else 22
                for (i in 0 until particleCount) {
                    val angle = (Random.nextFloat() * 2f * Math.PI).toFloat()
                    val speed = Random.nextFloat() * 125f + 45f
                    activeParticles.add(
                        EatParticle(
                            x = originX,
                            y = originY,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            color = colorPalette[Random.nextInt(colorPalette.size)],
                            maxRadius = Random.nextFloat() * 3.8f + 2.5f
                        )
                    )
                }

                // Expanding shockwave ring
                val ringColor = when (eatenEvent.type) {
                    FoodType.REGULAR -> palette.primary
                    FoodType.GOLDEN -> palette.foodGolden
                    FoodType.SPEED_SLOW -> palette.foodSlow
                    FoodType.BERRY -> palette.foodBerry
                    FoodType.RAINBOW_GEM -> Color(0xFFEC4899)
                }
                activeShockwaves.add(
                    EatShockwave(
                        x = originX,
                        y = originY,
                        color = ringColor,
                        maxRadius = if (eatenEvent.type == FoodType.REGULAR) cellMin * 1.9f else cellMin * 3.1f
                    )
                )

                // Floating popup text
                val popupLabel = buildString {
                    append("+${eatenEvent.pointsEarned}")
                    if (eatenEvent.comboCount > 1) append(" x${eatenEvent.comboCount}")
                    when (eatenEvent.type) {
                        FoodType.SPEED_SLOW -> append(" YAVAŞ!")
                        FoodType.RAINBOW_GEM -> append(" 2X FEVER!")
                        else -> {}
                    }
                }
                activePopups.add(
                    FloatingScorePopup(
                        x = originX.coerceIn(70f, size.width - 70f),
                        y = originY.coerceIn(90f, size.height - 60f),
                        text = popupLabel,
                        color = ringColor
                    )
                )
            }

            // Draw subtle infinite grid dots
            for (x in 0 until state.boardWidth) {
                for (y in 0 until state.boardHeight) {
                    drawCircle(
                        color = palette.gridLine,
                        radius = 1.25f,
                        center = Offset((x + 0.5f) * cellWidth, (y + 0.5f) * cellHeight)
                    )
                }
            }

            // Rainbow Fever / 2X Score ambient edge aura
            if (state.doubleScoreUntilMs > nowMs) {
                val feverColor = VibrantSnakeColors.RAINBOW[((nowMs / 180L) % VibrantSnakeColors.RAINBOW.size).toInt()]
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            feverColor.copy(alpha = 0.22f),
                            Color.Transparent,
                            Color.Transparent,
                            feverColor.copy(alpha = 0.22f)
                        )
                    )
                )
            }

            // Draw Obstacles (Hard / Insane mode)
            state.obstacles.forEach { obs ->
                val left = obs.x * cellWidth
                val top = obs.y * cellHeight
                drawRoundRect(
                    color = palette.obstacle,
                    topLeft = Offset(left + 1.5f, top + 1.5f),
                    size = Size(cellWidth - 3f, cellHeight - 3f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(left + 2.5f, top + 2.5f),
                    size = Size(cellWidth - 5f, cellHeight - 5f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.2f)
                )
            }

            // Draw Regular Food (Ruby Red Apple)
            state.regularFood?.let { food ->
                val cx = (food.position.x + 0.5f) * cellWidth
                val cy = (food.position.y + 0.5f) * cellHeight
                val r = cellMin * 0.42f

                // Magnetic aura ring if Fever mode is pulling food
                if (state.doubleScoreUntilMs > nowMs) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.28f),
                        radius = r * 1.45f * pulseScale,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f)
                    )
                }

                drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = r, center = Offset(cx, cy + 2f))
                drawCircle(color = palette.foodRegular, radius = r, center = Offset(cx, cy))
                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = r * 0.32f, center = Offset(cx - r * 0.32f, cy - r * 0.28f))
                drawCircle(color = Color(0xFF10B981), radius = r * 0.35f, center = Offset(cx + 2f, cy - r - 1f))
            }

            // Draw Special Bonus Food (Golden Star, Cyan Potion, Purple Berry, Rainbow Gem)
            state.specialFood?.let { special ->
                val cx = (special.position.x + 0.5f) * cellWidth
                val cy = (special.position.y + 0.5f) * cellHeight
                val baseRadius = (cellMin * 0.46f) * pulseScale

                if (special.type == FoodType.RAINBOW_GEM) {
                    val prismColor = VibrantSnakeColors.RAINBOW[((nowMs / 150L) % VibrantSnakeColors.RAINBOW.size).toInt()]
                    drawCircle(color = prismColor.copy(alpha = 0.35f), radius = baseRadius * 1.6f, center = Offset(cx, cy))
                    rotate(degrees = gemRotation, pivot = Offset(cx, cy)) {
                        val path = Path().apply {
                            moveTo(cx, cy - baseRadius * 1.15f)
                            lineTo(cx + baseRadius * 0.95f, cy)
                            lineTo(cx, cy + baseRadius * 1.15f)
                            lineTo(cx - baseRadius * 0.95f, cy)
                            close()
                        }
                        drawPath(path = path, color = prismColor)
                        drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
                    }
                } else {
                    val color = when (special.type) {
                        FoodType.GOLDEN -> palette.foodGolden
                        FoodType.SPEED_SLOW -> palette.foodSlow
                        FoodType.BERRY -> palette.foodBerry
                        else -> palette.foodRegular
                    }

                    drawCircle(color = color.copy(alpha = 0.38f), radius = baseRadius * 1.45f, center = Offset(cx, cy))
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(cx - baseRadius, cy - baseRadius),
                        size = Size(baseRadius * 2, baseRadius * 2),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(cx - baseRadius * 0.55f, cy),
                        end = Offset(cx + baseRadius * 0.55f, cy),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(cx, cy - baseRadius * 0.55f),
                        end = Offset(cx, cy + baseRadius * 0.55f),
                        strokeWidth = 2.5f
                    )
                }
            }

            // Draw Customized Snake Body with Tapered Tail & Head Glow
            if (state.snake.isNotEmpty()) {
                val body = state.snake.drop(1)
                val bodySize = body.size

                body.forEachIndexed { index, seg ->
                    val left = seg.x * cellWidth
                    val top = seg.y * cellHeight

                    // Taper the last 2 tail segments smoothly
                    val tailFactor = when {
                        index == bodySize - 1 && bodySize >= 2 -> 0.22f
                        index == bodySize - 2 && bodySize >= 3 -> 0.12f
                        else -> 0.04f
                    }
                    val padX = cellWidth * tailFactor + 1.0f
                    val padY = cellHeight * tailFactor + 1.0f

                    val segmentColor = resolveSnakeSegmentColor(
                        mode = snakeColorMode,
                        customColorArgb = customSnakeColorArgb,
                        palette = palette,
                        index = index
                    )

                    drawRoundRect(
                        color = segmentColor,
                        topLeft = Offset(left + padX, top + padY),
                        size = Size((cellWidth - padX * 2).coerceAtLeast(4f), (cellHeight - padY * 2).coerceAtLeast(4f)),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Inner glossy glint
                    drawCircle(
                        color = Color.White.copy(alpha = 0.32f),
                        radius = cellMin * (0.16f - tailFactor * 0.35f).coerceAtLeast(0.06f),
                        center = Offset(left + cellWidth / 2, top + cellHeight / 2)
                    )
                }

                // Draw Snake Head with Subtle Aura
                val head = state.snake.first()
                val headLeft = head.x * cellWidth
                val headTop = head.y * cellHeight
                val padding = 0.8f
                val headColor = resolveSnakeHeadColor(
                    mode = snakeColorMode,
                    customColorArgb = customSnakeColorArgb,
                    palette = palette
                )

                // Soft head aura
                drawCircle(
                    color = headColor.copy(alpha = 0.26f),
                    radius = cellMin * 0.72f,
                    center = Offset(headLeft + cellWidth / 2f, headTop + cellHeight / 2f)
                )

                drawRoundRect(
                    color = headColor,
                    topLeft = Offset(headLeft + padding, headTop + padding),
                    size = Size(cellWidth - padding * 2, cellHeight - padding * 2),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // Expressive eyes pointing in motion direction
                val eyeRadius = cellMin * 0.17f
                val pupilRadius = eyeRadius * 0.55f

                val (eye1Offset, eye2Offset) = when (state.currentDirection) {
                    Direction.RIGHT -> Pair(
                        Offset(headLeft + cellWidth * 0.72f, headTop + cellHeight * 0.28f),
                        Offset(headLeft + cellWidth * 0.72f, headTop + cellHeight * 0.72f)
                    )
                    Direction.LEFT -> Pair(
                        Offset(headLeft + cellWidth * 0.28f, headTop + cellHeight * 0.28f),
                        Offset(headLeft + cellWidth * 0.28f, headTop + cellHeight * 0.72f)
                    )
                    Direction.UP -> Pair(
                        Offset(headLeft + cellWidth * 0.28f, headTop + cellHeight * 0.28f),
                        Offset(headLeft + cellWidth * 0.72f, headTop + cellHeight * 0.28f)
                    )
                    Direction.DOWN -> Pair(
                        Offset(headLeft + cellWidth * 0.28f, headTop + cellHeight * 0.72f),
                        Offset(headLeft + cellWidth * 0.72f, headTop + cellHeight * 0.72f)
                    )
                }

                drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = eyeRadius * 1.15f, center = eye1Offset)
                drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = eyeRadius * 1.15f, center = eye2Offset)
                drawCircle(color = Color.White, radius = eyeRadius, center = eye1Offset)
                drawCircle(color = Color.White, radius = eyeRadius, center = eye2Offset)
                drawCircle(color = Color.Black, radius = pupilRadius, center = eye1Offset)
                drawCircle(color = Color.Black, radius = pupilRadius, center = eye2Offset)

                // Tongue when running
                if (state.status == GameStatus.RUNNING) {
                    val tongueLen = cellMin * 0.38f
                    val tongueWidth = 2.8f
                    val (tStart, tEnd) = when (state.currentDirection) {
                        Direction.RIGHT -> Pair(
                            Offset(headLeft + cellWidth, headTop + cellHeight / 2),
                            Offset(headLeft + cellWidth + tongueLen, headTop + cellHeight / 2)
                        )
                        Direction.LEFT -> Pair(
                            Offset(headLeft, headTop + cellHeight / 2),
                            Offset(headLeft - tongueLen, headTop + cellHeight / 2)
                        )
                        Direction.UP -> Pair(
                            Offset(headLeft + cellWidth / 2, headTop),
                            Offset(headLeft + cellWidth / 2, headTop - tongueLen)
                        )
                        Direction.DOWN -> Pair(
                            Offset(headLeft + cellWidth / 2, headTop + cellHeight),
                            Offset(headLeft + cellWidth / 2, headTop + cellHeight + tongueLen)
                        )
                    }
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = tStart,
                        end = tEnd,
                        strokeWidth = tongueWidth
                    )
                }
            }

            // Draw Expanding Shockwave Rings
            activeShockwaves.forEach { ring ->
                val progress = ((nowMs - ring.createdAt).toFloat() / ring.durationMs).coerceIn(0f, 1f)
                val radius = ring.maxRadius * progress
                val alpha = (1f - progress).coerceIn(0f, 1f) * 0.7f
                drawCircle(
                    color = ring.color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(ring.x, ring.y),
                    style = Stroke(width = 3.5f * (1f - progress * 0.6f))
                )
            }

            // Draw Subtle Particle Explosions
            activeParticles.forEach { p ->
                val progress = ((nowMs - p.createdAt).toFloat() / p.durationMs).coerceIn(0f, 1f)
                val currentX = p.x + (p.vx * progress)
                val currentY = p.y + (p.vy * progress) + (16f * progress * progress)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val radius = p.maxRadius * (1f - progress * 0.45f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.75f),
                    radius = radius * 0.45f,
                    center = Offset(currentX, currentY)
                )
            }

            // Draw Floating Score & Combo Popups
            activePopups.forEach { popup ->
                val progress = ((nowMs - popup.createdAt).toFloat() / popup.durationMs).coerceIn(0f, 1f)
                val alpha = if (progress < 0.7f) 1f else ((1f - progress) / 0.3f).coerceIn(0f, 1f)
                val riseY = popup.y - (52f * progress)
                val measured = textMeasurer.measure(
                    text = popup.text,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                val textX = (popup.x - measured.size.width / 2f).coerceIn(8f, size.width - measured.size.width - 8f)
                val textY = (riseY - measured.size.height / 2f).coerceIn(8f, size.height - measured.size.height - 8f)

                drawText(
                    textLayoutResult = measured,
                    color = popup.color.copy(alpha = alpha),
                    topLeft = Offset(textX, textY)
                )
            }
        }
    }
}
