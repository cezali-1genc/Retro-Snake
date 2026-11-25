package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.SnakeViewModel
import com.example.model.MaterialPalettes
import com.example.model.ThemeMode
import com.example.ui.screens.GameModesScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelsScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SkinsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN_MENU,
    GAME,
    GAME_MODES,
    LEVELS,
    SKINS,
    LEADERBOARD,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnakeRoot()
        }
    }
}

@Composable
fun SnakeRoot(
    viewModel: SnakeViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    val effectiveDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val layoutDir = if (settings.language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        MyApplicationTheme(darkTheme = effectiveDark) {
            SnakeApp(viewModel = viewModel, isDark = effectiveDark)
        }
    }
}

@Composable
fun SnakeApp(
    viewModel: SnakeViewModel,
    isDark: Boolean
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var returnScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    val settings by viewModel.settings.collectAsState()
    val palette = MaterialPalettes.getPalette(settings.materialTheme, settings.themeMode, isDark)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = palette.background
    ) { _ ->
        when (currentScreen) {
            AppScreen.MAIN_MENU -> {
                MainMenuScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onPlay = { currentScreen = AppScreen.GAME },
                    onContinue = { currentScreen = AppScreen.GAME },
                    onNavigateGameModes = { currentScreen = AppScreen.GAME_MODES },
                    onNavigateLevels = { currentScreen = AppScreen.LEVELS },
                    onNavigateSkins = {
                        returnScreen = AppScreen.MAIN_MENU
                        currentScreen = AppScreen.SKINS
                    },
                    onNavigateLeaderboard = {
                        returnScreen = AppScreen.MAIN_MENU
                        currentScreen = AppScreen.LEADERBOARD
                    },
                    onNavigateSettings = {
                        returnScreen = AppScreen.MAIN_MENU
                        currentScreen = AppScreen.SETTINGS
                    }
                )
            }
            AppScreen.GAME -> {
                GameScreen(
                    viewModel = viewModel,
                    onNavigateMainMenu = { currentScreen = AppScreen.MAIN_MENU },
                    onNavigateLeaderboard = {
                        returnScreen = AppScreen.GAME
                        currentScreen = AppScreen.LEADERBOARD
                    },
                    onNavigateSettings = {
                        returnScreen = AppScreen.GAME
                        currentScreen = AppScreen.SETTINGS
                    }
                )
            }
            AppScreen.GAME_MODES -> {
                GameModesScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onModeLaunched = { currentScreen = AppScreen.GAME },
                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                )
            }
            AppScreen.LEVELS -> {
                LevelsScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onLevelLaunched = { currentScreen = AppScreen.GAME },
                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                )
            }
            AppScreen.SKINS -> {
                SkinsScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onOpenColorSettings = {
                        returnScreen = AppScreen.SKINS
                        currentScreen = AppScreen.SETTINGS
                    },
                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                )
            }
            AppScreen.LEADERBOARD -> {
                LeaderboardScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onBack = { currentScreen = returnScreen }
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onBack = { currentScreen = returnScreen }
                )
            }
        }
    }
}
