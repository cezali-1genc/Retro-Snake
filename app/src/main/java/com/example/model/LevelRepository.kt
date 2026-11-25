package com.example.model

enum class LevelTier(val label: String, val range: IntRange) {
    BEGINNER("Başlangıç", 1..10),
    EASY("Kolay", 11..25),
    MEDIUM("Orta", 26..50),
    HARD("Zor", 51..75),
    EXPERT("Uzman", 76..100)
}

data class GameLevel(
    val number: Int,
    val tier: LevelTier,
    val title: String,
    val targetScore: Int,
    val hasWalls: Boolean,
    val hasPortals: Boolean,
    val movingObstacleCount: Int,
    val initialTickMs: Long,
    val minTickMs: Long,
    val rewardCoins: Int,
    val mapPatternIndex: Int
)

object LevelRepository {
    val ALL_LEVELS: List<GameLevel> = (1..100).map { num ->
        val tier = when (num) {
            in 1..10 -> LevelTier.BEGINNER
            in 11..25 -> LevelTier.EASY
            in 26..50 -> LevelTier.MEDIUM
            in 51..75 -> LevelTier.HARD
            else -> LevelTier.EXPERT
        }
        val hasPortals = num >= 15 && (num % 5 == 0 || num % 7 == 0)
        val hasWalls = num >= 6 && (num % 3 != 0 || num >= 51)
        val movingObs = when {
            num < 26 -> 0
            num < 51 -> if (num % 4 == 0) 2 else 0
            num < 76 -> 2 + (num % 3)
            else -> 3 + (num % 4)
        }
        val baseTarget = when {
            num <= 10 -> 50 + num * 12
            num <= 25 -> 160 + (num - 10) * 18
            num <= 50 -> 420 + (num - 25) * 24
            num <= 75 -> 1020 + (num - 50) * 32
            else -> 1820 + (num - 75) * 45
        }
        val initialTick = (220L - num * 1.25f).toLong().coerceIn(90L, 220L)
        val minTick = (initialTick - 55L).coerceAtLeast(45L)
        val rewardCoins = 15 + (num / 3) * 3

        GameLevel(
            number = num,
            tier = tier,
            title = "Bölüm $num",
            targetScore = baseTarget,
            hasWalls = hasWalls,
            hasPortals = hasPortals,
            movingObstacleCount = movingObs,
            initialTickMs = initialTick,
            minTickMs = minTick,
            rewardCoins = rewardCoins,
            mapPatternIndex = (num - 1) % 10
        )
    }

    fun getLevel(number: Int): GameLevel =
        ALL_LEVELS.firstOrNull { it.number == number } ?: ALL_LEVELS.first()

    /**
     * Generates deterministic obstacle coordinates tailored to board dimensions
     * while guaranteeing a clear spawn corridor for the snake.
     */
    fun generateObstaclesForLevel(
        level: GameLevel,
        width: Int,
        height: Int,
        safePoints: Set<Point>
    ): List<Point> {
        val result = linkedSetOf<Point>()
        val cx = width / 2
        val cy = height / 2

        when (level.mapPatternIndex) {
            0 -> {
                // Open training or 4 corner pillars for higher levels
                if (level.number > 5) {
                    listOf(
                        Point(3, 3), Point(4, 3),
                        Point(width - 4, 3), Point(width - 5, 3),
                        Point(3, height - 4), Point(4, height - 4),
                        Point(width - 4, height - 4), Point(width - 5, height - 4)
                    ).forEach { result.add(it) }
                }
            }
            1 -> {
                // Twin horizontal bars
                for (x in 4 until width - 4 step 2) {
                    result.add(Point(x, 5))
                    result.add(Point(x, height - 6))
                }
            }
            2 -> {
                // Vertical gate columns
                for (y in 4 until height - 4) {
                    if (y !in (cy - 2)..(cy + 2)) {
                        result.add(Point(5, y))
                        result.add(Point(width - 6, y))
                    }
                }
            }
            3 -> {
                // Center diamond / cross blocks
                for (offset in -3..3) {
                    if (offset != 0) {
                        result.add(Point(cx + offset, 4))
                        result.add(Point(cx + offset, height - 5))
                    }
                }
                result.add(Point(cx, cy - 4))
                result.add(Point(cx, cy + 4))
            }
            4 -> {
                // Diagonal stepping stones
                val steps = minOf(width, height) / 3
                for (i in 2..steps) {
                    result.add(Point(i + 1, i + 2))
                    result.add(Point(width - i - 2, height - i - 3))
                }
            }
            5 -> {
                // Four inner bastion L-corners
                listOf(
                    Point(4, 5), Point(5, 5), Point(4, 6),
                    Point(width - 5, 5), Point(width - 6, 5), Point(width - 5, 6),
                    Point(4, height - 6), Point(5, height - 6), Point(4, height - 7),
                    Point(width - 5, height - 6), Point(width - 6, height - 6), Point(width - 5, height - 7)
                ).forEach { result.add(it) }
            }
            6 -> {
                // Narrow twin side channels
                for (x in 3 until width - 3) {
                    if (x !in (cx - 2)..(cx + 2)) {
                        result.add(Point(x, 6))
                        result.add(Point(x, height - 7))
                    }
                }
            }
            7 -> {
                // Checker islands
                for (x in 4 until width - 4 step 4) {
                    for (y in 4 until height - 4 step 5) {
                        result.add(Point(x, y))
                        result.add(Point(x + 1, y))
                    }
                }
            }
            8 -> {
                // Fortress center split
                for (y in 5 until height - 5) {
                    if (y !in (cy - 2)..(cy + 2)) {
                        result.add(Point(cx, y))
                    }
                }
            }
            else -> {
                // Expert corridor zigzag
                for (x in 3 until width - 5) result.add(Point(x, 6))
                for (x in 5 until width - 3) result.add(Point(x, height - 7))
                for (y in 9 until height - 9) {
                    if (y !in (cy - 1)..(cy + 1)) result.add(Point(cx, y))
                }
            }
        }

        // Never place obstacles on snake spawn or immediate path ahead
        return result.filter { p ->
            p.x in 1 until (width - 1) &&
                p.y in 1 until (height - 1) &&
                !safePoints.contains(p)
        }
    }
}
