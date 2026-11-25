package com.example.model

import androidx.compose.ui.graphics.Color

data class Point(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean {
        return (this == UP && other == DOWN) ||
            (this == DOWN && other == UP) ||
            (this == LEFT && other == RIGHT) ||
            (this == RIGHT && other == LEFT)
    }
}

enum class FoodType(val points: Int, val label: String) {
    REGULAR(10, "Elma"),
    GOLDEN(40, "Altın Elma"),
    DIAMOND(75, "Elmas"),
    ENERGY_ORB(25, "Enerji Küresi"),
    SHIELD(20, "Kalkan"),
    SPEED_SLOW(15, "Yavaşlatma"),
    MAGNET(20, "Mıknatıs"),
    DOUBLE_SCORE(25, "2X Puan"),
    MYSTERY_BOX(30, "Gizemli Kutu")
}

data class Food(
    val position: Point,
    val type: FoodType = FoodType.REGULAR,
    val spawnTime: Long = System.currentTimeMillis(),
    val durationMs: Long = 9500L
) {
    fun isExpired(): Boolean {
        if (type == FoodType.REGULAR) return false
        return System.currentTimeMillis() - spawnTime > durationMs
    }
}

data class MovingObstacle(
    val position: Point,
    val dx: Int,
    val dy: Int
)

enum class GameMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val hasSolidWalls: Boolean,
    val accentHex: Long
) {
    CLASSIC("CLASSIC", "Classic Mode", "Katı duvarlar ve saf klasik yılan deneyimi", "🐍", true, 0xFF10B981),
    ENDLESS("ENDLESS", "Endless Mode", "Sonsuz alan, artan hız ve x5 kombo zinciri", "♾️", false, 0xFF06B6D4),
    TIME_ATTACK("TIME_ATTACK", "Time Attack", "60 saniye içinde maksimum skoru topla!", "⏱️", false, 0xFFF59E0B),
    SURVIVAL("SURVIVAL", "Survival Mode", "3 can hakkı ile zorlu engellerde hayatta kal", "❤️", true, 0xFFEF4444),
    OBSTACLE("OBSTACLE", "Obstacle Mode", "Hareketli ve sabit labirent engellerini aş", "🧱", true, 0xFFF97316),
    PORTAL("PORTAL", "Portal Mode", "İki kuantum portal arasında ışınlanarak ilerle", "🌀", false, 0xFFA855F7),
    CHALLENGE("CHALLENGE", "Challenge Mode", "75 saniyede hedef skora ulaş ve altın kazan!", "🎯", true, 0xFFEC4899),
    LEVELS("LEVELS", "Seviye Modu", "100 benzersiz bölümde yıldızları topla", "🗺️", false, 0xFF3B82F6);

    companion object {
        val PLAYABLE_MODES = listOf(CLASSIC, ENDLESS, TIME_ATTACK, SURVIVAL, OBSTACLE, PORTAL, CHALLENGE)
        fun fromId(id: String?): GameMode = entries.firstOrNull { it.id == id } ?: ENDLESS
    }
}

enum class Difficulty(
    val id: String,
    val label: String,
    val initialTickMs: Long,
    val minTickMs: Long,
    val speedStepMs: Long,
    val scoreMultiplier: Float
) {
    EASY("EASY", "KOLAY", 220L, 130L, 3L, 1.0f),
    MEDIUM("MEDIUM", "ORTA", 165L, 90L, 4L, 1.5f),
    HARD("HARD", "ZOR", 120L, 62L, 5L, 2.0f),
    INSANE("INSANE", "EKSTREM", 85L, 42L, 6L, 3.0f)
}

enum class ControlMode(val label: String) {
    SWIPE("Kaydırma (Swipe)"),
    DPAD("Ekran D-Pad"),
    FOUR_BUTTONS("4-Yön Butonları"),
    JOYSTICK("Sanal Joystick");

    companion object {
        fun fromStored(name: String?): ControlMode {
            if (name == "TOUCH_OVERLAY") return FOUR_BUTTONS
            return entries.firstOrNull { it.name == name } ?: SWIPE
        }
    }
}

