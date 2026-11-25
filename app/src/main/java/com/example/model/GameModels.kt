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

enum class FoodType(val points: Int) {
    REGULAR(10),
    GOLDEN(35),
    SPEED_SLOW(15),
    BERRY(20),
    RAINBOW_GEM(50)
}

data class Food(
    val position: Point,
    val type: FoodType = FoodType.REGULAR,
    val spawnTime: Long = System.currentTimeMillis(),
    val durationMs: Long = 9000L
) {
    fun isExpired(): Boolean {
        if (type == FoodType.REGULAR) return false
        return System.currentTimeMillis() - spawnTime > durationMs
    }
}

enum class Difficulty(
    val id: String,
    val label: String,
    val initialTickMs: Long,
    val minTickMs: Long,
    val speedStepMs: Long,
    val scoreMultiplier: Float,
    val hasWalls: Boolean,
    val hasObstacles: Boolean
) {
    EASY(
        id = "EASY",
        label = "KOLAY",
        initialTickMs = 230L,
        minTickMs = 140L,
        speedStepMs = 3L,
        scoreMultiplier = 1.0f,
        hasWalls = false,
        hasObstacles = false
    ),
    MEDIUM(
        id = "MEDIUM",
        label = "ORTA",
        initialTickMs = 170L,
        minTickMs = 95L,
        speedStepMs = 4L,
        scoreMultiplier = 1.5f,
        hasWalls = false, // Infinite borderless wrap-around
        hasObstacles = false
    ),
    HARD(
        id = "HARD",
        label = "ZOR",
        initialTickMs = 120L,
        minTickMs = 60L,
        speedStepMs = 5L,
        scoreMultiplier = 2.0f,
        hasWalls = false, // Infinite borderless wrap-around with obstacles
        hasObstacles = true
    ),
    INSANE(
        id = "INSANE",
        label = "EKSTREM",
        initialTickMs = 80L,
        minTickMs = 38L,
        speedStepMs = 6L,
        scoreMultiplier = 3.0f,
        hasWalls = false,
        hasObstacles = true
    )
}

enum class ControlMode(val label: String) {
    SWIPE("Tam Ekran Kaydırma (Swipe)"),
    TOUCH_OVERLAY("Yüzen Dokunmatik Butonlar (Touch Pad)"),
    DPAD("Material D-Pad"),
    FOUR_BUTTONS("4-Yön Butonları")
}

enum class ThemeMode(val label: String) {
    SYSTEM("Sistem Varsayılanı"),
    LIGHT("Açık Tema"),
    DARK("Koyu Tema")
}

enum class MaterialThemeType(val label: String) {
    EMERALD("Material Emerald (Yeşil)"),
    OCEAN("Material Ocean (Mavi)"),
    SUNSET("Material Sunset (Turuncu)"),
    LAVENDER("Material Lavender (Mor)"),
    AMOLED("Material AMOLED (Siyah)")
}

data class MaterialPalette(
    val background: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val tertiary: Color,
    val outline: Color,
    val outlineVariant: Color,
    val boardBackground: Color,
    val gridLine: Color,
    val snakeHead: Color,
    val snakeBody: Color,
    val snakeEye: Color,
    val foodRegular: Color,
    val foodGolden: Color,
    val foodSlow: Color,
    val foodBerry: Color,
    val wallBorder: Color,
    val obstacle: Color,
    val isDark: Boolean
)

object VibrantSnakeColors {
    val RAINBOW = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444)  // Red
    )
}

enum class SnakeColorMode(val label: String) {
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
    palette: MaterialPalette
): Color {
    return when (mode) {
        SnakeColorMode.RAINBOW -> palette.snakeHead
        SnakeColorMode.THEME -> palette.snakeHead
        SnakeColorMode.CUSTOM -> Color(customColorArgb)
    }
}

fun resolveSnakeSegmentColor(
    mode: SnakeColorMode,
    customColorArgb: Int,
    palette: MaterialPalette,
    index: Int
): Color {
    return when (mode) {
        SnakeColorMode.RAINBOW -> {
            val rainbow = VibrantSnakeColors.RAINBOW
            rainbow[index % rainbow.size]
        }
        SnakeColorMode.THEME -> {
            if (index % 2 == 0) palette.snakeBody else androidx.compose.ui.graphics.lerp(palette.snakeBody, palette.primary, 0.28f)
        }
        SnakeColorMode.CUSTOM -> {
            val base = Color(customColorArgb)
            val shadeA = androidx.compose.ui.graphics.lerp(base, Color.Black, 0.14f)
            val shadeB = androidx.compose.ui.graphics.lerp(base, Color.White, 0.12f)
            if (index % 2 == 0) shadeA else shadeB
        }
    }
}

