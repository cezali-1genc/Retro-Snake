package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SnakeViewModel
import com.example.i18n.AppLocalization
import com.example.model.GameLevel
import com.example.model.GameMode
import com.example.model.LevelRepository
import com.example.model.LevelTier
import com.example.model.MaterialPalette

@Composable
fun LevelsScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onLevelLaunched: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()
    val strings = remember(settings.language) { AppLocalization.get(settings.language) }
    var selectedTier by remember { mutableStateOf(LevelTier.BEGINNER) }

    val tierLevels = remember(selectedTier) {
        LevelRepository.ALL_LEVELS.filter { it.tier == selectedTier }
    }
    val totalStars = settings.levelStars.values.sum()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("levels_screen"),
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
                        .testTag("levels_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = palette.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.levels,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = palette.primary
                    )
                    Text(
                        text = "${settings.completedLevelsCount}/100 • ★ $totalStars/300",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = palette.foodGolden
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Tier Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(LevelTier.entries) { tier ->
                    val isSelected = tier == selectedTier
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) palette.primary else palette.surfaceContainer)
                            .border(
                                1.dp,
                                if (isSelected) palette.primary else palette.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTier = tier
                                viewModel.soundManager.playButtonClick()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("level_tier_${tier.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tier.label} (${tier.range.first}-${tier.range.last})",
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) palette.onPrimary else palette.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tierLevels, key = { it.number }) { level ->
                    val unlocked = level.number <= settings.highestUnlockedLevel
                    val stars = settings.levelStars[level.number] ?: 0
                    LevelGridCard(
                        level = level,
                        unlocked = unlocked,
                        stars = stars,
                        palette = palette,
                        onClick = {
                            if (unlocked) {
                                viewModel.launchMode(GameMode.LEVELS, level.number)
                                onLevelLaunched()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelGridCard(
    level: GameLevel,
    unlocked: Boolean,
    stars: Int,
    palette: MaterialPalette,
    onClick: () -> Unit
) {
    val bgColor = when {
        stars > 0 -> palette.primaryContainer.copy(alpha = 0.45f)
        unlocked -> palette.surfaceContainer
        else -> palette.surfaceContainerHigh.copy(alpha = 0.6f)
    }
    val borderColor = when {
        stars > 0 -> palette.primary
        unlocked -> palette.outline
        else -> palette.outlineVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (unlocked) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                enabled = unlocked,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.primary),
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("level_card_${level.number}")
    ) {
        if (unlocked) {
            Text(
                text = "${level.number}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = palette.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "🎯 ${level.targetScore}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (s <= stars) palette.foodGolden else palette.outlineVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            val tags = buildString {
                if (level.hasWalls) append("🧱")
                if (level.hasPortals) append("🌀")
                if (level.movingObstacleCount > 0) append("⚡")
            }
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = tags, fontSize = 10.sp)
            }
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = palette.outline,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${level.number}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = palette.outline
            )
        }
    }
}
