package com.example.model

data class DailyRewardTier(
    val day: Int,
    val title: String,
    val coins: Int,
    val iconEmoji: String
)

enum class MissionPeriod {
    DAILY,
    WEEKLY
}

enum class MissionType {
    EAT_FOODS,
    SCORE_POINTS,
    COLLECT_BONUSES,
    COMPLETE_LEVELS,
    PLAY_GAMES
}

data class MissionDefinition(
    val id: String,
    val title: String,
    val description: String,
    val period: MissionPeriod,
    val type: MissionType,
    val targetValue: Int,
    val rewardCoins: Int,
    val iconEmoji: String
)

object DailyRewardsRepository {
    val SEVEN_DAY_REWARDS = listOf(
        DailyRewardTier(1, "1. Gün", 25, "🪙"),
        DailyRewardTier(2, "2. Gün", 40, "🍎"),
        DailyRewardTier(3, "3. Gün", 60, "⚡"),
        DailyRewardTier(4, "4. Gün", 85, "🛡️"),
        DailyRewardTier(5, "5. Gün", 110, "🌟"),
        DailyRewardTier(6, "6. Gün", 150, "💎"),
        DailyRewardTier(7, "7. Gün Mega", 250, "👑")
    )
}

object MissionsRepository {
    val ALL_MISSIONS = listOf(
        // Daily Missions
        MissionDefinition(
            id = "daily_eat_25",
            title = "Günlük Ziyafet",
            description = "Bugün 25 yem topla",
            period = MissionPeriod.DAILY,
            type = MissionType.EAT_FOODS,
            targetValue = 25,
            rewardCoins = 40,
            iconEmoji = "🍎"
        ),
        MissionDefinition(
            id = "daily_score_300",
            title = "Günlük Skor Avı",
            description = "Bugün toplam 300 puan kazan",
            period = MissionPeriod.DAILY,
            type = MissionType.SCORE_POINTS,
            targetValue = 300,
            rewardCoins = 55,
            iconEmoji = "🔥"
        ),
        MissionDefinition(
            id = "daily_bonus_4",
            title = "Güç Toplayıcı",
            description = "Bugün 4 özel güç/bonus al",
            period = MissionPeriod.DAILY,
            type = MissionType.COLLECT_BONUSES,
            targetValue = 4,
            rewardCoins = 50,
            iconEmoji = "⚡"
        ),
        MissionDefinition(
            id = "daily_games_3",
            title = "Günlük Isınma",
            description = "Bugün 3 oyun tamamla",
            period = MissionPeriod.DAILY,
            type = MissionType.PLAY_GAMES,
            targetValue = 3,
            rewardCoins = 35,
            iconEmoji = "🎮"
        ),

        // Weekly Missions
        MissionDefinition(
            id = "weekly_eat_150",
            title = "Haftalık Hasat",
            description = "Bu hafta 150 yem topla",
            period = MissionPeriod.WEEKLY,
            type = MissionType.EAT_FOODS,
            targetValue = 150,
            rewardCoins = 160,
            iconEmoji = "🍏"
        ),
        MissionDefinition(
            id = "weekly_score_2500",
            title = "Haftanın Efsanesi",
            description = "Bu hafta toplam 2500 puan topla",
            period = MissionPeriod.WEEKLY,
            type = MissionType.SCORE_POINTS,
            targetValue = 2500,
            rewardCoins = 220,
            iconEmoji = "🏆"
        ),
        MissionDefinition(
            id = "weekly_levels_5",
            title = "Bölüm Fatihi",
            description = "Bu hafta 5 seviye tamamla",
            period = MissionPeriod.WEEKLY,
            type = MissionType.COMPLETE_LEVELS,
            targetValue = 5,
            rewardCoins = 200,
            iconEmoji = "🗺️"
        ),
        MissionDefinition(
            id = "weekly_bonus_20",
            title = "Bonus Ustası",
            description = "Bu hafta 20 özel nesne topla",
            period = MissionPeriod.WEEKLY,
            type = MissionType.COLLECT_BONUSES,
            targetValue = 20,
            rewardCoins = 180,
            iconEmoji = "💎"
        )
    )
}