object MaterialPalettes {
    // 1. Emerald (Green / Nature)
    val EmeraldLight = MaterialPalette(
        background = Color(0xFFF6FBF3),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFE9F2E7),
        surfaceContainerHigh = Color(0xFFDEE9DC),
        primary = Color(0xFF1E6C3B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFA6F5B5),
        onPrimaryContainer = Color(0xFF00210C),
        secondary = Color(0xFF506352),
        tertiary = Color(0xFF3A656F),
        outline = Color(0xFF727970),
        outlineVariant = Color(0xFFC1C9BF),
        boardBackground = Color(0xFFEDF6EC),
        gridLine = Color(0xFFD4E2D2),
        snakeHead = Color(0xFF13572D),
        snakeBody = Color(0xFF2E854B),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFBA1A1A),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF9333EA),
        wallBorder = Color(0xFF1E6C3B),
        obstacle = Color(0xFF7D5260),
        isDark = false
    )

    val EmeraldDark = MaterialPalette(
        background = Color(0xFF0F1511),
        surface = Color(0xFF161E18),
        surfaceContainer = Color(0xFF1C271F),
        surfaceContainerHigh = Color(0xFF243228),
        primary = Color(0xFF8BD89B),
        onPrimary = Color(0xFF003919),
        primaryContainer = Color(0xFF005326),
        onPrimaryContainer = Color(0xFFA6F5B5),
        secondary = Color(0xFFB7CCB7),
        tertiary = Color(0xFFA2CED9),
        outline = Color(0xFF8B9389),
        outlineVariant = Color(0xFF424940),
        boardBackground = Color(0xFF121B14),
        gridLine = Color(0xFF1E2C22),
        snakeHead = Color(0xFF8BD89B),
        snakeBody = Color(0xFF52B76D),
        snakeEye = Color(0xFF0F1511),
        foodRegular = Color(0xFFFF5449),
        foodGolden = Color(0xFFFBBF24),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFC084FC),
        wallBorder = Color(0xFF8BD89B),
        obstacle = Color(0xFFE07A5F),
        isDark = true
    )

    // 2. Ocean (Deep Blue / Cyan)
    val OceanLight = MaterialPalette(
        background = Color(0xFFF7F9FF),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFEAF1FB),
        surfaceContainerHigh = Color(0xFFDFE8F6),
        primary = Color(0xFF1E5BB5),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E2FF),
        onPrimaryContainer = Color(0xFF001A41),
        secondary = Color(0xFF565E71),
        tertiary = Color(0xFF006874),
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        boardBackground = Color(0xFFEEF3FC),
        gridLine = Color(0xFFD8E2F3),
        snakeHead = Color(0xFF0B4699),
        snakeBody = Color(0xFF2B6FD3),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFE11D48),
        foodGolden = Color(0xFFEAB308),
        foodSlow = Color(0xFF06B6D4),
        foodBerry = Color(0xFFA855F7),
        wallBorder = Color(0xFF1E5BB5),
        obstacle = Color(0xFF825500),
        isDark = false
    )

    val OceanDark = MaterialPalette(
        background = Color(0xFF0E131F),
        surface = Color(0xFF141C2E),
        surfaceContainer = Color(0xFF1A243B),
        surfaceContainerHigh = Color(0xFF222F4D),
        primary = Color(0xFFAEC6FF),
        onPrimary = Color(0xFF002E6C),
        primaryContainer = Color(0xFF004499),
        onPrimaryContainer = Color(0xFFD8E2FF),
        secondary = Color(0xFFBEC6DC),
        tertiary = Color(0xFF4DD8EC),
        outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF44474F),
        boardBackground = Color(0xFF111827),
        gridLine = Color(0xFF1F293D),
        snakeHead = Color(0xFFAEC6FF),
        snakeBody = Color(0xFF6B9BFA),
        snakeEye = Color(0xFF0E131F),
        foodRegular = Color(0xFFFB7185),
        foodGolden = Color(0xFFFACC15),
        foodSlow = Color(0xFF22D3EE),
        foodBerry = Color(0xFFC084FC),
        wallBorder = Color(0xFFAEC6FF),
        obstacle = Color(0xFFFFB74D),
        isDark = true
    )

    // 3. Sunset (Warm Coral / Peach)
    val SunsetLight = MaterialPalette(
        background = Color(0xFFFFF8F6),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFFBECE6),
        surfaceContainerHigh = Color(0xFFF5E0D8),
        primary = Color(0xFF9C4124),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBD1),
        onPrimaryContainer = Color(0xFF3B0900),
        secondary = Color(0xFF77574E),
        tertiary = Color(0xFF6E5D2E),
        outline = Color(0xFF85736E),
        outlineVariant = Color(0xFFD8C2BC),
        boardBackground = Color(0xFFFDF0EA),
        gridLine = Color(0xFFEED7CF),
        snakeHead = Color(0xFF86341B),
        snakeBody = Color(0xFFB85332),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFF16A34A),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF9333EA),
        wallBorder = Color(0xFF9C4124),
        obstacle = Color(0xFF7B3F00),
        isDark = false
    )

    val SunsetDark = MaterialPalette(
        background = Color(0xFF1A110E),
        surface = Color(0xFF231814),
        surfaceContainer = Color(0xFF2E201B),
        surfaceContainerHigh = Color(0xFF3B2923),
        primary = Color(0xFFFFB5A0),
        onPrimary = Color(0xFF5F1500),
        primaryContainer = Color(0xFF7E2B10),
        onPrimaryContainer = Color(0xFFFFDBD1),
        secondary = Color(0xFFE7BDB2),
        tertiary = Color(0xFFDCC48C),
        outline = Color(0xFFA08C86),
        outlineVariant = Color(0xFF53433F),
        boardBackground = Color(0xFF1E1410),
        gridLine = Color(0xFF33231C),
        snakeHead = Color(0xFFFFB5A0),
        snakeBody = Color(0xFFE07A5F),
        snakeEye = Color(0xFF1A110E),
        foodRegular = Color(0xFF4ADE80),
        foodGolden = Color(0xFFFCD34D),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFE879F9),
        wallBorder = Color(0xFFFFB5A0),
        obstacle = Color(0xFFFFB74D),
        isDark = true
    )

    // 4. Lavender (Purple / Lilac)
    val LavenderLight = MaterialPalette(
        background = Color(0xFFFCF7FF),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFF3EBFA),
        surfaceContainerHigh = Color(0xFFEADDF3),
        primary = Color(0xFF704E98),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF0DBFF),
        onPrimaryContainer = Color(0xFF29054E),
        secondary = Color(0xFF655A6F),
        tertiary = Color(0xFF805158),
        outline = Color(0xFF7C7584),
        outlineVariant = Color(0xFFCDC4D4),
        boardBackground = Color(0xFFF5EDFC),
        gridLine = Color(0xFFE2D4EC),
        snakeHead = Color(0xFF5A3982),
        snakeBody = Color(0xFF8B64B7),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFE11D48),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF059669),
        wallBorder = Color(0xFF704E98),
        obstacle = Color(0xFF6D4C41),
        isDark = false
    )

    val LavenderDark = MaterialPalette(
        background = Color(0xFF16101D),
        surface = Color(0xFF1E1627),
        surfaceContainer = Color(0xFF271D33),
        surfaceContainerHigh = Color(0xFF322541),
        primary = Color(0xFFDCB8FF),
        onPrimary = Color(0xFF3F1D65),
        primaryContainer = Color(0xFF57357E),
        onPrimaryContainer = Color(0xFFF0DBFF),
        secondary = Color(0xFFCFC2DA),
        tertiary = Color(0xFFF2B7C0),
        outline = Color(0xFF968E9E),
        outlineVariant = Color(0xFF4B4452),
        boardBackground = Color(0xFF1A1322),
        gridLine = Color(0xFF2E223D),
        snakeHead = Color(0xFFDCB8FF),
        snakeBody = Color(0xFFA875E3),
        snakeEye = Color(0xFF16101D),
        foodRegular = Color(0xFFFB7185),
        foodGolden = Color(0xFFFDE047),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFF34D399),
        wallBorder = Color(0xFFDCB8FF),
        obstacle = Color(0xFFBCAAA4),
        isDark = true
    )

    // 5. AMOLED (True Black M3 with Neon Highlights)
    val AmoledDark = MaterialPalette(
        background = Color(0xFF000000),
        surface = Color(0xFF0C0C0C),
        surfaceContainer = Color(0xFF141414),
        surfaceContainerHigh = Color(0xFF202020),
        primary = Color(0xFF00E676),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF00381B),
        onPrimaryContainer = Color(0xFF00E676),
        secondary = Color(0xFF00B0FF),
        tertiary = Color(0xFFFFD600),
        outline = Color(0xFF484848),
        outlineVariant = Color(0xFF2C2C2C),
        boardBackground = Color(0xFF050505),
        gridLine = Color(0xFF1A1A1A),
        snakeHead = Color(0xFF00E676),
        snakeBody = Color(0xFF00B359),
        snakeEye = Color(0xFF000000),
        foodRegular = Color(0xFFFF1744),
        foodGolden = Color(0xFFFFEA00),
        foodSlow = Color(0xFF00E5FF),
        foodBerry = Color(0xFFE040FB),
        wallBorder = Color(0xFF00E676),
        obstacle = Color(0xFFFF9100),
        isDark = true
    )

    fun getPalette(
        type: MaterialThemeType,
        themeMode: ThemeMode = ThemeMode.SYSTEM,
        isSystemInDarkTheme: Boolean = true
    ): MaterialPalette {
        val effectiveDark = when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        return when (type) {
            MaterialThemeType.EMERALD -> if (effectiveDark) EmeraldDark else EmeraldLight
            MaterialThemeType.OCEAN -> if (effectiveDark) OceanDark else OceanLight
            MaterialThemeType.SUNSET -> if (effectiveDark) SunsetDark else SunsetLight
            MaterialThemeType.LAVENDER -> if (effectiveDark) LavenderDark else LavenderLight
            MaterialThemeType.AMOLED -> if (effectiveDark) AmoledDark else EmeraldLight
        }
    }
}

