package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.game.SnakeViewModel
import com.example.i18n.AppLocalization
import com.example.model.AchievementRepository
import com.example.model.GameMode
import com.example.model.MaterialPalette
import com.example.model.MissionsRepository
import com.example.model.SnakeSkinRepository
import com.example.ui.components.AchievementsDialog
import com.example.ui.components.DailyRewardsDialog
import com.example.ui.components.MissionsDialog

@Composable
fun MainMenuScreen(
    viewModel: SnakeViewModel,
    palette: MaterialPalette,
    onPlay: () -> Unit,
    onContinue: () -> Unit,
    onNavigateGameModes: () -> Unit,
    onNavigateLevels: () -> Unit,
    onNavigateSkins: () -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val highScore by viewModel.currentHighScore.collectAsState()
    val strings = remember(settings.language) { AppLocalization.get(settings.language) }

    var showDailyRewards by remember { mutableStateOf(false) }
    var showMissions by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val canClaimDaily = settings.canClaimDailyReward()
    val claimableMissionsCount = remember(settings) {
        MissionsRepository.ALL_MISSIONS.count { m ->
            !settings.claimedMissions.contains(m.id) && settings.getMissionProgress(m) >= m.targetValue
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "main_play_pulse")
    val playScale by pulseTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "play_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.background,
                        palette.surface,
                        palette.boardBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("main_menu_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Bar: Logo Text + Version + Coins
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = strings.appTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "${strings.subtitle} • v${BuildConfig.VERSION_NAME}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.tertiary,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Coin balance chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.foodGolden.copy(alpha = 0.16f))
                            .border(1.dp, palette.foodGolden.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .clickable { showDailyRewards = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("menu_coins_chip")
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${settings.coins}",
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = palette.foodGolden
                        )
                    }
                }
            }

            // Hero Banner Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surfaceContainer),
                border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(126.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, palette.outlineVariant, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_snake_logo),
                            contentDescription = strings.appTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val activeSkin = remember(settings.selectedSkinId) {
                                SnakeSkinRepository.getById(settings.selectedSkinId)
                            }
                            Text(
                                text = "${settings.selectedGameMode.iconEmoji} ${settings.selectedGameMode.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.primary.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                            Text(
                                text = "🐍 ${activeSkin.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Lifetime Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroStatBox(
                            label = strings.highScore,
                            value = "${highScore ?: 0}",
                            accent = palette.foodGolden,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatBox(
                            label = strings.totalScore,
                            value = "${settings.totalScore}",
                            accent = palette.primary,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatBox(
                            label = strings.gamesPlayed,
                            value = "${settings.totalGames}",
                            accent = palette.tertiary,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatBox(
                            label = strings.achievements,
                            value = "${settings.unlockedAchievements.size}/${AchievementRepository.ALL_ACHIEVEMENTS.size}",
                            accent = palette.secondary,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Primary PLAY Button (Animated)
            Button(
                onClick = {
                    viewModel.launchMode(settings.selectedGameMode, settings.selectedLevel)
                    onPlay()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.onPrimary
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(playScale)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
                    .testTag("menu_play_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = strings.play,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.play,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            // CONTINUE Button (resumes active game or launches highest unlocked level)
            val continueLabel = if (settings.selectedGameMode == GameMode.LEVELS) {
                "${strings.continueGame} (${strings.level} ${settings.highestUnlockedLevel})"
            } else {
                "${strings.continueGame} (${settings.selectedGameMode.title})"
            }
            FilledTonalButton(
                onClick = {
                    if (settings.selectedGameMode == GameMode.LEVELS) {
                        viewModel.launchMode(GameMode.LEVELS, settings.highestUnlockedLevel)
                    }
                    onContinue()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = palette.surfaceContainerHigh,
                    contentColor = palette.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, palette.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("menu_continue_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = continueLabel,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = continueLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 2-Column Feature Grid (Every single item is functional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuTileButton(
                    icon = Icons.Default.SportsEsports,
                    title = strings.gameModes,
                    subtitle = "${GameMode.PLAYABLE_MODES.size} Mod",
                    accent = Color(0xFF06B6D4),
                    palette = palette,
                    testTag = "menu_game_modes_button",
                    onClick = onNavigateGameModes,
                    modifier = Modifier.weight(1f)
                )
                MenuTileButton(
                    icon = Icons.Default.Map,
                    title = strings.levels,
                    subtitle = "${settings.completedLevelsCount}/100 • ★${settings.levelStars.values.sum()}",
                    accent = Color(0xFF3B82F6),
                    palette = palette,
                    testTag = "menu_levels_button",
                    onClick = onNavigateLevels,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuTileButton(
                    icon = Icons.Default.Palette,
                    title = strings.snakeSkins,
                    subtitle = "${settings.unlockedSkins.size}/${SnakeSkinRepository.ALL_SKINS.size} Skin",
                    accent = Color(0xFFA855F7),
                    palette = palette,
                    testTag = "menu_skins_button",
                    onClick = onNavigateSkins,
                    modifier = Modifier.weight(1f)
                )
                MenuTileButton(
                    icon = Icons.Default.CardGiftcard,
                    title = strings.dailyRewards,
                    subtitle = if (canClaimDaily) "🎁 ${strings.claim}!" else "${settings.dailyRewardStreak}/7",
                    accent = palette.foodGolden,
                    hasBadgeDot = canClaimDaily,
                    palette = palette,
                    testTag = "menu_daily_rewards_button",
                    onClick = { showDailyRewards = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuTileButton(
                    icon = Icons.Default.TaskAlt,
                    title = strings.missions,
                    subtitle = if (claimableMissionsCount > 0) "✨ $claimableMissionsCount ${strings.claim}" else "${MissionsRepository.ALL_MISSIONS.size}",
                    accent = Color(0xFF10B981),
                    hasBadgeDot = claimableMissionsCount > 0,
                    palette = palette,
                    testTag = "menu_missions_button",
                    onClick = { showMissions = true },
                    modifier = Modifier.weight(1f)
                )
                MenuTileButton(
                    icon = Icons.Default.Leaderboard,
                    title = strings.leaderboard,
                    subtitle = "${strings.highScore}: ${highScore ?: 0}",
                    accent = Color(0xFFF97316),
                    palette = palette,
                    testTag = "menu_leaderboard_button",
                    onClick = onNavigateLeaderboard,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuTileButton(
                    icon = Icons.Default.EmojiEvents,
                    title = strings.achievements,
                    subtitle = "${settings.unlockedAchievements.size}/${AchievementRepository.ALL_ACHIEVEMENTS.size}",
                    accent = palette.foodGolden,
                    palette = palette,
                    testTag = "menu_achievements_button",
                    onClick = { showAchievements = true },
                    modifier = Modifier.weight(1f)
                )
                MenuTileButton(
                    icon = Icons.Default.Settings,
                    title = strings.settings,
                    subtitle = "${settings.language.flag} ${settings.materialTheme.label}",
                    accent = palette.primary,
                    palette = palette,
                    testTag = "menu_settings_button",
                    onClick = onNavigateSettings,
                    modifier = Modifier.weight(1f)
                )
            }

            // About button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surfaceContainer)
                    .border(1.dp, palette.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = palette.primary),
                        onClick = { showAbout = true }
                    )
                    .padding(vertical = 12.dp, horizontal = 14.dp)
                    .testTag("menu_about_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = strings.about,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${strings.about} • CEZALI 1GENÇ [S-G] ꪜ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.secondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showDailyRewards) {
        DailyRewardsDialog(
            settings = settings,
            strings = strings,
            palette = palette,
            onClaim = {
                val coins = viewModel.settingsManager.claimDailyReward()
                if (coins > 0) viewModel.soundManager.playBonus()
                coins
            },
            onDismiss = { showDailyRewards = false }
        )
    }

    if (showMissions) {
        MissionsDialog(
            settings = settings,
            strings = strings,
            palette = palette,
            onClaimMission = { mission ->
                val coins = viewModel.settingsManager.claimMission(mission)
                if (coins > 0) viewModel.soundManager.playFever()
                coins
            },
            onDismiss = { showMissions = false }
        )
    }

    if (showAchievements) {
        AchievementsDialog(
            settings = settings,
            currentScore = 0,
            highScore = highScore ?: 0,
            palette = palette,
            onDismiss = { showAchievements = false }
        )
    }

    if (showAbout) {
        AboutDialog(
            palette = palette,
            onDismiss = { showAbout = false }
        )
    }
}

@Composable
private fun HeroStatBox(
    label: String,
    value: String,
    accent: Color,
    palette: MaterialPalette,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceContainerHigh)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = palette.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = accent,
            maxLines = 1
        )
    }
}

@Composable
private fun MenuTileButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    palette: MaterialPalette,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasBadgeDot: Boolean = false
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceContainer)
            .border(1.dp, if (hasBadgeDot) accent else palette.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accent),
                onClick = onClick
            )
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (hasBadgeDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            )
        }
    }
}
