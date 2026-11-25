package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.SnakeViewModel
import com.example.model.GameStatus
import com.example.model.MaterialPalettes
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.components.DynamicTipBanner
import com.example.ui.components.MaterialPauseOverlay
import com.example.ui.components.QuickControlFab
import com.example.ui.components.RetroHUD
import com.example.ui.components.SnakeGameBoard
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: SnakeViewModel,
    onNavigateLeaderboard: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val highScore by viewModel.currentHighScore.collectAsState()
    val topScoresForGameOver by viewModel.topScoresForGameOver.collectAsState()
    val activeTip by viewModel.activeTip.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val palette = MaterialPalettes.getPalette(settings.materialTheme, settings.themeMode, isSystemDark)

    var showAboutDialog by remember { mutableStateOf(false) }
    var isBoardTouching by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var isFabTemporarilyRevealed by remember { mutableStateOf(false) }

    // Auto-hide the temporarily revealed FAB after 2.6s while running
    LaunchedEffect(isFabTemporarilyRevealed, state.status, isFabMenuExpanded) {
        if (isFabTemporarilyRevealed && state.status == GameStatus.RUNNING && !isFabMenuExpanded) {
            delay(2600L)
            isFabTemporarilyRevealed = false
        }
    }

    // Immediately hide FAB and collapse menu whenever user touches/swipes on screen
    LaunchedEffect(isBoardTouching) {
        if (isBoardTouching) {
            isFabMenuExpanded = false
            isFabTemporarilyRevealed = false
        }
    }

    // Collapse menu whenever game starts running
    LaunchedEffect(state.status) {
        if (state.status == GameStatus.RUNNING) {
            isFabMenuExpanded = false
            isFabTemporarilyRevealed = false
        }
    }

    val isFabVisible = when {
        state.status == GameStatus.PAUSED -> false
        showAboutDialog -> false
        isFabMenuExpanded -> true
        isBoardTouching -> false
        state.status == GameStatus.RUNNING -> isFabTemporarilyRevealed
        else -> true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.boardBackground)
    ) {
        // Layer 1: Full-bleed frameless edge-to-edge infinite game board
        SnakeGameBoard(
            state = state,
            palette = palette,
            snakeColorMode = settings.snakeColorMode,
            customSnakeColorArgb = settings.customSnakeColorArgb,
            enableSwipe = true,
            onDirectionChange = { dir ->
                viewModel.changeDirection(dir)
            },
            onTouchActivity = { touching ->
                isBoardTouching = touching
            },
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

        // Layer 2: Floating compact HUD + Dynamic Beginner Tip Banner at top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .widthIn(max = 600.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RetroHUD(
                state = state,
                highScore = highScore ?: 0,
                palette = palette
            )

            DynamicTipBanner(
                tip = if (settings.tipsEnabled) activeTip else null,
                palette = palette,
                onDismiss = { viewModel.dismissTip() },
                onNextTip = { viewModel.cycleNextTip() }
            )
        }

        // Layer 3: Ready Overlay Card
        if (state.status == GameStatus.READY) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surfaceContainer.copy(alpha = 0.94f))
                    .border(2.dp, palette.primary, RoundedCornerShape(22.dp))
                    .clickable { viewModel.startGame() }
                    .padding(20.dp)
                    .testTag("ready_overlay_tap"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 260.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_snake_logo),
                        contentDescription = "Snake Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, palette.outlineVariant, RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MATERIAL SNAKE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = palette.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "♾️ Sonsuz Alan • 🔥 Kombo • 💎 2X Fever",
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = palette.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "▶ BAŞLAMAK İÇİN DOKUN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = palette.secondary
                    )
                }
            }
        }

        // Layer 3b: Centered Slowly-Scaling Game Over Dialog
        if (state.status == GameStatus.GAME_OVER) {
            GameOverCard(
                state = state,
                highScore = highScore ?: 0,
                topScores = topScoresForGameOver,
                palette = palette,
                onRestart = { viewModel.restartGame() },
                onViewLeaderboard = onNavigateLeaderboard
            )
        }

        // Layer 4: Tap-outside scrim when quick menu is expanded
        if (isFabMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isFabMenuExpanded = false }
                    )
            )
        }

        // Single Floating Bottom-Right Control Button (disappears during active touch/play)
        QuickControlFab(
            visible = isFabVisible,
            expanded = isFabMenuExpanded,
            status = state.status,
            palette = palette,
            onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
            onPauseToggle = {
                isFabMenuExpanded = false
                when (state.status) {
                    GameStatus.RUNNING -> viewModel.pauseGame()
                    GameStatus.PAUSED -> viewModel.resumeGame()
                    GameStatus.READY -> viewModel.startGame()
                    GameStatus.GAME_OVER -> viewModel.restartGame()
                }
            },
            onRestart = {
                isFabMenuExpanded = false
                viewModel.restartGame()
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
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 18.dp)
        )

        // Semi-transparent Material Design Pause Overlay
        if (state.status == GameStatus.PAUSED) {
            MaterialPauseOverlay(
                state = state,
                palette = palette,
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartGame() },
                onSettings = onNavigateSettings,
                onAbout = { showAboutDialog = true }
            )
        }

        // About Developer Dialog (Serhat Güner)
        if (showAboutDialog) {
            AboutDialog(
                palette = palette,
                onDismiss = { showAboutDialog = false }
            )
        }

        // Optional scanlines overlay if user enabled it in settings
        if (settings.crtScanlinesEnabled) {
            CrtScanlineOverlay(
                scanlineColor = palette.outline.copy(alpha = 0.15f),
                enabled = true
            )
        }
    }
}