enum class GameStatus {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER
}

/**
 * Dynamic in-game tip item for beginners
 */
data class InGameTip(
    val id: String,
    val iconEmoji: String,
    val title: String,
    val message: String
)

object TipRepository {
    val ALL_TIPS = listOf(
        InGameTip(
            id = "infinite_board",
            iconEmoji = "♾️",
            title = "Sonsuz Alan",
            message = "Alan sonsuzdur! Duvarın bir tarafından çıkıp diğerinden girebilirsin."
        ),
        InGameTip(
            id = "speed_increase",
            iconEmoji = "⚡",
            title = "Dinamik Hız",
            message = "Yem topladıkça yılanın hızı ivmelenerek artar!"
        ),
        InGameTip(
            id = "golden_star",
            iconEmoji = "🌟",
            title = "Altın Yıldız",
            message = "Altın yıldız +35 ekstra puan verir, süresi bitmeden yakala!"
        ),
        InGameTip(
            id = "blue_potion",
            iconEmoji = "❄️",
            title = "Mavi İksir",
            message = "Mavi iksir hızı 6 saniye boyunca yavaşlatarak nefes aldırır!"
        ),
        InGameTip(
            id = "purple_berry",
            iconEmoji = "🍇",
            title = "Mor Böğürtlen",
            message = "Mor meyve +20 ekstra puan verir ve yılanı renklendirir!"
        ),
        InGameTip(
            id = "combo_streak",
            iconEmoji = "🔥",
            title = "Kombo Çarpanı",
            message = "Yemleri peş peşe hızlı topla, x5'e kadar kombo puanı kazan!"
        ),
        InGameTip(
            id = "rainbow_gem",
            iconEmoji = "💎",
            title = "Gökkuşağı Elması",
            message = "6 saniye boyunca 2X PUAN ve yemleri çeken manyetik alan açar!"
        ),
        InGameTip(
            id = "self_collision",
            iconEmoji = "🐍",
            title = "Kendi Kuyruğun",
            message = "Yılan büyüdükçe kendi gövdesine çarparsa oyun biter!"
        ),
        InGameTip(
            id = "swipe_controls",
            iconEmoji = "👆",
            title = "Tam Ekran Dokunmatik",
            message = "Ekranın herhangi bir yerine kaydırarak yılanı anında yönlendir!"
        )
    )
}
