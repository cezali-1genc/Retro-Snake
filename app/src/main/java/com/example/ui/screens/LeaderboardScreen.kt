package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HighScoreEntity
import com.example.game.SnakeViewModel
import com.example.i18n.AppLocalization
import com.example.model.GameMode
import com.example.model.MaterialPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LeaderboardTab {
    OVERALL,
    DAILY,
    WEEKLY,
    BY_MODE
}

@Composable
fun LeaderboardScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()
    val highScore by viewModel.currentHighScore.collectAsState()
    val strings = remember(settings.language) { AppLocalization.get(settings.language) }

    var selectedTab by remember { mutableStateOf(LeaderboardTab.OVERALL) }
    var selectedModeFilter by remember { mutableStateOf(GameMode.ENDLESS) }
    var showClearDialog by remember { mutableStateOf(false) }

    val nowMs = remember { System.currentTimeMillis() }
    val dayAgoMs = remember(nowMs) { nowMs - 86_400_000L }
    val weekAgoMs = remember(nowMs) { nowMs - 7L * 86_400_000L }

    val overallScores by viewModel.topScores.collectAsState()
    val dailyScores by remember(dayAgoMs) {
        viewModel.repository.getTopScoresSince(dayAgoMs, 50)
    }.collectAsState(initial = emptyList())
    val weeklyScores by remember(weekAgoMs) {
        viewModel.repository.getTopScoresSince(weekAgoMs, 50)
    }.collectAsState(initial = emptyList())
    val modeScores by remember(selectedModeFilter) {
        viewModel.repository.getTopScoresByMode(selectedModeFilter.id, 50)
    }.collectAsState(initial = emptyList())

    val displayedScores = when (selectedTab) {
        LeaderboardTab.OVERALL -> overallScores
        LeaderboardTab.DAILY -> dailyScores
        LeaderboardTab.WEEKLY -> weeklyScores
        LeaderboardTab.BY_MODE -> modeScores
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("leaderboard_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(palette.surfaceContainer, CircleShape)
                        .border(1.dp, palette.outlineVariant, CircleShape)
                        .testTag("leaderboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = palette.primary
                    )
                }

                Text(
                    text = strings.leaderboard,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = palette.primary
                )

                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(palette.surfaceContainer, CircleShape)
                        .border(1.dp, palette.outlineVariant, CircleShape)
                        .testTag("clear_leaderboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear",
                        tint = palette.foodRegular
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Lifetime Stats Card (Room & local progression)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LifetimeStatItem(strings.highScore, "${highScore ?: 0}", palette.foodGolden, palette)
                    LifetimeStatItem(strings.totalScore, "${settings.totalScore}", palette.primary, palette)
                    LifetimeStatItem(strings.gamesPlayed, "${settings.totalGames}", palette.tertiary, palette)
                    LifetimeStatItem(strings.foodEaten, "${settings.totalFoodsEaten}", palette.secondary, palette)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Tabs
            val tabs = listOf(
                LeaderboardTab.OVERALL to strings.statsOverall,
                LeaderboardTab.DAILY to strings.statsDaily,
                LeaderboardTab.WEEKLY to strings.statsWeekly,
                LeaderboardTab.BY_MODE to strings.statsByMode
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEach { (tab, label) ->
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) palette.primary else palette.surfaceContainer)
                            .border(
                                1.dp,
                                if (isSelected) palette.primary else palette.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) palette.onPrimary else palette.secondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Secondary GameMode chips when BY_MODE is selected
            if (selectedTab == LeaderboardTab.BY_MODE) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GameMode.entries) { mode ->
                        val isSelected = mode == selectedModeFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(mode.accentHex) else palette.surfaceContainerHigh)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(mode.accentHex) else palette.outlineVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedModeFilter = mode }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${mode.iconEmoji} ${mode.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else palette.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scores List
            if (displayedScores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = palette.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Henüz kayıtlı skor bulunmuyor.\nOyna ve ilk rekoru sen yaz!",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = palette.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(displayedScores) { index, item ->
                        ScoreItemCard(
                            rank = index + 1,
                            item = item,
                            palette = palette
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(text = "Skorları Sıfırla", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Tüm yerel skor kayıtları silinsin mi? Bu işlem geri alınamaz.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHighScores()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.foodRegular)
                ) {
                    Text("SİL", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("İPTAL")
                }
            }
        )
    }
}

@Composable
private fun LifetimeStatItem(
    label: String,
    value: String,
    accent: Color,
    palette: MaterialPalette
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = palette.outline)
        Text(
            text = value,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
    }
}

@Composable
private fun ScoreItemCard(
    rank: Int,
    item: HighScoreEntity,
    palette: MaterialPalette
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> palette.surfaceContainerHigh
    }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()) }
    val dateString = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }
    val mode = remember(item.gameMode) { GameMode.fromId(item.gameMode) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
        border = BorderStroke(1.dp, palette.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(rankBadgeColor, CircleShape)
                    .border(1.dp, Color.Black.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (rank <= 3) Color.Black else palette.secondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.playerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = palette.secondary
                )
                Text(
                    text = "${mode.iconEmoji} ${mode.title} • ${item.difficulty} • 🐍 ${item.snakeLength} • $dateString",
                    fontSize = 10.sp,
                    color = palette.outline
                )
            }

            Text(
                text = "${item.score}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = palette.primary
            )
        }
    }
}
