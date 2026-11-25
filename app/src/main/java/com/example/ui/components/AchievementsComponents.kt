package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserSettings
import com.example.model.AchievementBadge
import com.example.model.AchievementCategory
import com.example.model.AchievementRepository
import com.example.model.MaterialPalette

@Composable
fun AchievementUnlockBanner(
    badge: AchievementBadge?,
    palette: MaterialPalette,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = badge != null,
        enter = slideInVertically(tween(260)) { -it } + fadeIn(tween(220)),
        exit = slideOutVertically(tween(220)) { -it } + fadeOut(tween(180)),
        modifier = modifier
    ) {
        if (badge != null) {
            val accent = Color(badge.accentHex)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(palette.surfaceContainerHigh, palette.surfaceContainer)
                        )
                    )
                    .border(1.5.dp, accent, RoundedCornerShape(18.dp))
                    .clickable { onDismiss() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("achievement_unlock_banner")
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f))
                        .border(1.5.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = badge.iconEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROZET KAZANILDI! (+${badge.rewardCoins} 🪙)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = badge.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary
                    )
                    Text(
                        text = badge.description,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsDialog(
    settings: UserSettings,
    currentScore: Int,
    highScore: Int,
    palette: MaterialPalette,
    onDismiss: () -> Unit
) {
    val allBadges = AchievementRepository.ALL_ACHIEVEMENTS
    val unlockedIds = settings.unlockedAchievements
    val unlockedCount = allBadges.count { unlockedIds.contains(it.id) }
    val bestScore = maxOf(currentScore, highScore)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .widthIn(max = 420.dp)
                .heightIn(max = 600.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceContainer)
                .border(1.5.dp, palette.primary.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("achievements_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.foodGolden.copy(alpha = 0.18f))
                            .border(1.dp, palette.foodGolden.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Başarımlar",
                            tint = palette.foodGolden,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BAŞARIMLAR & ROZETLER",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.primary
                        )
                        Text(
                            text = "$unlockedCount / ${allBadges.size} Kazanıldı",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.secondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("achievements_close_icon")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = palette.secondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { unlockedCount.toFloat() / allBadges.size.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = palette.foodGolden,
                trackColor = palette.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                allBadges.forEach { badge ->
                    val currentValue = when (badge.category) {
                        AchievementCategory.FIRST_FOOD -> settings.totalFoodsEaten
                        AchievementCategory.SCORE -> bestScore
                        AchievementCategory.LEVELS -> settings.completedLevelsCount
                        AchievementCategory.SURVIVAL_TIME -> settings.totalSurvivalSeconds.toInt()
                        AchievementCategory.GOLDEN_COLLECTOR -> settings.goldenCollected
                        AchievementCategory.PERFECT_RUN -> if (unlockedIds.contains(badge.id)) 1 else 0
                    }
                    val isUnlocked = unlockedIds.contains(badge.id) || currentValue >= badge.requiredValue
                    val accent = Color(badge.accentHex)
                    val progress = (currentValue.toFloat() / badge.requiredValue.coerceAtLeast(1)).coerceIn(0f, 1f)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isUnlocked) accent.copy(alpha = 0.13f) else palette.surfaceContainerHigh)
                            .border(
                                width = if (isUnlocked) 1.5.dp else 1.dp,
                                color = if (isUnlocked) accent.copy(alpha = 0.7f) else palette.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isUnlocked) accent.copy(alpha = 0.24f) else palette.surfaceContainer)
                                .border(1.dp, if (isUnlocked) accent else palette.outlineVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = badge.iconEmoji, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = badge.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isUnlocked) palette.primary else palette.secondary
                                )
                                if (isUnlocked) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(accent.copy(alpha = 0.22f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Kazanıldı",
                                            tint = accent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "AÇILDI", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(palette.surfaceContainer)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Kilitli",
                                            tint = palette.outline,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "+${badge.rewardCoins} 🪙", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = palette.foodGolden)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = badge.description, fontSize = 12.sp, color = palette.outline)

                            if (!isUnlocked) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = accent,
                                    trackColor = palette.surfaceContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("achievements_close_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "KAPAT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
