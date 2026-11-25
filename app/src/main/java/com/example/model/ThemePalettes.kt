package com.example.model

import androidx.compose.ui.graphics.Color

enum class MaterialThemeType(val label: String) {
    NEON_GREEN("Neon Green"),
    CYBER_BLUE("Cyber Blue"),
    PURPLE_GALAXY("Purple Galaxy"),
    FIRE_RED("Fire Red"),
    GOLDEN_PREMIUM("Golden Premium"),
    CLASSIC_RETRO("Classic Retro"),
    DARK_AMOLED("Dark AMOLED");

    companion object {
        fun fromStored(name: String?): MaterialThemeType = when (name) {
            "EMERALD" -> NEON_GREEN
            "OCEAN" -> CYBER_BLUE
            "SUNSET" -> FIRE_RED
            "LAVENDER" -> PURPLE_GALAXY
            "AMOLED" -> DARK_AMOLED
            else -> entries.firstOrNull { it.name == name } ?: NEON_GREEN
        }
    }
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

object MaterialPalettes {
    private val NeonGreenDark = MaterialPalette(
        background = Color(0xFF09120D),
        surface = Color(0xFF101C15),
        surfaceContainer = Color(0xFF15261C),
        surfaceContainerHigh = Color(0xFF1D3326),
        primary = Color(0xFF00FF87),
        onPrimary = Color(0xFF002914),
        primaryContainer = Color(0xFF004D27),
        onPrimaryContainer = Color(0xFFA6FFCB),
        secondary = Color(0xFFD1FAE5),
        tertiary = Color(0xFF00E5FF),
        outline = Color(0xFF6EE7B7),
        outlineVariant = Color(0xFF284736),
        boardBackground = Color(0xFF0B160F),
        gridLine = Color(0xFF1A3123),
        snakeHead = Color(0xFF00FF87),
        snakeBody = Color(0xFF10B981),
        snakeEye = Color(0xFF09120D),
        foodRegular = Color(0xFFFF4D6D),
        foodGolden = Color(0xFFFACC15),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFC084FC),
        wallBorder = Color(0xFF00FF87),
        obstacle = Color(0xFFFF7849),
        isDark = true
    )

