package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.model.Difficulty
import com.example.model.MaterialPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val allScores by viewModel.topScores.collectAsState()
    var selectedFilter by remember { mutableStateOf("TÜMÜ") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("TÜMÜ", Difficulty.EASY.label, Difficulty.MEDIUM.label, Difficulty.HARD.label, Difficulty.INSANE.label)

    val filteredScores = remember(allScores, selectedFilter) {
        if (selectedFilter == "TÜMÜ") {
            allScores
        } else {
            allScores.filter { it.difficulty == selectedFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
            .testTag("leaderboard_screen")
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
                    .size(44.dp)
                    .background(palette.surfaceContainer, CircleShape)
                    .border(1.dp, palette.outlineVariant, CircleShape)
                    .testTag("leaderboard_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = palette.primary
                )
            }

            Text(
                text = "SKOR TABLOSU",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = palette.primary
            )

            IconButton(
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .background(palette.surfaceContainer, CircleShape)
                    .border(1.dp, palette.outlineVariant, CircleShape)
                    .testTag("clear_leaderboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Skorları Sıfırla",
                    tint = palette.foodRegular
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) palette.primary else palette.surfaceContainer)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) palette.primary else palette.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isSelected) palette.onPrimary else palette.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scores List
        if (filteredScores.isEmpty()) {
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
                itemsIndexed(filteredScores) { index, item ->
                    ScoreItemCard(
                        rank = index + 1,
                        item = item,
                        palette = palette
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Skorları Sıfırla",
                    fontWeight = FontWeight.Bold
                )
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

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
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

            // Player name and metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.playerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = palette.secondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.difficulty} • Boyut: ${item.snakeLength}",
                        fontSize = 11.sp,
                        color = palette.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $dateString",
                        fontSize = 10.sp,
                        color = palette.outline
                    )
                }
            }

            // Score
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
