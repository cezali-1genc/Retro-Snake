package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

enum class SkinPattern {
    SOLID,
    STRIPED,
    GRADIENT,
    CYBER_GRID,
    PRISMATIC,
    SCALES,
    ELECTRIC
}

enum class SkinHeadStyle {
    CLASSIC,
    VIPER,
    CYBER_VISOR,
    CROWNED,
    HORNED
}

enum class SkinTrailEffect {
    NONE,
    SPARKLE,
    FLAME,
    ICE,
    PLASMA,
    MATRIX
}

data class SnakeSkin(
    val id: String,
    val name: String,
    val description: String,
    val headColor: Color,
    val bodyPrimary: Color,
    val bodySecondary: Color,
    val auraColor: Color,
    val pattern: SkinPattern,
    val headStyle: SkinHeadStyle,
    val trailEffect: SkinTrailEffect,
    val unlockCostCoins: Int,
    val isDefaultUnlocked: Boolean = false
)

object SnakeSkinRepository {
    val ALL_SKINS: List<SnakeSkin> = listOf(
        SnakeSkin("classic", "Classic Snake", "Zamansız retro arcade stili", Color(0xFF16A34A), Color(0xFF22C55E), Color(0xFF15803D), Color(0xFF4ADE80), SkinPattern.STRIPED, SkinHeadStyle.CLASSIC, SkinTrailEffect.NONE, 0, true),
        SnakeSkin("neon", "Neon Snake", "Parlak neon ışıklı gövde", Color(0xFF00FF87), Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFF00FF87), SkinPattern.GRADIENT, SkinHeadStyle.VIPER, SkinTrailEffect.SPARKLE, 0, true),
        SnakeSkin("fire", "Fire Snake", "Alev ve kor parçacıklı yılan", Color(0xFFFF5722), Color(0xFFFF3D00), Color(0xFFFFC107), Color(0xFFFF6D00), SkinPattern.GRADIENT, SkinHeadStyle.HORNED, SkinTrailEffect.FLAME, 60),
        SnakeSkin("ice", "Ice Snake", "Buzul kristali ve soğuk iz", Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFFE0F2FE), Color(0xFF7DD3FC), SkinPattern.SCALES, SkinHeadStyle.VIPER, SkinTrailEffect.ICE, 75),
        SnakeSkin("galaxy", "Galaxy Snake", "Kozmik bulutsu ve yıldız tozu", Color(0xFFA855F7), Color(0xFF7E22CE), Color(0xFFEC4899), Color(0xFFD946EF), SkinPattern.GRADIENT, SkinHeadStyle.CROWNED, SkinTrailEffect.SPARKLE, 90),
        SnakeSkin("golden", "Golden Snake", "Saf altın kraliyet zırhı", Color(0xFFFACC15), Color(0xFFEAB308), Color(0xFFFEF08A), Color(0xFFFDE047), SkinPattern.SCALES, SkinHeadStyle.CROWNED, SkinTrailEffect.SPARKLE, 120),
        SnakeSkin("cyber", "Cyber Snake", "Siber vizör ve neon devreler", Color(0xFF06B6D4), Color(0xFF0891B2), Color(0xFFF43F5E), Color(0xFF22D3EE), SkinPattern.CYBER_GRID, SkinHeadStyle.CYBER_VISOR, SkinTrailEffect.PLASMA, 110),
        SnakeSkin("diamond", "Diamond Snake", "Elmas parlaklığında prizma", Color(0xFF67E8F9), Color(0xFFA5F3FC), Color(0xFFFFFFFF), Color(0xFF38BDF8), SkinPattern.SCALES, SkinHeadStyle.CROWNED, SkinTrailEffect.ICE, 180),
        SnakeSkin("rainbow", "Rainbow Snake", "7 renkli dinamik gökkuşağı", Color(0xFFEC4899), Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFFF59E0B), SkinPattern.PRISMATIC, SkinHeadStyle.CLASSIC, SkinTrailEffect.SPARKLE, 100),
        SnakeSkin("shadow", "Shadow Snake", "Karanlık gölge ve mor aura", Color(0xFF312E81), Color(0xFF1E1B4B), Color(0xFF6366F1), Color(0xFF818CF8), SkinPattern.GRADIENT, SkinHeadStyle.VIPER, SkinTrailEffect.PLASMA, 130),
        SnakeSkin("toxic", "Toxic Snake", "Asit yeşili biyolojik iz", Color(0xFF84CC16), Color(0xFFA3E635), Color(0xFF365314), Color(0xFFBEF264), SkinPattern.STRIPED, SkinHeadStyle.HORNED, SkinTrailEffect.PLASMA, 140),
        SnakeSkin("electric", "Electric Snake", "Yüksek voltajlı şimşek efekti", Color(0xFFFDE047), Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFFFEF08A), SkinPattern.ELECTRIC, SkinHeadStyle.CYBER_VISOR, SkinTrailEffect.PLASMA, 150),
        SnakeSkin("lava_dragon", "Lava Dragon", "Volkanik pullu ejderha", Color(0xFFDC2626), Color(0xFF991B1B), Color(0xFFF97316), Color(0xFFFB923C), SkinPattern.SCALES, SkinHeadStyle.HORNED, SkinTrailEffect.FLAME, 160),
        SnakeSkin("emerald_viper", "Emerald Viper", "Zümrüt engerek asaleti", Color(0xFF059669), Color(0xFF10B981), Color(0xFF064E3B), Color(0xFF34D399), SkinPattern.SCALES, SkinHeadStyle.VIPER, SkinTrailEffect.SPARKLE, 170),
        SnakeSkin("ocean_pulse", "Ocean Pulse", "Derin okyanus dalga ritmi", Color(0xFF2563EB), Color(0xFF0EA5E9), Color(0xFF1D4ED8), Color(0xFF60A5FA), SkinPattern.GRADIENT, SkinHeadStyle.CLASSIC, SkinTrailEffect.ICE, 175),
        SnakeSkin("crimson_fang", "Crimson Fang", "Keskin yakut dişli avcı", Color(0xFFE11D48), Color(0xFFBE123C), Color(0xFFFDA4AF), Color(0xFFFB7185), SkinPattern.STRIPED, SkinHeadStyle.VIPER, SkinTrailEffect.FLAME, 185),
        SnakeSkin("solar_flare", "Solar Flare", "Güneş patlaması altın-turuncu", Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFEF3C7), Color(0xFFFBBF24), SkinPattern.ELECTRIC, SkinHeadStyle.CROWNED, SkinTrailEffect.FLAME, 195),
        SnakeSkin("arctic_frost", "Arctic Frost", "Kuzey kutbu kristal beyazı", Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF0284C7), Color(0xFFF0F9FF), SkinPattern.GRADIENT, SkinHeadStyle.CLASSIC, SkinTrailEffect.ICE, 200),
        SnakeSkin("matrix_code", "Matrix Code", "Dijital yeşil veri akışı", Color(0xFF00FF41), Color(0xFF008F11), Color(0xFF003B00), Color(0xFF00FF41), SkinPattern.CYBER_GRID, SkinHeadStyle.CYBER_VISOR, SkinTrailEffect.MATRIX, 210),
        SnakeSkin("synthwave_84", "Synthwave 84", "80'ler retro neon pembe-camgöbeği", Color(0xFFFF007F), Color(0xFF9D4EDD), Color(0xFF00F5D4), Color(0xFFFF007F), SkinPattern.GRADIENT, SkinHeadStyle.CYBER_VISOR, SkinTrailEffect.PLASMA, 220),
        SnakeSkin("cobalt_knight", "Cobalt Knight", "Metalik kobalt zırhlı yılan", Color(0xFF1D4ED8), Color(0xFF3B82F6), Color(0xFF93C5FD), Color(0xFF60A5FA), SkinPattern.SCALES, SkinHeadStyle.CROWNED, SkinTrailEffect.SPARKLE, 230),
        SnakeSkin("amethyst_king", "Amethyst King", "Kraliyet moru ametist taç", Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFDDD6FE), Color(0xFFA78BFA), SkinPattern.SCALES, SkinHeadStyle.CROWNED, SkinTrailEffect.SPARKLE, 240),
        SnakeSkin("mint_breeze", "Mint Breeze", "Ferah nane ve turkuaz geçiş", Color(0xFF2DD4BF), Color(0xFF14B8A6), Color(0xFFCCFBF1), Color(0xFF5EEAD4), SkinPattern.GRADIENT, SkinHeadStyle.CLASSIC, SkinTrailEffect.ICE, 250),
        SnakeSkin("sunset_glow", "Sunset Glow", "Gün batımı mercan ve mor", Color(0xFFFB7185), Color(0xFFF97316), Color(0xFFC026D3), Color(0xFFFDBA74), SkinPattern.GRADIENT, SkinHeadStyle.VIPER, SkinTrailEffect.FLAME, 260),
        SnakeSkin("obsidian_gold", "Obsidian Gold", "Siyah obsidyen üstü altın şerit", Color(0xFFF59E0B), Color(0xFF1F2937), Color(0xFFFBBF24), Color(0xFFFDE047), SkinPattern.STRIPED, SkinHeadStyle.CROWNED, SkinTrailEffect.SPARKLE, 280),
        SnakeSkin("plasma_storm", "Plasma Storm", "İyonize mor-camgöbeği fırtına", Color(0xFFE879F9), Color(0xFF6366F1), Color(0xFF22D3EE), Color(0xFFF0ABFC), SkinPattern.ELECTRIC, SkinHeadStyle.CYBER_VISOR, SkinTrailEffect.PLASMA, 300),
        SnakeSkin("sakura", "Sakura Blossom", "Bahar kiraz çiçeği pembesi", Color(0xFFF472B6), Color(0xFFF9A8D4), Color(0xFFFDF2F8), Color(0xFFFBCFE8), SkinPattern.GRADIENT, SkinHeadStyle.CLASSIC, SkinTrailEffect.SPARKLE, 320),
        SnakeSkin("candy_pop", "Candy Pop", "Renkli şekerleme şeritleri", Color(0xFFFB7185), Color(0xFF38BDF8), Color(0xFFFDE047), Color(0xFFF472B6), SkinPattern.STRIPED, SkinHeadStyle.CLASSIC, SkinTrailEffect.SPARKLE, 340),
        SnakeSkin("stealth_camo", "Stealth Camo", "Taktiksel gece kamuflajı", Color(0xFF475569), Color(0xFF334155), Color(0xFF1E293B), Color(0xFF94A3B8), SkinPattern.CYBER_GRID, SkinHeadStyle.VIPER, SkinTrailEffect.NONE, 360),
        SnakeSkin("ultra_prismatic", "Ultra Prismatic", "Efsanevi prizmatik şampiyon skini", Color(0xFFFFFFFF), Color(0xFF00F5D4), Color(0xFFFF007F), Color(0xFFFDE047), SkinPattern.PRISMATIC, SkinHeadStyle.CROWNED, SkinTrailEffect.PLASMA, 500)
    )

    fun getById(id: String): SnakeSkin = ALL_SKINS.firstOrNull { it.id == id } ?: ALL_SKINS.first()

    fun segmentColor(skin: SnakeSkin, index: Int, totalBody: Int): Color {
        return when (skin.pattern) {
            SkinPattern.SOLID -> skin.bodyPrimary
            SkinPattern.STRIPED -> if (index % 2 == 0) skin.bodyPrimary else skin.bodySecondary
            SkinPattern.GRADIENT -> {
                val t = if (totalBody <= 1) 0f else (index.toFloat() / (totalBody - 1).toFloat()).coerceIn(0f, 1f)
                lerp(skin.bodyPrimary, skin.bodySecondary, t)
            }
            SkinPattern.CYBER_GRID -> if (index % 3 == 0) skin.bodySecondary else skin.bodyPrimary
            SkinPattern.SCALES -> if (index % 2 == 0) skin.bodyPrimary else lerp(skin.bodyPrimary, skin.bodySecondary, 0.45f)
            SkinPattern.ELECTRIC -> if (index % 4 == 0) Color.White else if (index % 2 == 0) skin.bodyPrimary else skin.bodySecondary
            SkinPattern.PRISMATIC -> {
                val rainbow = VibrantSnakeColors.RAINBOW
                rainbow[index % rainbow.size]
            }
        }
    }
}
