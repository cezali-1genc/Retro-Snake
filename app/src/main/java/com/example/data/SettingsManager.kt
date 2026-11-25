package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ControlMode
import com.example.model.Difficulty
import com.example.model.MaterialThemeType
import com.example.model.SnakeColorMode
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val vibrationEnabled: Boolean = true,
    val crtScanlinesEnabled: Boolean = false,
    val materialTheme: MaterialThemeType = MaterialThemeType.EMERALD,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val controlMode: ControlMode = ControlMode.SWIPE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val tipsEnabled: Boolean = true,
    val fpsOverlayEnabled: Boolean = true,
    val playerName: String = "SNAKE_MASTER",
    val snakeColorMode: SnakeColorMode = SnakeColorMode.RAINBOW,
    val customSnakeColorArgb: Int = 0xFF10B981.toInt()
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("material_snake_settings_v3", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val sound = prefs.getBoolean("sound_enabled", true)
        val music = prefs.getBoolean("music_enabled", true)
        val volume = prefs.getFloat("sound_volume", 0.8f)
        val vibration = prefs.getBoolean("vibration_enabled", true)
        val crt = prefs.getBoolean("crt_scanlines", false)
        val themeStr = prefs.getString("material_theme", MaterialThemeType.EMERALD.name)
        val themeModeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        val controlStr = prefs.getString("control_mode", ControlMode.SWIPE.name)
        val diffStr = prefs.getString("difficulty", Difficulty.MEDIUM.name)
        val tips = prefs.getBoolean("tips_enabled", true)
        val fps = prefs.getBoolean("fps_overlay_enabled", true)
        val name = prefs.getString("player_name", "SNAKE_MASTER") ?: "SNAKE_MASTER"
        val snakeColorModeStr = prefs.getString("snake_color_mode", SnakeColorMode.RAINBOW.name)
        val customSnakeColor = prefs.getInt("custom_snake_color_argb", 0xFF10B981.toInt())

        val theme = runCatching { MaterialThemeType.valueOf(themeStr ?: "") }.getOrDefault(MaterialThemeType.EMERALD)
        val themeMode = runCatching { ThemeMode.valueOf(themeModeStr ?: "") }.getOrDefault(ThemeMode.SYSTEM)
        val control = runCatching { ControlMode.valueOf(controlStr ?: "") }.getOrDefault(ControlMode.SWIPE)
        val diff = runCatching { Difficulty.valueOf(diffStr ?: "") }.getOrDefault(Difficulty.MEDIUM)
        val snakeColorMode = runCatching { SnakeColorMode.valueOf(snakeColorModeStr ?: "") }.getOrDefault(SnakeColorMode.RAINBOW)

        return UserSettings(
            soundEnabled = sound,
            musicEnabled = music,
            soundVolume = volume.coerceIn(0f, 1f),
            vibrationEnabled = vibration,
            crtScanlinesEnabled = crt,
            materialTheme = theme,
            themeMode = themeMode,
            controlMode = control,
            difficulty = diff,
            tipsEnabled = tips,
            fpsOverlayEnabled = fps,
            playerName = name,
            snakeColorMode = snakeColorMode,
            customSnakeColorArgb = customSnakeColor
        )
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("music_enabled", enabled).apply()
        _settings.value = _settings.value.copy(musicEnabled = enabled)
    }

    fun setSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("sound_volume", clamped).apply()
        _settings.value = _settings.value.copy(soundVolume = clamped)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _settings.value = _settings.value.copy(vibrationEnabled = enabled)
    }

    fun setCrtScanlinesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("crt_scanlines", enabled).apply()
        _settings.value = _settings.value.copy(crtScanlinesEnabled = enabled)
    }

    fun setMaterialTheme(theme: MaterialThemeType) {
        prefs.edit().putString("material_theme", theme.name).apply()
        _settings.value = _settings.value.copy(materialTheme = theme)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setControlMode(control: ControlMode) {
        prefs.edit().putString("control_mode", control.name).apply()
        _settings.value = _settings.value.copy(controlMode = control)
    }

    fun setDifficulty(difficulty: Difficulty) {
        prefs.edit().putString("difficulty", difficulty.name).apply()
        _settings.value = _settings.value.copy(difficulty = difficulty)
    }

    fun setTipsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("tips_enabled", enabled).apply()
        _settings.value = _settings.value.copy(tipsEnabled = enabled)
    }

    fun setFpsOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("fps_overlay_enabled", enabled).apply()
        _settings.value = _settings.value.copy(fpsOverlayEnabled = enabled)
    }

    fun setPlayerName(name: String) {
        val sanitized = name.trim().take(16)
        prefs.edit().putString("player_name", sanitized).apply()
        _settings.value = _settings.value.copy(playerName = sanitized)
    }

    fun setSnakeColorMode(mode: SnakeColorMode) {
        prefs.edit().putString("snake_color_mode", mode.name).apply()
        _settings.value = _settings.value.copy(snakeColorMode = mode)
    }

    fun setCustomSnakeColor(colorArgb: Int) {
        prefs.edit()
            .putString("snake_color_mode", SnakeColorMode.CUSTOM.name)
            .putInt("custom_snake_color_argb", colorArgb)
            .apply()
        _settings.value = _settings.value.copy(
            snakeColorMode = SnakeColorMode.CUSTOM,
            customSnakeColorArgb = colorArgb
        )
    }
}
