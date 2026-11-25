package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStatus
import com.example.model.MaterialPalette

/**
 * Single floating bottom-right control button that combines all essential actions
 * into a clean expandable quick-menu and automatically fades away while interacting
 * or playing on the board.
 */
@Composable
fun QuickControlFab(
    visible: Boolean,
    expanded: Boolean,
    status: GameStatus,
    palette: MaterialPalette,
    onToggleExpand: () -> Unit,
    onPauseToggle: () -> Unit,
    onRestart: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.85f),
        exit = fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 0.85f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Expandable quick actions stack above the single bottom-right FAB
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 3 },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(140)) { it / 3 }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionPill(
                        icon = if (status == GameStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        label = when (status) {
                            GameStatus.RUNNING -> "Duraklat"
                            GameStatus.PAUSED -> "Devam Et"
                            else -> "Başla"
                        },
                        palette = palette,
                        testTag = "pause_resume_button",
                        onClick = onPauseToggle
                    )
                    QuickActionPill(
                        icon = Icons.Default.Refresh,
                        label = "Yeniden Başlat",
                        palette = palette,
                        testTag = "quick_restart_button",
                        onClick = onRestart
                    )
                    QuickActionPill(
                        icon = Icons.Default.Leaderboard,
                        label = "Skor Tablosu",
                        palette = palette,
                        testTag = "leaderboard_button",
                        onClick = onOpenLeaderboard
                    )
                    QuickActionPill(
                        icon = Icons.Default.Settings,
                        label = "Ayarlar & Renk",
                        palette = palette,
                        testTag = "settings_button",
                        onClick = onOpenSettings
                    )
                    QuickActionPill(
                        icon = Icons.Default.Info,
                        label = "Hakkında",
                        palette = palette,
                        testTag = "about_button",
                        onClick = onOpenAbout
                    )
                }
            }

            // Single bottom-right floating action button
            FloatingActionButton(
                onClick = onToggleExpand,
                shape = CircleShape,
                containerColor = palette.primary,
                contentColor = palette.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(54.dp)
                    .testTag("single_quick_control_fab")
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Tune,
                    contentDescription = if (expanded) "Menüyü Kapat" else "Oyun Menüsü",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    palette: MaterialPalette,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceContainer.copy(alpha = 0.96f))
            .border(1.dp, palette.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.primary),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = palette.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = palette.secondary
        )
    }
}
