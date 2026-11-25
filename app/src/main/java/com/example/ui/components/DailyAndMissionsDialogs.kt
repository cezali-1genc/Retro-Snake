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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserSettings
import com.example.i18n.LocalizedStrings
import com.example.model.DailyRewardsRepository
import com.example.model.MaterialPalette
import com.example.model.MissionDefinition
import com.example.model.MissionPeriod
import com.example.model.MissionsRepository

@Composable
fun DailyRewardsDialog(
    settings: UserSettings,
    strings: LocalizedStrings,
    palette: MaterialPalette,
    onClaim: () -> Int,
    onDismiss: () -> Unit
) {
    val canClaim = settings.canClaimDailyReward()
    val currentDayIndex = if (canClaim) {
        val isConsecutive = settings.dailyRewardLastEpochDay == UserSettings.currentEpochDay() - 1L
        if (isConsecutive) (settings.dailyRewardStreak % 7) else 0
    } else {
        ((settings.dailyRewardStreak - 1).coerceIn(0, 6))
    }

    var claimedBanner by remember { mutableStateOf<Int?>(null) }

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
                .border(1.5.dp, palette.foodGolden.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("daily_rewards_dialog")
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
                            .background(palette.foodGolden.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = strings.dailyRewards,
                            tint = palette.foodGolden,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.dailyRewards,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.primary
                        )
                        Text(
                            text = "Seri: ${settings.dailyRewardStreak} / 7 • 🪙 ${settings.coins}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.secondary
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = strings.close, tint = palette.secondary)
                }
            }

            if (claimedBanner != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.primary.copy(alpha = 0.18f))
                        .border(1.dp, palette.primary, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$claimedBanner 🪙 ${strings.claimed}!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = palette.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyRewardsRepository.SEVEN_DAY_REWARDS.forEachIndexed { index, tier ->
                    val isClaimedPast = if (canClaim) index < currentDayIndex else index <= currentDayIndex
                    val isToday = index == currentDayIndex
                    val highlightColor = if (isToday) palette.foodGolden else palette.outlineVariant

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isToday && canClaim) palette.foodGolden.copy(alpha = 0.16f)
                                else palette.surfaceContainerHigh
                            )
                            .border(
                                width = if (isToday) 1.5.dp else 1.dp,
                                color = highlightColor,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = tier.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tier.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = palette.secondary
                                )
                                Text(
                                    text = "+${tier.coins} ${strings.coinsLabel}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = palette.foodGolden
                                )
                            }
                        }

                        if (isClaimedPast) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = strings.claimed,
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.claimed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                            }
                        } else if (isToday && canClaim) {
                            Button(
                                onClick = {
                                    val coins = onClaim()
                                    if (coins > 0) claimedBanner = coins
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.foodGolden,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("claim_daily_reward_button")
                            ) {
                                Text(text = strings.claim, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = strings.close, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MissionsDialog(
    settings: UserSettings,
    strings: LocalizedStrings,
    palette: MaterialPalette,
    onClaimMission: (MissionDefinition) -> Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .widthIn(max = 420.dp)
                .heightIn(max = 620.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceContainer)
                .border(1.5.dp, palette.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("missions_dialog")
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
                            .background(palette.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = strings.missions,
                            tint = palette.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.missions,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.primary
                        )
                        Text(
                            text = "🪙 ${settings.coins} ${strings.coinsLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.foodGolden
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = strings.close, tint = palette.secondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📅 ${strings.statsDaily}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = palette.primary
                )
                MissionsRepository.ALL_MISSIONS.filter { it.period == MissionPeriod.DAILY }.forEach { m ->
                    MissionRowCard(m, settings, strings, palette, onClaimMission)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🗓️ ${strings.statsWeekly}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = palette.tertiary
                )
                MissionsRepository.ALL_MISSIONS.filter { it.period == MissionPeriod.WEEKLY }.forEach { m ->
                    MissionRowCard(m, settings, strings, palette, onClaimMission)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = strings.close, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MissionRowCard(
    mission: MissionDefinition,
    settings: UserSettings,
    strings: LocalizedStrings,
    palette: MaterialPalette,
    onClaimMission: (MissionDefinition) -> Int
) {
    val progress = settings.getMissionProgress(mission).coerceAtMost(mission.targetValue)
    val isCompleted = progress >= mission.targetValue
    val isClaimed = settings.claimedMissions.contains(mission.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceContainerHigh)
            .border(
                1.dp,
                if (isCompleted && !isClaimed) palette.foodGolden else palette.outlineVariant,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = mission.iconEmoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = mission.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = palette.secondary
                    )
                    Text(
                        text = mission.description,
                        fontSize = 11.sp,
                        color = palette.outline
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isClaimed) {
                Text(
                    text = "✓ ${strings.claimed}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
            } else if (isCompleted) {
                Button(
                    onClick = { onClaimMission(mission) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.foodGolden,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "+${mission.rewardCoins} 🪙",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Text(
                    text = "+${mission.rewardCoins} 🪙",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.foodGolden
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress.toFloat() / mission.targetValue.coerceAtLeast(1) },
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) palette.foodGolden else palette.primary,
                trackColor = palette.surfaceContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$progress/${mission.targetValue}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.secondary
            )
        }
    }
}
