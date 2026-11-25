package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.i18n.AppLanguage
import com.example.model.ControlMode
import com.example.model.DailyRewardsRepository
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.MaterialThemeType
import com.example.model.MissionDefinition
import com.example.model.MissionPeriod
import com.example.model.MissionType
import com.example.model.MissionsRepository
import com.example.model.MusicStyle
import com.example.model.SnakeColorMode
import com.example.model.SnakeSkinRepository
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.65f,
    val musicStyle: MusicStyle = MusicStyle.RETRO_ARCADE,
    val vibrationEnabled: Boolean = true,
    val crtScanlinesEnabled: Boolean = false,
    val lowPerformanceMode: Boolean = false,
    val materialTheme: MaterialThemeType = MaterialThemeType.NEON_GREEN,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val controlMode: ControlMode = ControlMode.SWIPE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val selectedGameMode: GameMode = GameMode.ENDLESS,
    val selectedLevel: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val tipsEnabled: Boolean = true,
    val fpsOverlayEnabled: Boolean = false,
    val playerName: String = "SNAKE_MASTER",
    val language: AppLanguage = AppLanguage.TR,
    val snakeColorMode: SnakeColorMode = SnakeColorMode.SKIN,
    val customSnakeColorArgb: Int = 0xFF10B981.toInt(),
    val selectedSkinId: String = "neon",
    val unlockedSkins: Set<String> = setOf("classic", "neon"),
    val coins: Int = 50,
    val unlockedAchievements: Set<String> = emptySet(),
    val totalScore: Long = 0L,
    val totalGames: Int = 0,
    val totalFoodsEaten: Int = 0,
    val totalSurvivalSeconds: Long = 0L,
    val goldenCollected: Int = 0,
    val dailyRewardLastEpochDay: Long = 0L,
    val dailyRewardStreak: Int = 0,
    val dailyFoods: Int = 0,
    val dailyScore: Int = 0,
    val dailyBonuses: Int = 0,
    val dailyGames: Int = 0,
    val weeklyFoods: Int = 0,
    val weeklyScore: Int = 0,
    val weeklyLevels: Int = 0,
    val weeklyBonuses: Int = 0,
    val claimedMissions: Set<String> = emptySet()
) {
    val completedLevelsCount: Int get() = levelStars.size

    fun canClaimDailyReward(todayEpochDay: Long = currentEpochDay()): Boolean =
        dailyRewardLastEpochDay < todayEpochDay

    fun getMissionProgress(mission: MissionDefinition): Int = when (mission.period) {
        MissionPeriod.DAILY -> when (mission.type) {
            MissionType.EAT_FOODS -> dailyFoods
            MissionType.SCORE_POINTS -> dailyScore
            MissionType.COLLECT_BONUSES -> dailyBonuses
            MissionType.PLAY_GAMES -> dailyGames
            MissionType.COMPLETE_LEVELS -> 0
        }
        MissionPeriod.WEEKLY -> when (mission.type) {
            MissionType.EAT_FOODS -> weeklyFoods
            MissionType.SCORE_POINTS -> weeklyScore
            MissionType.COLLECT_BONUSES -> weeklyBonuses
            MissionType.COMPLETE_LEVELS -> weeklyLevels
            MissionType.PLAY_GAMES -> 0
        }
    }

    companion object {
        fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L
        fun currentEpochWeek(): Long = currentEpochDay() / 7L
    }
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("material_snake_settings_v3", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val today = UserSettings.currentEpochDay()
        val thisWeek = UserSettings.currentEpochWeek()

        val savedDay = prefs.getLong("mission_day_epoch", today)
        val savedWeek = prefs.getLong("mission_week_epoch", thisWeek)
        var claimedMissions = prefs.getStringSet("claimed_missions", emptySet())?.toMutableSet() ?: mutableSetOf()

        var dailyFoods = prefs.getInt("daily_foods", 0)
        var dailyScore = prefs.getInt("daily_score", 0)
        var dailyBonuses = prefs.getInt("daily_bonuses", 0)
        var dailyGames = prefs.getInt("daily_games", 0)
        if (savedDay != today) {
            dailyFoods = 0; dailyScore = 0; dailyBonuses = 0; dailyGames = 0
            claimedMissions.removeAll { id ->
                MissionsRepository.ALL_MISSIONS.any { it.id == id && it.period == MissionPeriod.DAILY }
            }
            prefs.edit()
                .putLong("mission_day_epoch", today)
                .putInt("daily_foods", 0)
                .putInt("daily_score", 0)
                .putInt("daily_bonuses", 0)
                .putInt("daily_games", 0)
                .putStringSet("claimed_missions", claimedMissions)
                .apply()
        }

        var weeklyFoods = prefs.getInt("weekly_foods", 0)
        var weeklyScore = prefs.getInt("weekly_score", 0)
        var weeklyLevels = prefs.getInt("weekly_levels", 0)
        var weeklyBonuses = prefs.getInt("weekly_bonuses", 0)
        if (savedWeek != thisWeek) {
            weeklyFoods = 0; weeklyScore = 0; weeklyLevels = 0; weeklyBonuses = 0
            claimedMissions.removeAll { id ->
                MissionsRepository.ALL_MISSIONS.any { it.id == id && it.period == MissionPeriod.WEEKLY }
            }
            prefs.edit()
                .putLong("mission_week_epoch", thisWeek)
                .putInt("weekly_foods", 0)
                .putInt("weekly_score", 0)
                .putInt("weekly_levels", 0)
                .putInt("weekly_bonuses", 0)
                .putStringSet("claimed_missions", claimedMissions)
                .apply()
        }

        val defaultUnlockedSkins = SnakeSkinRepository.ALL_SKINS.filter { it.isDefaultUnlocked }.map { it.id }.toSet()
        val storedSkins = prefs.getStringSet("unlocked_skins", defaultUnlockedSkins)?.toSet() ?: defaultUnlockedSkins
        val unlockedSkins = storedSkins + defaultUnlockedSkins

        val starsRaw = prefs.getString("level_stars_map", "") ?: ""
        val levelStars = parseLevelStars(starsRaw)

        val theme = MaterialThemeType.fromStored(prefs.getString("material_theme", MaterialThemeType.NEON_GREEN.name))
        val themeMode = runCatching { ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: "") }.getOrDefault(ThemeMode.DARK)
        val control = ControlMode.fromStored(prefs.getString("control_mode", ControlMode.SWIPE.name))
        val diff = runCatching { Difficulty.valueOf(prefs.getString("difficulty", Difficulty.MEDIUM.name) ?: "") }.getOrDefault(Difficulty.MEDIUM)
        val gameMode = GameMode.fromId(prefs.getString("selected_game_mode", GameMode.ENDLESS.id))
        val musicStyle = runCatching { MusicStyle.valueOf(prefs.getString("music_style", MusicStyle.RETRO_ARCADE.name) ?: "") }.getOrDefault(MusicStyle.RETRO_ARCADE)
        val lang = runCatching { AppLanguage.valueOf(prefs.getString("app_language", AppLanguage.TR.name) ?: "") }.getOrDefault(AppLanguage.TR)
        val snakeColorMode = runCatching { SnakeColorMode.valueOf(prefs.getString("snake_color_mode", SnakeColorMode.SKIN.name) ?: "") }.getOrDefault(SnakeColorMode.SKIN)

        return UserSettings(
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            musicEnabled = prefs.getBoolean("music_enabled", true),
            soundVolume = prefs.getFloat("sound_volume", 0.8f).coerceIn(0f, 1f),
            musicVolume = prefs.getFloat("music_volume", 0.65f).coerceIn(0f, 1f),
            musicStyle = musicStyle,
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            crtScanlinesEnabled = prefs.getBoolean("crt_scanlines", false),
            lowPerformanceMode = prefs.getBoolean("low_performance_mode", false),
            materialTheme = theme,
            themeMode = themeMode,
            controlMode = control,
            difficulty = diff,
            selectedGameMode = gameMode,
            selectedLevel = prefs.getInt("selected_level", 1).coerceIn(1, 100),
            highestUnlockedLevel = prefs.getInt("highest_unlocked_level", 1).coerceIn(1, 100),
            levelStars = levelStars,
            tipsEnabled = prefs.getBoolean("tips_enabled", true),
            fpsOverlayEnabled = prefs.getBoolean("fps_overlay_enabled", false),
            playerName = prefs.getString("player_name", "SNAKE_MASTER") ?: "SNAKE_MASTER",
            language = lang,
            snakeColorMode = snakeColorMode,
            customSnakeColorArgb = prefs.getInt("custom_snake_color_argb", 0xFF10B981.toInt()),
            selectedSkinId = prefs.getString("selected_skin_id", "neon") ?: "neon",
            unlockedSkins = unlockedSkins,
            coins = prefs.getInt("coins", 50).coerceAtLeast(0),
            unlockedAchievements = prefs.getStringSet("unlocked_achievements", emptySet())?.toSet() ?: emptySet(),
            totalScore = prefs.getLong("total_score", 0L),
            totalGames = prefs.getInt("total_games", 0),
            totalFoodsEaten = prefs.getInt("total_foods_eaten", 0),
            totalSurvivalSeconds = prefs.getLong("total_survival_seconds", 0L),
            goldenCollected = prefs.getInt("golden_collected", 0),
            dailyRewardLastEpochDay = prefs.getLong("daily_reward_last_day", 0L),
            dailyRewardStreak = prefs.getInt("daily_reward_streak", 0),
            dailyFoods = dailyFoods,
            dailyScore = dailyScore,
            dailyBonuses = dailyBonuses,
            dailyGames = dailyGames,
            weeklyFoods = weeklyFoods,
            weeklyScore = weeklyScore,
            weeklyLevels = weeklyLevels,
            weeklyBonuses = weeklyBonuses,
            claimedMissions = claimedMissions
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

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("music_volume", clamped).apply()
        _settings.value = _settings.value.copy(musicVolume = clamped)
    }

    fun setMusicStyle(style: MusicStyle) {
        prefs.edit().putString("music_style", style.name).apply()
        _settings.value = _settings.value.copy(musicStyle = style)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _settings.value = _settings.value.copy(vibrationEnabled = enabled)
    }

    fun setCrtScanlinesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("crt_scanlines", enabled).apply()
        _settings.value = _settings.value.copy(crtScanlinesEnabled = enabled)
    }

    fun setLowPerformanceMode(enabled: Boolean) {
        prefs.edit().putBoolean("low_performance_mode", enabled).apply()
        _settings.value = _settings.value.copy(lowPerformanceMode = enabled)
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

    fun setSelectedGameMode(mode: GameMode) {
        prefs.edit().putString("selected_game_mode", mode.id).apply()
        _settings.value = _settings.value.copy(selectedGameMode = mode)
    }

    fun setSelectedLevel(level: Int) {
        val safe = level.coerceIn(1, _settings.value.highestUnlockedLevel)
        prefs.edit().putInt("selected_level", safe).apply()
        _settings.value = _settings.value.copy(selectedLevel = safe)
    }

    fun setTipsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("tips_enabled", enabled).apply()
        _settings.value = _settings.value.copy(tipsEnabled = enabled)
    }

    fun setPlayerName(name: String) {
        val sanitized = name.trim().take(16)
        prefs.edit().putString("player_name", sanitized).apply()
        _settings.value = _settings.value.copy(playerName = sanitized)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.name).apply()
        _settings.value = _settings.value.copy(language = language)
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

    fun selectSkin(skinId: String) {
        if (!_settings.value.unlockedSkins.contains(skinId)) return
        prefs.edit()
            .putString("selected_skin_id", skinId)
            .putString("snake_color_mode", SnakeColorMode.SKIN.name)
            .apply()
        _settings.value = _settings.value.copy(
            selectedSkinId = skinId,
            snakeColorMode = SnakeColorMode.SKIN
        )
    }

    fun unlockAndSelectSkin(skinId: String, cost: Int): Boolean {
        val cur = _settings.value
        if (cur.unlockedSkins.contains(skinId)) {
            selectSkin(skinId)
            return true
        }
        if (cur.coins < cost) return false
        val newCoins = cur.coins - cost
        val newUnlocked = cur.unlockedSkins + skinId
        prefs.edit()
            .putInt("coins", newCoins)
            .putStringSet("unlocked_skins", newUnlocked)
            .putString("selected_skin_id", skinId)
            .putString("snake_color_mode", SnakeColorMode.SKIN.name)
            .apply()
        _settings.value = cur.copy(
            coins = newCoins,
            unlockedSkins = newUnlocked,
            selectedSkinId = skinId,
            snakeColorMode = SnakeColorMode.SKIN
        )
        return true
    }

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        val newCoins = _settings.value.coins + amount
        prefs.edit().putInt("coins", newCoins).apply()
        _settings.value = _settings.value.copy(coins = newCoins)
    }

    fun unlockAchievements(ids: Collection<String>, bonusCoins: Int = 0) {
        if (ids.isEmpty()) return
        val cur = _settings.value
        val actuallyNew = ids.filter { !cur.unlockedAchievements.contains(it) }
        if (actuallyNew.isEmpty()) return
        val updated = cur.unlockedAchievements + actuallyNew
        val newCoins = cur.coins + bonusCoins
        prefs.edit()
            .putStringSet("unlocked_achievements", updated)
            .putInt("coins", newCoins)
            .apply()
        _settings.value = cur.copy(
            unlockedAchievements = updated,
            coins = newCoins
        )
    }

    fun recordLevelCompletion(levelNumber: Int, stars: Int, rewardCoins: Int): Int {
        val cur = _settings.value
        val previousStars = cur.levelStars[levelNumber] ?: 0
        val earnedStars = stars.coerceIn(1, 3)
        val newMap = cur.levelStars.toMutableMap().apply {
            this[levelNumber] = maxOf(previousStars, earnedStars)
        }
        val newHighest = maxOf(cur.highestUnlockedLevel, (levelNumber + 1).coerceAtMost(100))
        // Prevent duplicate full coin farming: first clear gives full coins, replays give 20%
        val coinsAwarded = if (previousStars == 0) rewardCoins else (rewardCoins / 5).coerceAtLeast(3)
        val newCoins = cur.coins + coinsAwarded
        val newWeeklyLevels = cur.weeklyLevels + if (previousStars == 0) 1 else 0

        prefs.edit()
            .putString("level_stars_map", serializeLevelStars(newMap))
            .putInt("highest_unlocked_level", newHighest)
            .putInt("coins", newCoins)
            .putInt("weekly_levels", newWeeklyLevels)
            .apply()

        _settings.value = cur.copy(
            levelStars = newMap,
            highestUnlockedLevel = newHighest,
            coins = newCoins,
            weeklyLevels = newWeeklyLevels
        )
        return coinsAwarded
    }

    fun recordRunStats(
        score: Int,
        foodsEaten: Int,
        bonusesCollected: Int,
        goldenCollectedCount: Int,
        survivalSeconds: Long
    ) {
        val cur = _settings.value
        val today = UserSettings.currentEpochDay()
        val thisWeek = UserSettings.currentEpochWeek()

        val newTotalScore = cur.totalScore + score
        val newTotalGames = cur.totalGames + 1
        val newTotalFoods = cur.totalFoodsEaten + foodsEaten
        val newTotalSurvival = cur.totalSurvivalSeconds + survivalSeconds
        val newGolden = cur.goldenCollected + goldenCollectedCount

        val newDailyFoods = cur.dailyFoods + foodsEaten
        val newDailyScore = cur.dailyScore + score
        val newDailyBonuses = cur.dailyBonuses + bonusesCollected
        val newDailyGames = cur.dailyGames + 1

        val newWeeklyFoods = cur.weeklyFoods + foodsEaten
        val newWeeklyScore = cur.weeklyScore + score
        val newWeeklyBonuses = cur.weeklyBonuses + bonusesCollected

        prefs.edit()
            .putLong("mission_day_epoch", today)
            .putLong("mission_week_epoch", thisWeek)
            .putLong("total_score", newTotalScore)
            .putInt("total_games", newTotalGames)
            .putInt("total_foods_eaten", newTotalFoods)
            .putLong("total_survival_seconds", newTotalSurvival)
            .putInt("golden_collected", newGolden)
            .putInt("daily_foods", newDailyFoods)
            .putInt("daily_score", newDailyScore)
            .putInt("daily_bonuses", newDailyBonuses)
            .putInt("daily_games", newDailyGames)
            .putInt("weekly_foods", newWeeklyFoods)
            .putInt("weekly_score", newWeeklyScore)
            .putInt("weekly_bonuses", newWeeklyBonuses)
            .apply()

        _settings.value = cur.copy(
            totalScore = newTotalScore,
            totalGames = newTotalGames,
            totalFoodsEaten = newTotalFoods,
            totalSurvivalSeconds = newTotalSurvival,
            goldenCollected = newGolden,
            dailyFoods = newDailyFoods,
            dailyScore = newDailyScore,
            dailyBonuses = newDailyBonuses,
            dailyGames = newDailyGames,
            weeklyFoods = newWeeklyFoods,
            weeklyScore = newWeeklyScore,
            weeklyBonuses = newWeeklyBonuses
        )
    }

    fun claimDailyReward(): Int {
        val cur = _settings.value
        val today = UserSettings.currentEpochDay()
        if (!cur.canClaimDailyReward(today)) return 0

        val isConsecutive = cur.dailyRewardLastEpochDay == today - 1L
        val nextStreak = if (isConsecutive) ((cur.dailyRewardStreak % 7) + 1) else 1
        val tier = DailyRewardsRepository.SEVEN_DAY_REWARDS.getOrElse(nextStreak - 1) {
            DailyRewardsRepository.SEVEN_DAY_REWARDS.first()
        }
        val newCoins = cur.coins + tier.coins

        prefs.edit()
            .putLong("daily_reward_last_day", today)
            .putInt("daily_reward_streak", nextStreak)
            .putInt("coins", newCoins)
            .apply()

        _settings.value = cur.copy(
            dailyRewardLastEpochDay = today,
            dailyRewardStreak = nextStreak,
            coins = newCoins
        )
        return tier.coins
    }

    fun claimMission(mission: MissionDefinition): Int {
        val cur = _settings.value
        if (cur.claimedMissions.contains(mission.id)) return 0
        if (cur.getMissionProgress(mission) < mission.targetValue) return 0

        val updatedClaimed = cur.claimedMissions + mission.id
        val newCoins = cur.coins + mission.rewardCoins
        prefs.edit()
            .putStringSet("claimed_missions", updatedClaimed)
            .putInt("coins", newCoins)
            .apply()
        _settings.value = cur.copy(
            claimedMissions = updatedClaimed,
            coins = newCoins
        )
        return mission.rewardCoins
    }

    private fun parseLevelStars(raw: String): Map<Int, Int> {
        if (raw.isBlank()) return emptyMap()
        val map = mutableMapOf<Int, Int>()
        raw.split(";").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val lvl = parts[0].toIntOrNull()
                val stars = parts[1].toIntOrNull()
                if (lvl != null && stars != null) {
                    map[lvl] = stars.coerceIn(1, 3)
                }
            }
        }
        return map
    }

    private fun serializeLevelStars(map: Map<Int, Int>): String =
        map.entries.sortedBy { it.key }.joinToString(";") { "${it.key}:${it.value}" }
}
