package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SnakeGameState
import com.example.i18n.LocalizedStrings
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.MaterialPalette
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RetroHUD(
    state: SnakeGameState,
    highScore: Int,
    strings: LocalizedStrings,
    palette: MaterialPalette,
    onPauseToggle: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(
        state.comboExpiresAtMs,
        state.doubleScoreUntilMs,
        state.slowMotionUntilMs,
        state.shieldUntilMs,
        state.magnetUntilMs,
        state.energyUntilMs,
        state.status
    ) {
        while (state.status == GameStatus.RUNNING &&
            (state.comboExpiresAtMs > System.currentTimeMillis() ||
                state.doubleScoreUntilMs > System.currentTimeMillis() ||
                state.slowMotionUntilMs > System.currentTimeMillis() ||
                state.shieldUntilMs > System.currentTimeMillis() ||
                state.magnetUntilMs > System.currentTimeMillis() ||
                state.energyUntilMs > System.currentTimeMillis())
        ) {
            nowMs = System.currentTimeMillis()
            delay(180L)
        }
        nowMs = System.currentTimeMillis()
    }

    val isCombo = state.comboCount > 1 && state.comboExpiresAtMs > nowMs
    val doubleSec = ((state.doubleScoreUntilMs - nowMs) / 1000L).toInt().coerceAtLeast(0)
    val slowSec = ((state.slowMotionUntilMs - nowMs) / 1000L).toInt().coerceAtLeast(0)
    val shieldSec = ((state.shieldUntilMs - nowMs) / 1000L).toInt().coerceAtLeast(0)
    val magnetSec = ((state.magnetUntilMs - nowMs) / 1000L).toInt().coerceAtLeast(0)
    val energySec = ((state.energyUntilMs - nowMs) / 1000L).toInt().coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Current Score
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(palette.primary.copy(alpha = 0.16f), palette.surfaceContainerHigh)
                        )
                    )
                    .border(1.dp, palette.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = strings.score, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = palette.secondary)
                Text(
                    text = String.format("%05d", state.score),
                    fontSize = 21.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.primary,
                    modifier = Modifier.testTag("hud_current_score")
                )
            }

            // Center Mode / Level / Length / Lives / Timer Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modeLabel = if (state.gameMode == GameMode.LEVELS) {
                        "${state.gameMode.iconEmoji} ${strings.level} ${state.currentLevel}"
                    } else {
                        "${state.gameMode.iconEmoji} ${state.gameMode.title}"
                    }
                    StatusChip(
                        text = modeLabel,
                        bg = palette.surfaceContainerHigh,
                        fg = palette.secondary,
                        onClick = onOpenAchievements
                    )

                    StatusChip(
                        text = "🐍 ${state.snake.size}",
                        bg = palette.primaryContainer.copy(alpha = 0.6f),
                        fg = palette.primary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.maxLives > 1) {
                        val hearts = "❤️".repeat(state.lives) + "🖤".repeat((state.maxLives - state.lives).coerceAtLeast(0))
                        StatusChip(
                            text = hearts,
                            bg = Color(0xFFEF4444).copy(alpha = 0.16f),
                            fg = Color(0xFFEF4444)
                        )
                    }
                    if (state.gameMode == GameMode.TIME_ATTACK || state.gameMode == GameMode.CHALLENGE) {
                        val secs = ((state.timeRemainingMs + 999L) / 1000L).coerceAtLeast(0L)
                        val urgent = secs <= 10L
                        StatusChip(
                            text = "⏱️ ${secs}s",
                            bg = (if (urgent) Color(0xFFEF4444) else palette.foodGolden).copy(alpha = 0.22f),
                            fg = if (urgent) Color(0xFFEF4444) else palette.foodGolden
                        )
                    }
                }
            }

            // Right: High Score + Pause Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceContainerHigh)
                        .border(1.dp, palette.foodGolden.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = strings.highScore,
                            tint = palette.foodGolden,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = strings.highScore, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = palette.secondary)
                    }
                    Text(
                        text = String.format("%05d", highScore.coerceAtLeast(state.score)),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = palette.foodGolden,
                        modifier = Modifier.testTag("hud_high_score")
                    )
                }

                IconButton(
                    onClick = onPauseToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.primaryContainer)
                        .border(1.5.dp, palette.primary, CircleShape)
                        .testTag("hud_pause_button")
                ) {
                    Icon(
                        imageVector = if (state.status == GameStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = strings.pause,
                        tint = palette.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Target progress bar for Levels / Challenge
        if (state.targetScore > 0) {
            Spacer(modifier = Modifier.height(5.dp))
            val pct = (state.score.toFloat() / state.targetScore.toFloat()).coerceIn(0f, 1f)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎯 ${state.score}/${state.targetScore}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.primary,
                    trackColor = palette.surfaceContainerHigh
                )
            }
        }

        // Active Power-up Timers & Combo Pills
        if (isCombo || state.doubleScoreUntilMs > nowMs || state.shieldUntilMs > nowMs ||
            state.magnetUntilMs > nowMs || state.energyUntilMs > nowMs || state.slowMotionUntilMs > nowMs
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isCombo) {
                    StatusChip("🔥 x${state.comboCount}", palette.foodGolden.copy(alpha = 0.22f), palette.foodGolden)
                }
                if (state.shieldUntilMs > nowMs) {
                    StatusChip("🛡️ ${shieldSec + 1}s", Color(0xFF38BDF8).copy(alpha = 0.22f), Color(0xFF38BDF8))
                }
                if (state.doubleScoreUntilMs > nowMs) {
                    StatusChip("💎 2X ${doubleSec + 1}s", Color(0xFFEC4899).copy(alpha = 0.22f), Color(0xFFEC4899))
                }
                if (state.magnetUntilMs > nowMs) {
                    StatusChip("🧲 ${magnetSec + 1}s", Color(0xFFA855F7).copy(alpha = 0.22f), Color(0xFFA855F7))
                }
                if (state.energyUntilMs > nowMs) {
                    StatusChip("⚡ ${energySec + 1}s", Color(0xFFFACC15).copy(alpha = 0.22f), Color(0xFFFACC15))
                }
                if (state.slowMotionUntilMs > nowMs) {
                    StatusChip("❄️ ${slowSec + 1}s", palette.foodSlow.copy(alpha = 0.22f), palette.foodSlow)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    bg: Color,
    fg: Color,
    onClick: (() -> Unit)? = null
) {
    val mod = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(bg)
        .border(1.dp, fg.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        .let { base ->
            if (onClick != null) {
                base.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = fg),
                    onClick = onClick
                )
            } else {
                base
            }
        }
        .padding(horizontal = 7.dp, vertical = 3.dp)

    Box(modifier = mod) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fg
        )
    }
}
