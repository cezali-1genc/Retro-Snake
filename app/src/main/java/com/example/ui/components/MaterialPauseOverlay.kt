package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.game.SnakeGameState
import com.example.model.MaterialPalette

/**
 * Semi-transparent Material Design Pause Overlay.
 * Appears when the game is interrupted/paused, providing a sleek frosted backdrop
 * with quick access to Resume, Restart, Settings, and About developer controls.
 */
@Composable
fun MaterialPauseOverlay(
    state: SnakeGameState,
    palette: MaterialPalette,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.94f),
        exit = fadeOut() + scaleOut(targetScale = 0.94f),
        modifier = modifier
    ) {
        // Semi-transparent backdrop scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Material 3 Dialog Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(palette.surfaceContainer)
                    .border(2.dp, palette.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(22.dp)
                    .testTag("material_pause_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing Pause Icon Badge
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(palette.primaryContainer, CircleShape)
                            .border(1.5.dp, palette.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DURAKLATILDI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Brief in-game status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(palette.surfaceContainerHigh, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "SKOR", fontSize = 10.sp, color = palette.outline, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${state.score}",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "UZUNLUK", fontSize = 10.sp, color = palette.outline, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${state.snake.size}",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "HIZ", fontSize = 10.sp, color = palette.outline, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${state.currentTickMs}ms",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Primary Action: Resume (Devam Et)
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pause_resume_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = palette.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEVAM ET",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Action: Restart (Yeniden Başlat)
                    FilledTonalButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("pause_restart_btn"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = palette.surfaceContainerHigh,
                            contentColor = palette.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YENİDEN BAŞLAT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Row Actions: Settings (Ayarlar) & About (Hakkında)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("pause_settings_btn"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = palette.surfaceContainerHigh,
                                contentColor = palette.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "AYARLAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onAbout,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("pause_about_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "HAKKINDA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
