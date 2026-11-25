package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.HighScoreEntity
import com.example.game.SnakeGameState
import com.example.i18n.LocalizedStrings
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.MaterialPalette
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GameOverCard(
    state: SnakeGameState,
    highScore: Int,
    topScores: List<HighScoreEntity>,
    strings: LocalizedStrings,
    palette: MaterialPalette,
    onRestart: () -> Unit,
    onNextLevel: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLevelVictory = state.status == GameStatus.LEVEL_COMPLETE

    val scaleAnim = remember { Animatable(0.55f) }
    val alphaAnim = remember { Animatable(0f) }
    val backdropAlpha = remember { Animatable(0f) }
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(state.score, state.status) {
        launch { backdropAlpha.animateTo(0.65f, tween(420, easing = FastOutSlowInEasing)) }
        launch { alphaAnim.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
        launch { scaleAnim.animateTo(1f, tween(560, easing = FastOutSlowInEasing)) }
        launch { animatedScore.animateTo(state.score.toFloat(), tween(700, easing = FastOutSlowInEasing)) }
    }

    val elapsedSeconds = (state.elapsedMs / 1000L).coerceAtLeast(0L)
    val durationFormatted = String.format("%02d:%02d", elapsedSeconds / 60L, elapsedSeconds % 60L)
    val accentColor = if (isLevelVictory) palette.primary else palette.foodRegular

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backdropAlpha.value))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 390.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
                .shadow(18.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceContainer)
                .border(2.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("game_over_card"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(accentColor.copy(alpha = 0.18f), CircleShape)
                        .border(1.5.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLevelVictory) Icons.Default.EmojiEvents else Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isLevelVictory) strings.levelComplete else strings.gameOver,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    textAlign = TextAlign.Center
                )

                if (isLevelVictory && state.gameMode == GameMode.LEVELS) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (s in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (s <= state.starsEarnedInLevel) palette.foodGolden else palette.outlineVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                if (state.isNewHighScore) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(palette.primaryContainer, RoundedCornerShape(10.dp))
                            .border(1.dp, palette.primary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = strings.newRecord, tint = palette.foodGolden, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "★ ${strings.newRecord} ★", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surfaceContainerHigh, RoundedCornerShape(16.dp))
                        .border(1.5.dp, palette.primary.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                ) {
                    Text(text = strings.score, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.outline)
                    Text(
                        text = "${animatedScore.value.roundToInt()}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = palette.primary,
                        modifier = Modifier.testTag("game_over_score_text")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatBox(strings.highScore, "${highScore.coerceAtLeast(state.score)}", palette, Modifier.weight(1f))
                    StatBox(strings.foodEaten, "${state.foodEatenCount}", palette, Modifier.weight(1f))
                    StatBox(strings.duration, durationFormatted, palette, Modifier.weight(1f))
                }

                // Rewards & Badges Earned
                if (state.coinsEarnedInRun > 0 || state.sessionUnlockedBadges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.primaryContainer.copy(alpha = 0.35f))
                            .border(1.dp, palette.primary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = strings.rewardsEarned,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.primary
                        )
                        if (state.coinsEarnedInRun > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${state.coinsEarnedInRun} 🪙 ${strings.coinsLabel}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.foodGolden
                            )
                        }
                        state.sessionUnlockedBadges.forEach { badge ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${badge.iconEmoji} ${badge.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isLevelVictory && state.gameMode == GameMode.LEVELS && state.currentLevel < 100) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("game_over_next_level_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.nextLevel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("game_over_restart_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.playAgain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onMainMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("game_over_main_menu_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.surfaceContainerHigh, contentColor = palette.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.mainMenu, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = { shareScore(context, state) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_score_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.surfaceContainerHigh, contentColor = palette.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PAYLAŞ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    palette: MaterialPalette,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(palette.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .border(1.dp, palette.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = palette.outline, textAlign = TextAlign.Center)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = palette.secondary, textAlign = TextAlign.Center)
    }
}

private fun shareScore(context: Context, state: SnakeGameState) {
    val shareText = """
        🐍 RETRO SNAKE v${BuildConfig.VERSION_NAME} 🎮
        ━━━━━━━━━━━━━━━━━━━━━
        🏆 Skor: ${state.score}
        🕹️ Mod: ${state.gameMode.title}
        📏 Uzunluk: ${state.snake.size}
        🍎 Yem: ${state.foodEatenCount}
        ━━━━━━━━━━━━━━━━━━━━━
    """.trimIndent()
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Retro Snake Skorunu Paylaş"))
}
