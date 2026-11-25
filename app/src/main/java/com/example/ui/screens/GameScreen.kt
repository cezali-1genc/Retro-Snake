package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.SnakeViewModel
import com.example.i18n.AppLocalization
import com.example.model.AchievementRepository
import com.example.model.ControlMode
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.MaterialPalettes
import com.example.ui.components.AchievementUnlockBanner
import com.example.ui.components.AchievementsDialog
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.components.DynamicTipBanner
import com.example.ui.components.MaterialPauseOverlay
import com.example.ui.components.QuickControlFab
import com.example.ui.components.RetroControls
import com.example.ui.components.RetroHUD
import com.example.ui.components.SnakeGameBoard
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: SnakeViewModel,
    onNavigateMainMenu: () -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val highScore by viewModel.currentHighScore.collectAsState()
    val topScoresForGameOver by viewModel.topScoresForGameOver.collectAsState()
    val activeTip by viewModel.activeTip.collectAsState()
    val newlyUnlockedBadge by viewModel.newlyUnlockedBadge.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val palette = MaterialPalettes.getPalette(settings.materialTheme, settings.themeMode, isSystemDark)
    val strings = remember(settings.language) { AppLocalization.get(settings.language) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var isBoardTouching by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var isFabTemporarilyRevealed by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showAboutDialog -> showAboutDialog = false
            showAchievementsDialog -> showAchievementsDialog = false
            isFabMenuExpanded -> isFabMenuExpanded = false
            state.status == GameStatus.RUNNING -> viewModel.pauseGame()
            else -> onNavigateMainMenu()
        }
    }

    val totalBadges = AchievementRepository.ALL_ACHIEVEMENTS.size
    val unlockedBadgeCount = settings.unlockedAchievements.size

    LaunchedEffect(isFabTemporarilyRevealed, state.status, isFabMenuExpanded) {
        if (isFabTemporarilyRevealed && state.status == GameStatus.RUNNING && !isFabMenuExpanded) {
            delay(2600L)
            isFabTemporarilyRevealed = false
        }
    }

    LaunchedEffect(isBoardTouching) {
        if (isBoardTouching) {
            isFabMenuExpanded = false
            isFabTemporarilyRevealed = false
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == GameStatus.RUNNING) {
            isFabMenuExpanded = false
            isFabTemporarilyRevealed = false
        }
    }

    val isEndDialogShown = state.status == GameStatus.GAME_OVER || state.status == GameStatus.LEVEL_COMPLETE
    val isFabVisible = when {
        state.status == GameStatus.PAUSED -> false
        isEndDialogShown -> false
        showAboutDialog -> false
        showAchievementsDialog -> false
        isFabMenuExpanded -> true
        isBoardTouching -> false
        state.status == GameStatus.RUNNING -> isFabTemporarilyRevealed
        else -> true
    }

    val hasBottomControlDeck = settings.controlMode != ControlMode.SWIPE &&
        state.status != GameStatus.PAUSED &&
        !isEndDialogShown

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // SECTION 1: Dedicated Upper Score & Status Header (strictly separated from the playfield)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.surfaceContainer,
                            palette.surface
                        )
                    )
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            RetroHUD(
                state = state,
                highScore = highScore ?: 0,
                strings = strings,
                palette = palette,
                onPauseToggle = {
                    when (state.status) {
                        GameStatus.RUNNING -> viewModel.pauseGame()
                        GameStatus.PAUSED -> viewModel.resumeGame()
                        GameStatus.READY -> viewModel.startGame()
                        GameStatus.LEVEL_COMPLETE -> viewModel.startNextLevel()
                        GameStatus.GAME_OVER -> viewModel.restartGame()
                    }
                },
                onOpenAchievements = {
                    if (state.status == GameStatus.RUNNING) viewModel.pauseGame()
                    showAchievementsDialog = true
                },
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // Crisp Glowing Accent Divider separating Score Header from Playable Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            palette.outlineVariant.copy(alpha = 0.4f),
                            palette.primary,
                            palette.tertiary,
                            palette.primary,
                            palette.outlineVariant.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // SECTION 2: Dedicated Full Playable Arena below the Score Header
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(palette.boardBackground)
        ) {
            SnakeGameBoard(
                state = state,
                palette = palette,
                snakeColorMode = settings.snakeColorMode,
                customSnakeColorArgb = settings.customSnakeColorArgb,
                selectedSkinId = settings.selectedSkinId,
                lowPerformanceMode = settings.lowPerformanceMode,
                enableSwipe = true,
                onDirectionChange = { dir -> viewModel.changeDirection(dir) },
                onTouchActivity = { touching -> isBoardTouching = touching },
                onBoardTap = {
                    if (isFabMenuExpanded) {
                        isFabMenuExpanded = false
                    } else if (state.status == GameStatus.RUNNING) {
                        isFabTemporarilyRevealed = !isFabTemporarilyRevealed
                    }
                },
                onBoardSizeMeasured = { cols, rows ->
                    viewModel.updateBoardDimensions(cols, rows)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top-of-arena Floating Banners (Achievement Unlock & Dynamic Tips)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 600.dp)
                    .padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AchievementUnlockBanner(
                    badge = newlyUnlockedBadge,
                    palette = palette,
                    onDismiss = { viewModel.dismissAchievementBanner() }
                )

                DynamicTipBanner(
                    tip = if (settings.tipsEnabled) activeTip else null,
                    palette = palette,
                    onDismiss = { viewModel.dismissTip() },
                    onNextTip = { viewModel.cycleNextTip() }
                )
            }

            // Ready Overlay Card
            if (state.status == GameStatus.READY) {
                val pulseTransition = rememberInfiniteTransition(label = "ready_cta")
                val ctaScale by pulseTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "cta_scale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                        .shadow(18.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    palette.surfaceContainerHigh.copy(alpha = 0.96f),
                                    palette.surfaceContainer.copy(alpha = 0.96f)
                                )
                            )
                        )
                        .border(2.dp, palette.primary, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.widthIn(max = 270.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_snake_logo),
                            contentDescription = strings.appTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, palette.outlineVariant, RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val modeHeader = if (state.gameMode == GameMode.LEVELS) {
                            "${state.gameMode.iconEmoji} ${strings.level} ${state.currentLevel}"
                        } else {
                            "${state.gameMode.iconEmoji} ${state.gameMode.title}"
                        }

                        Text(
                            text = modeHeader,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = palette.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (state.targetScore > 0) "🎯 ${state.targetScore} • ${state.difficulty.label}"
                            else state.gameMode.subtitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = palette.secondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(ctaScale)
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.primary)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = palette.onPrimary),
                                    onClick = { viewModel.startGame() }
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .testTag("ready_overlay_tap")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = strings.play,
                                tint = palette.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.tapToStart,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = palette.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.foodGolden.copy(alpha = 0.14f))
                                .border(1.dp, palette.foodGolden.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = palette.foodGolden),
                                    onClick = { showAchievementsDialog = true }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("ready_achievements_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = strings.achievements,
                                tint = palette.foodGolden,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.achievements}: $unlockedBadgeCount / $totalBadges",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.foodGolden
                            )
                        }
                    }
                }
            }

            // Centered Slowly-Scaling Game Over / Level Complete Dialog
            if (isEndDialogShown) {
                GameOverCard(
                    state = state,
                    highScore = highScore ?: 0,
                    topScores = topScoresForGameOver,
                    strings = strings,
                    palette = palette,
                    onRestart = { viewModel.restartGame() },
                    onNextLevel = { viewModel.startNextLevel() },
                    onMainMenu = onNavigateMainMenu
                )
            }

            // Tap-outside scrim when quick menu is expanded
            if (isFabMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isFabMenuExpanded = false }
                        )
                )
            }

            // Single Floating Bottom-Right Control Button
            QuickControlFab(
                visible = isFabVisible,
                expanded = isFabMenuExpanded,
                status = state.status,
                palette = palette,
                unlockedBadgeCount = unlockedBadgeCount,
                totalBadgeCount = totalBadges,
                onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                onPauseToggle = {
                    isFabMenuExpanded = false
                    when (state.status) {
                        GameStatus.RUNNING -> viewModel.pauseGame()
                        GameStatus.PAUSED -> viewModel.resumeGame()
                        GameStatus.READY -> viewModel.startGame()
                        GameStatus.LEVEL_COMPLETE -> viewModel.startNextLevel()
                        GameStatus.GAME_OVER -> viewModel.restartGame()
                    }
                },
                onRestart = {
                    isFabMenuExpanded = false
                    viewModel.restartGame()
                },
                onOpenAchievements = {
                    isFabMenuExpanded = false
                    if (state.status == GameStatus.RUNNING) viewModel.pauseGame()
                    showAchievementsDialog = true
                },
                onOpenLeaderboard = {
                    isFabMenuExpanded = false
                    if (state.status == GameStatus.RUNNING) viewModel.pauseGame()
                    onNavigateLeaderboard()
                },
                onOpenSettings = {
                    isFabMenuExpanded = false
                    if (state.status == GameStatus.RUNNING) viewModel.pauseGame()
                    onNavigateSettings()
                },
                onOpenAbout = {
                    isFabMenuExpanded = false
                    if (state.status == GameStatus.RUNNING) viewModel.pauseGame()
                    showAboutDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .let { if (!hasBottomControlDeck) it.navigationBarsPadding() else it }
                    .padding(end = 18.dp, bottom = 18.dp)
            )

            // Semi-transparent Material Design Pause Overlay
            if (state.status == GameStatus.PAUSED) {
                MaterialPauseOverlay(
                    state = state,
                    strings = strings,
                    palette = palette,
                    onResume = { viewModel.resumeGame() },
                    onRestart = { viewModel.restartGame() },
                    onSettings = onNavigateSettings,
                    onMainMenu = onNavigateMainMenu
                )
            }

            if (showAchievementsDialog) {
                AchievementsDialog(
                    settings = settings,
                    currentScore = state.score,
                    highScore = highScore ?: 0,
                    palette = palette,
                    onDismiss = { showAchievementsDialog = false }
                )
            }

            if (showAboutDialog) {
                AboutDialog(
                    palette = palette,
                    onDismiss = { showAboutDialog = false }
                )
            }

            if (settings.crtScanlinesEnabled) {
                CrtScanlineOverlay(
                    scanlineColor = palette.outline.copy(alpha = 0.15f),
                    enabled = true
                )
            }
        }

        // SECTION 3: Dedicated Bottom Control Deck when D-Pad / 4-Buttons / Virtual Joystick is active
        if (hasBottomControlDeck) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surfaceContainer)
                    .border(1.dp, palette.outlineVariant)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                RetroControls(
                    controlMode = settings.controlMode,
                    palette = palette,
                    onDirectionClick = { dir -> viewModel.changeDirection(dir) },
                    modifier = Modifier.widthIn(max = 520.dp)
                )
            }
        }
    }
}