enum class ThemeMode(val label: String) {
    SYSTEM("Sistem"),
    LIGHT("Açık"),
    DARK("Koyu")
}

enum class MusicStyle(val label: String) {
    RETRO_ARCADE("Retro Arcade"),
    ELECTRONIC("Elektronik"),
    NEON_CYBER("Neon Cyber")
}

object VibrantSnakeColors {
    val RAINBOW = listOf(
        Color(0xFF10B981),
        Color(0xFF06B6D4),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFF59E0B),
        Color(0xFFEF4444)
    )
}

enum class SnakeColorMode(val label: String) {
    SKIN("Seçili Skin"),
    RAINBOW("Gökkuşağı"),
    THEME("Tema Rengi"),
    CUSTOM("Özel Renk")
}

data class SnakeColorPreset(
    val name: String,
    val colorArgb: Int
)

object SnakeColorPresets {
    val PRESETS = listOf(
        SnakeColorPreset("Zümrüt", 0xFF10B981.toInt()),
        SnakeColorPreset("Neon Yeşil", 0xFF22C55E.toInt()),
        SnakeColorPreset("Turkuaz", 0xFF06B6D4.toInt()),
        SnakeColorPreset("Kobalt", 0xFF3B82F6.toInt()),
        SnakeColorPreset("Ametist", 0xFF8B5CF6.toInt()),
        SnakeColorPreset("Neon Pembe", 0xFFEC4899.toInt()),
        SnakeColorPreset("Yakut", 0xFFEF4444.toInt()),
        SnakeColorPreset("Turuncu", 0xFFF97316.toInt()),
        SnakeColorPreset("Altın", 0xFFF59E0B.toInt()),
        SnakeColorPreset("Buz Beyazı", 0xFFE2E8F0.toInt())
    )
}

fun resolveSnakeHeadColor(
    mode: SnakeColorMode,
    customColorArgb: Int,
    palette: MaterialPalette,
    skin: SnakeSkin = SnakeSkinRepository.ALL_SKINS.first()
): Color = when (mode) {
    SnakeColorMode.SKIN -> skin.headColor
    SnakeColorMode.RAINBOW -> palette.snakeHead
    SnakeColorMode.THEME -> palette.snakeHead
    SnakeColorMode.CUSTOM -> Color(customColorArgb)
}

fun resolveSnakeSegmentColor(
    mode: SnakeColorMode,
    customColorArgb: Int,
    palette: MaterialPalette,
    index: Int,
    totalBody: Int = 10,
    skin: SnakeSkin = SnakeSkinRepository.ALL_SKINS.first()
): Color = when (mode) {
    SnakeColorMode.SKIN -> SnakeSkinRepository.segmentColor(skin, index, totalBody)
    SnakeColorMode.RAINBOW -> VibrantSnakeColors.RAINBOW[index % VibrantSnakeColors.RAINBOW.size]
    SnakeColorMode.THEME -> if (index % 2 == 0) palette.snakeBody else androidx.compose.ui.graphics.lerp(palette.snakeBody, palette.primary, 0.28f)
    SnakeColorMode.CUSTOM -> {
        val base = Color(customColorArgb)
        val shadeA = androidx.compose.ui.graphics.lerp(base, Color.Black, 0.14f)
        val shadeB = androidx.compose.ui.graphics.lerp(base, Color.White, 0.12f)
        if (index % 2 == 0) shadeA else shadeB
    }
}

enum class GameStatus {
    READY,
    RUNNING,
    PAUSED,
    LEVEL_COMPLETE,
    GAME_OVER
}

data class InGameTip(
    val id: String,
    val iconEmoji: String,
    val title: String,
    val message: String
)

