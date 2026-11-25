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
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
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
import com.example.model.MaterialPalette
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GameOverCard(
    state: SnakeGameState,
    highScore: Int,
    topScores: List<HighScoreEntity>,
    palette: MaterialPalette,
    onRestart: () -> Unit,
    onViewLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Slow, smooth scale-in + fade-in + score count-up animations
    val scaleAnim = remember { Animatable(0.55f) }
    val alphaAnim = remember { Animatable(0f) }
    val backdropAlpha = remember { Animatable(0f) }
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(state.score) {
        launch {
            backdropAlpha.animateTo(
                targetValue = 0.62f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animatedScore.animateTo(
                targetValue = state.score.toFloat(),
                animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing)
            )
        }
    }

    // Centered full-screen scrim + slowly scaling Game Over dialog
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backdropAlpha.value))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
                .shadow(18.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceContainer)
                .border(2.dp, palette.primary.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("game_over_card"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Icon Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(palette.foodRegular.copy(alpha = 0.16f), CircleShape)
                        .border(1.5.dp, palette.foodRegular.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = palette.foodRegular,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Game Over Title
                Text(
                    text = "OYUN BİTTİ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.foodRegular,
                    letterSpacing = 1.sp
                )

                if (state.isNewHighScore) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(palette.primaryContainer, RoundedCornerShape(10.dp))
                            .border(1.dp, palette.primary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Yeni Rekor",
                            tint = palette.foodGolden,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "★ YENİ REKOR KAYDEDİLDİ! ★",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hero Score Card with Count-Up Animation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surfaceContainerHigh, RoundedCornerShape(16.dp))
                        .border(1.5.dp, palette.primary.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "TOPLAM SKOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.outline,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${animatedScore.value.roundToInt()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = palette.primary,
                        modifier = Modifier.testTag("game_over_score_text")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "EN YÜKSEK",
                        value = "${highScore.coerceAtLeast(state.score)}",
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "UZUNLUK",
                        value = "${state.snake.size}",
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "YEM",
                        value = "${state.foodEatenCount}",
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                }

                // In-Game Over High Scores List
                if (topScores.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "EN YÜKSEK SKORLAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(palette.surfaceContainerHigh, RoundedCornerShape(12.dp))
                            .border(1.dp, palette.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        topScores.take(4).forEachIndexed { index, scoreItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val rankColor = when (index) {
                                        0 -> Color(0xFFFFD700)
                                        1 -> Color(0xFFC0C0C0)
                                        2 -> Color(0xFFCD7F32)
                                        else -> palette.outline
                                    }
                                    Text(
                                        text = "#${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = rankColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = scoreItem.playerName,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = palette.secondary
                                    )
                                }
                                Text(
                                    text = "${scoreItem.score} pts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (scoreItem.score == state.score) palette.primary else palette.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play Again Button (Primary Action)
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("game_over_restart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = palette.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEKRAR OYNA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Actions: Share Score & Leaderboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { shareScore(context, state) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_score_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = palette.surfaceContainerHigh,
                            contentColor = palette.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PAYLAŞ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = onViewLeaderboard,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("game_over_leaderboard_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = palette.surfaceContainerHigh,
                            contentColor = palette.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TÜM SKORLAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
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
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.outline,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = palette.secondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun shareScore(context: Context, state: SnakeGameState) {
    val shareText = """
        🐍 MATERIAL SNAKE v${BuildConfig.VERSION_NAME} 🎮
        ━━━━━━━━━━━━━━━━━━━━━
        🏆 Skor: ${state.score} puan!
        ⚡ Zorluk: ${state.difficulty.label}
        📏 Yılan Boyu: ${state.snake.size}
        🍎 Toplanan Yem: ${state.foodEatenCount}
        ━━━━━━━━━━━━━━━━━━━━━
        Bu skoru geçebilir misin?
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Material Snake Skorunu Paylaş")
    context.startActivity(shareIntent)
}
