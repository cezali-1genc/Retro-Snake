package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SnakeGameState
import com.example.model.GameStatus
import com.example.model.MaterialPalette
import kotlinx.coroutines.delay

@Composable
fun RetroHUD(
    state: SnakeGameState,
    highScore: Int,
    palette: MaterialPalette,
    modifier: Modifier = Modifier
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.comboExpiresAtMs, state.doubleScoreUntilMs, state.slowMotionUntilMs, state.status) {
        while (state.status == GameStatus.RUNNING &&
            (state.comboExpiresAtMs > System.currentTimeMillis() ||
                state.doubleScoreUntilMs > System.currentTimeMillis() ||
                state.slowMotionUntilMs > System.currentTimeMillis())
        ) {
            nowMs = System.currentTimeMillis()
            delay(200L)
        }
        nowMs = System.currentTimeMillis()
    }

    val isComboActive = state.comboCount > 1 && state.comboExpiresAtMs > nowMs
    val isFeverActive = state.doubleScoreUntilMs > nowMs
    val isSlowActive = state.slowMotionUntilMs > nowMs

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceContainer.copy(alpha = 0.84f))
            .border(1.dp, palette.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Current Score
            Column {
                Text(
                    text = "SKOR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.secondary
                )
                Text(
                    text = String.format("%05d", state.score),
                    fontSize = 23.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.primary,
                    modifier = Modifier.testTag("hud_current_score")
                )
            }

            // Active Fun Status Badges (Combo, Fever 2X, Slow-Mo)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isComboActive) {
                    StatusPill(
                        text = "🔥 x${state.comboCount}",
                        bg = palette.foodGolden.copy(alpha = 0.22f),
                        fg = palette.foodGolden
                    )
                }
                if (isFeverActive) {
                    StatusPill(
                        text = "💎 2X",
                        bg = Color(0xFFEC4899).copy(alpha = 0.22f),
                        fg = Color(0xFFEC4899)
                    )
                }
                if (isSlowActive) {
                    StatusPill(
                        text = "❄️ YAVAŞ",
                        bg = palette.foodSlow.copy(alpha = 0.22f),
                        fg = palette.foodSlow
                    )
                }
            }

            // High Score
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "En Yüksek Skor",
                        tint = palette.foodGolden,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "REKOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    )
                }
                Text(
                    text = String.format("%05d", highScore.coerceAtLeast(state.score)),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = palette.foodGolden,
                    modifier = Modifier.testTag("hud_high_score")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Compact Speed Gauge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Oyun Hızı",
                tint = palette.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = state.difficulty.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))

            val speedProgress = ((state.difficulty.initialTickMs - state.currentTickMs).toFloat() /
                (state.difficulty.initialTickMs - state.difficulty.minTickMs).coerceAtLeast(1L)).coerceIn(0f, 1f)

            LinearProgressIndicator(
                progress = { speedProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = palette.primary,
                trackColor = palette.surfaceContainerHigh
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+${(speedProgress * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primary
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    bg: Color,
    fg: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fg
        )
    }
}