    private val NeonGreenLight = MaterialPalette(
        background = Color(0xFFF3FBF6),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFE5F5EB),
        surfaceContainerHigh = Color(0xFFD5ECDE),
        primary = Color(0xFF008F4C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB3F8D0),
        onPrimaryContainer = Color(0xFF002110),
        secondary = Color(0xFF1E3A2B),
        tertiary = Color(0xFF0284C7),
        outline = Color(0xFF5D7A68),
        outlineVariant = Color(0xFFB5D1BE),
        boardBackground = Color(0xFFEAF7EF),
        gridLine = Color(0xFFCDE5D6),
        snakeHead = Color(0xFF007A40),
        snakeBody = Color(0xFF10B981),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFE11D48),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF9333EA),
        wallBorder = Color(0xFF008F4C),
        obstacle = Color(0xFF9A3412),
        isDark = false
    )

    private val CyberBlueDark = MaterialPalette(
        background = Color(0xFF07111E),
        surface = Color(0xFF0D1A2D),
        surfaceContainer = Color(0xFF13243D),
        surfaceContainerHigh = Color(0xFF1A3152),
        primary = Color(0xFF00E5FF),
        onPrimary = Color(0xFF002A33),
        primaryContainer = Color(0xFF004B59),
        onPrimaryContainer = Color(0xFFB8F6FF),
        secondary = Color(0xFFDCEBFF),
        tertiary = Color(0xFFFF2E93),
        outline = Color(0xFF6495C7),
        outlineVariant = Color(0xFF233B5E),
        boardBackground = Color(0xFF091526),
        gridLine = Color(0xFF162C47),
        snakeHead = Color(0xFF00E5FF),
        snakeBody = Color(0xFF3B82F6),
        snakeEye = Color(0xFF07111E),
        foodRegular = Color(0xFFFF2E93),
        foodGolden = Color(0xFFFACC15),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFA855F7),
        wallBorder = Color(0xFF00E5FF),
        obstacle = Color(0xFFF59E0B),
        isDark = true
    )

    private val CyberBlueLight = MaterialPalette(
        background = Color(0xFFF3F8FF),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFE5EFFD),
        surfaceContainerHigh = Color(0xFFD6E4FA),
        primary = Color(0xFF026AA7),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCDE9FF),
        onPrimaryContainer = Color(0xFF001E33),
        secondary = Color(0xFF1D3557),
        tertiary = Color(0xFFDB2777),
        outline = Color(0xFF5A7599),
        outlineVariant = Color(0xFFB6C8E2),
        boardBackground = Color(0xFFEBF3FE),
        gridLine = Color(0xFFD0E0F5),
        snakeHead = Color(0xFF0369A1),
        snakeBody = Color(0xFF2563EB),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFE11D48),
        foodGolden = Color(0xFFEAB308),
        foodSlow = Color(0xFF06B6D4),
        foodBerry = Color(0xFF9333EA),
        wallBorder = Color(0xFF026AA7),
        obstacle = Color(0xFFB45309),
        isDark = false
    )

    private val PurpleGalaxyDark = MaterialPalette(
        background = Color(0xFF110A1C),
        surface = Color(0xFF19102A),
        surfaceContainer = Color(0xFF221638),
        surfaceContainerHigh = Color(0xFF2D1E4A),
        primary = Color(0xFFD946EF),
        onPrimary = Color(0xFF2D0036),
        primaryContainer = Color(0xFF5B186B),
        onPrimaryContainer = Color(0xFFFCD6FF),
        secondary = Color(0xFFF3E8FF),
        tertiary = Color(0xFF22D3EE),
        outline = Color(0xFFA78BFA),
        outlineVariant = Color(0xFF3F2B63),
        boardBackground = Color(0xFF140D22),
        gridLine = Color(0xFF2B1D47),
        snakeHead = Color(0xFFE879F9),
        snakeBody = Color(0xFF8B5CF6),
        snakeEye = Color(0xFF110A1C),
        foodRegular = Color(0xFFFB7185),
        foodGolden = Color(0xFFFDE047),
        foodSlow = Color(0xFF22D3EE),
        foodBerry = Color(0xFF34D399),
        wallBorder = Color(0xFFD946EF),
        obstacle = Color(0xFFFF9F43),
        isDark = true
    )

    private val PurpleGalaxyLight = MaterialPalette(
        background = Color(0xFFFAF5FF),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFF2E8FC),
        surfaceContainerHigh = Color(0xFFE6D5F8),
        primary = Color(0xFF7E22CE),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF3D8FF),
        onPrimaryContainer = Color(0xFF2A004F),
        secondary = Color(0xFF3B1D5E),
        tertiary = Color(0xFF0891B2),
        outline = Color(0xFF7D6699),
        outlineVariant = Color(0xFFCBB6E2),
        boardBackground = Color(0xFFF5ECFE),
        gridLine = Color(0xFFE1CEFA),
        snakeHead = Color(0xFF6B21A8),
        snakeBody = Color(0xFF9333EA),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFE11D48),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF059669),
        wallBorder = Color(0xFF7E22CE),
        obstacle = Color(0xFF9A3412),
        isDark = false
    )

    private val FireRedDark = MaterialPalette(
        background = Color(0xFF190A09),
        surface = Color(0xFF24100E),
        surfaceContainer = Color(0xFF301613),
        surfaceContainerHigh = Color(0xFF401E1A),
        primary = Color(0xFFFF5252),
        onPrimary = Color(0xFF330000),
        primaryContainer = Color(0xFF6B1414),
        onPrimaryContainer = Color(0xFFFFDAD6),
        secondary = Color(0xFFFFE4E1),
        tertiary = Color(0xFFFFAB40),
        outline = Color(0xFFE57373),
        outlineVariant = Color(0xFF572B26),
        boardBackground = Color(0xFF1D0D0B),
        gridLine = Color(0xFF361915),
        snakeHead = Color(0xFFFF5252),
        snakeBody = Color(0xFFF97316),
        snakeEye = Color(0xFF190A09),
        foodRegular = Color(0xFF4ADE80),
        foodGolden = Color(0xFFFACC15),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFE879F9),
        wallBorder = Color(0xFFFF5252),
        obstacle = Color(0xFF94A3B8),
        isDark = true
    )

    private val FireRedLight = MaterialPalette(
        background = Color(0xFFFFF5F5),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFFDE8E6),
        surfaceContainerHigh = Color(0xFFFAD4D0),
        primary = Color(0xFFC62828),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD6),
        onPrimaryContainer = Color(0xFF410002),
        secondary = Color(0xFF4A1A17),
        tertiary = Color(0xFFD97706),
        outline = Color(0xFF996763),
        outlineVariant = Color(0xFFE0B8B4),
        boardBackground = Color(0xFFFEF0EE),
        gridLine = Color(0xFFF3D2CE),
        snakeHead = Color(0xFFB91C1C),
        snakeBody = Color(0xFFEA580C),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFF16A34A),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF9333EA),
        wallBorder = Color(0xFFC62828),
        obstacle = Color(0xFF475569),
        isDark = false
    )

    private val GoldenPremiumDark = MaterialPalette(
        background = Color(0xFF121008),
        surface = Color(0xFF1C180D),
        surfaceContainer = Color(0xFF262012),
        surfaceContainerHigh = Color(0xFF332B18),
        primary = Color(0xFFFFD700),
        onPrimary = Color(0xFF261E00),
        primaryContainer = Color(0xFF524100),
        onPrimaryContainer = Color(0xFFFFEE99),
        secondary = Color(0xFFFFF8DC),
        tertiary = Color(0xFF38BDF8),
        outline = Color(0xFFD4AF37),
        outlineVariant = Color(0xFF4D4122),
        boardBackground = Color(0xFF16130A),
        gridLine = Color(0xFF2E2715),
        snakeHead = Color(0xFFFFD700),
        snakeBody = Color(0xFFEAB308),
        snakeEye = Color(0xFF121008),
        foodRegular = Color(0xFFEF4444),
        foodGolden = Color(0xFFFEF08A),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFC084FC),
        wallBorder = Color(0xFFFFD700),
        obstacle = Color(0xFF94A3B8),
        isDark = true
    )

    private val GoldenPremiumLight = MaterialPalette(
        background = Color(0xFFFFFBF0),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFFAF2DC),
        surfaceContainerHigh = Color(0xFFF3E7C5),
        primary = Color(0xFFA16207),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFEF08A),
        onPrimaryContainer = Color(0xFF2D1E00),
        secondary = Color(0xFF423007),
        tertiary = Color(0xFF0284C7),
        outline = Color(0xFF8C7540),
        outlineVariant = Color(0xFFDCC999),
        boardBackground = Color(0xFFFCF7E8),
        gridLine = Color(0xFFEADDBB),
        snakeHead = Color(0xFF854D0E),
        snakeBody = Color(0xFFCA8A04),
        snakeEye = Color(0xFFFFFFFF),
        foodRegular = Color(0xFFDC2626),
        foodGolden = Color(0xFFF59E0B),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF9333EA),
        wallBorder = Color(0xFFA16207),
        obstacle = Color(0xFF57534E),
        isDark = false
    )

    private val ClassicRetroDark = MaterialPalette(
        background = Color(0xFF182214),
        surface = Color(0xFF212E1C),
        surfaceContainer = Color(0xFF2A3B24),
        surfaceContainerHigh = Color(0xFF354A2E),
        primary = Color(0xFF9BBC0F),
        onPrimary = Color(0xFF0F380F),
        primaryContainer = Color(0xFF306230),
        onPrimaryContainer = Color(0xFFCADC9F),
        secondary = Color(0xFFD8E8B8),
        tertiary = Color(0xFF8BAC0F),
        outline = Color(0xFF8BAC0F),
        outlineVariant = Color(0xFF445E39),
        boardBackground = Color(0xFF1E2919),
        gridLine = Color(0xFF2E3F27),
        snakeHead = Color(0xFF9BBC0F),
        snakeBody = Color(0xFF8BAC0F),
        snakeEye = Color(0xFF0F380F),
        foodRegular = Color(0xFFEF4444),
        foodGolden = Color(0xFFFACC15),
        foodSlow = Color(0xFF38BDF8),
        foodBerry = Color(0xFFC084FC),
        wallBorder = Color(0xFF9BBC0F),
        obstacle = Color(0xFF6B8E23),
        isDark = true
    )

    private val ClassicRetroLight = MaterialPalette(
        background = Color(0xFFCADC9F),
        surface = Color(0xFFD7E8B0),
        surfaceContainer = Color(0xFFBBD08E),
        surfaceContainerHigh = Color(0xFFAEC47F),
        primary = Color(0xFF0F380F),
        onPrimary = Color(0xFFCADC9F),
        primaryContainer = Color(0xFF8BAC0F),
        onPrimaryContainer = Color(0xFF0F380F),
        secondary = Color(0xFF1E461E),
        tertiary = Color(0xFF306230),
        outline = Color(0xFF306230),
        outlineVariant = Color(0xFF8BAC0F),
        boardBackground = Color(0xFFC3D796),
        gridLine = Color(0xFFA8BF76),
        snakeHead = Color(0xFF0F380F),
        snakeBody = Color(0xFF306230),
        snakeEye = Color(0xFFCADC9F),
        foodRegular = Color(0xFFB91C1C),
        foodGolden = Color(0xFFD97706),
        foodSlow = Color(0xFF0284C7),
        foodBerry = Color(0xFF7E22CE),
        wallBorder = Color(0xFF0F380F),
        obstacle = Color(0xFF1E3A1E),
        isDark = false
    )

    private val DarkAmoled = MaterialPalette(
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        surfaceContainer = Color(0xFF121212),
        surfaceContainerHigh = Color(0xFF1E1E1E),
        primary = Color(0xFF00E676),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF00381B),
        onPrimaryContainer = Color(0xFF00E676),
        secondary = Color(0xFFF8FAFC),
        tertiary = Color(0xFF00B0FF),
        outline = Color(0xFF525252),
        outlineVariant = Color(0xFF282828),
        boardBackground = Color(0xFF030303),
        gridLine = Color(0xFF171717),
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
        themeMode: ThemeMode = ThemeMode.DARK,
        isSystemInDarkTheme: Boolean = true
    ): MaterialPalette {
        val effectiveDark = when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        return when (type) {
            MaterialThemeType.NEON_GREEN -> if (effectiveDark) NeonGreenDark else NeonGreenLight
            MaterialThemeType.CYBER_BLUE -> if (effectiveDark) CyberBlueDark else CyberBlueLight
            MaterialThemeType.PURPLE_GALAXY -> if (effectiveDark) PurpleGalaxyDark else PurpleGalaxyLight
            MaterialThemeType.FIRE_RED -> if (effectiveDark) FireRedDark else FireRedLight
            MaterialThemeType.GOLDEN_PREMIUM -> if (effectiveDark) GoldenPremiumDark else GoldenPremiumLight
            MaterialThemeType.CLASSIC_RETRO -> if (effectiveDark) ClassicRetroDark else ClassicRetroLight
            MaterialThemeType.DARK_AMOLED -> if (effectiveDark) DarkAmoled else NeonGreenLight
        }
    }
}
