package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InGameTip
import com.example.model.MaterialPalette

/**
 * Dynamic In-Game Tip Banner for beginners.
 * Appears dynamically at the top corner of the game screen,
 * delivering concise hints on game mechanics, controls, bonuses, and speed.
 */
@Composable
fun DynamicTipBanner(
    tip: InGameTip?,
    palette: MaterialPalette,
    onDismiss: () -> Unit,
    onNextTip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tip_glow")
    val bulbScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bulb_scale"
    )

    AnimatedVisibility(
        visible = tip != null,
        enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        if (tip != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surfaceContainerHigh)
                    .border(1.5.dp, palette.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onNextTip() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("dynamic_tip_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left glowing icon badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(bulbScale)
                            .background(palette.primaryContainer, CircleShape)
                            .border(1.dp, palette.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tip.iconEmoji,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Tip text content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "İPUCU: ${tip.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Dokun: Sıradaki",
                                fontSize = 9.sp,
                                color = palette.outline
                            )
                        }
                        Text(
                            text = tip.message,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.secondary,
                            lineHeight = 15.sp
                        )
                    }

                    // Close / Dismiss button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("dismiss_tip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "İpucunu Kapat",
                            tint = palette.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