object TipRepository {
    val ALL_TIPS = listOf(
        InGameTip("combo_streak", "🔥", "Kombo Çarpanı", "Yemleri peş peşe hızlı topla, x5'e kadar kombo puanı kazan!"),
        InGameTip("shield_power", "🛡️", "Kalkan Koruması", "Kalkan aktifken bir kez duvar veya engel çarpmasından kurtulursun!"),
        InGameTip("magnet_power", "🧲", "Mıknatıs Alanı", "Mıknatıs yakındaki yemleri otomatik olarak yılana doğru çeker!"),
        InGameTip("double_score", "💎", "2X Puan & Elmas", "2X Puan aktifken tüm puanlar ikiye katlanır; Elmas +5 Altın verir!"),
        InGameTip("slow_potion", "❄️", "Yavaşlatma", "Mavi iksir yılanı geçici olarak yavaşlatır ve manevra kolaylığı sağlar!"),
        InGameTip("mystery_box", "🎁", "Gizemli Kutu", "Sürpriz güçlendirme, ekstra süre veya bonus altın içerir!"),
        InGameTip("portal_jump", "🌀", "Portal Geçişi", "Portal Modunda bir portaldan girip diğerinden çıkabilirsin!")
    )
}

enum class AchievementCategory {
    FIRST_FOOD,
    SCORE,
    LEVELS,
    SURVIVAL_TIME,
    GOLDEN_COLLECTOR,
    PERFECT_RUN
}

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val requiredValue: Int,
    val category: AchievementCategory,
    val rewardCoins: Int,
    val accentHex: Long
)

object AchievementRepository {
    val ALL_ACHIEVEMENTS = listOf(
        AchievementBadge("first_food", "İlk Yemek", "İlk elmanı topla ve maceraya başla!", "🍎", 1, AchievementCategory.FIRST_FOOD, 20, 0xFFEF4444),
        AchievementBadge("yilan_avcisi", "Yılan Avcısı", "Tek oyunda 50 puana ulaş!", "🐍", 50, AchievementCategory.SCORE, 25, 0xFF10B981),
        AchievementBadge("retro_usta", "İlk 100 Puan", "Tek oyunda 100 puana ulaş!", "🕹️", 100, AchievementCategory.SCORE, 40, 0xFF3B82F6),
        AchievementBadge("altin_efendi", "Altın Efendi", "Tek oyunda 200 puana ulaş!", "🌟", 200, AchievementCategory.SCORE, 60, 0xFFF59E0B),
        AchievementBadge("score_1000", "1000 Puan Ustası", "Tek oyunda 1000 puana ulaş!", "🔥", 1000, AchievementCategory.SCORE, 120, 0xFFEC4899),
        AchievementBadge("score_5000", "5000 Puan Efsanesi", "Tek oyunda 5000 puana ulaş!", "👑", 5000, AchievementCategory.SCORE, 300, 0xFF8B5CF6),
        AchievementBadge("levels_10", "10 Seviye Tamamla", "Seviye modunda 10 bölümü tamamla!", "🗺️", 10, AchievementCategory.LEVELS, 80, 0xFF06B6D4),
        AchievementBadge("levels_50", "50 Seviye Fatihi", "Seviye modunda 50 bölümü tamamla!", "⚡", 50, AchievementCategory.LEVELS, 220, 0xFF3B82F6),
        AchievementBadge("levels_100", "100 Seviye Şampiyonu", "Tüm 100 seviyeyi tamamla!", "💎", 100, AchievementCategory.LEVELS, 500, 0xFF00E676),
        AchievementBadge("survival_10m", "10 Dakika Hayatta Kal", "Toplam 10 dakika boyunca oyna!", "⏱️", 600, AchievementCategory.SURVIVAL_TIME, 120, 0xFF14B8A6),
        AchievementBadge("golden_hunter", "Altın Avcısı", "Toplam 15 Altın Elma veya Elmas topla!", "🪙", 15, AchievementCategory.GOLDEN_COLLECTOR, 100, 0xFFFBBF24),
        AchievementBadge("perfect_run", "Kusursuz Oyun", "Bir seviyeyi hasar almadan tamamla!", "🛡️", 1, AchievementCategory.PERFECT_RUN, 90, 0xFFA855F7)
    )

    fun nextLockedScoreBadge(unlockedIds: Set<String>): AchievementBadge? =
        ALL_ACHIEVEMENTS.firstOrNull { it.category == AchievementCategory.SCORE && !unlockedIds.contains(it.id) }
}
