package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.SnakeViewModel
import com.example.model.MaterialPalettes
import com.example.model.ThemeMode
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    GAME,
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

    // Lifecycle-aware: Pause game immediately when app enters background to save battery
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

    MyApplicationTheme(darkTheme = effectiveDark) {
        SnakeApp(viewModel = viewModel, isDark = effectiveDark)
    }
}

@Composable
fun SnakeApp(
    viewModel: SnakeViewModel,
    isDark: Boolean
) {
    var currentScreen by remember { mutableStateOf(AppScreen.GAME) }
    val settings by viewModel.settings.collectAsState()
    val palette = MaterialPalettes.getPalette(settings.materialTheme, settings.themeMode, isDark)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = palette.background
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.GAME -> {
                GameScreen(
                    viewModel = viewModel,
                    onNavigateLeaderboard = { currentScreen = AppScreen.LEADERBOARD },
                    onNavigateSettings = { currentScreen = AppScreen.SETTINGS },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppScreen.LEADERBOARD -> {
                LeaderboardScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onBack = { currentScreen = AppScreen.GAME },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    palette = palette,
                    onBack = { currentScreen = AppScreen.GAME },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
