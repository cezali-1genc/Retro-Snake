package com.example

import com.example.i18n.AppLanguage
import com.example.i18n.AppLocalization
import com.example.model.AchievementRepository
import com.example.model.ControlMode
import com.example.model.DailyRewardsRepository
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.FoodType
import com.example.model.GameMode
import com.example.model.LevelRepository
import com.example.model.LevelTier
import com.example.model.MaterialThemeType
import com.example.model.MissionsRepository
import com.example.model.Point
import com.example.model.SnakeSkinRepository
import com.example.model.TipRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `direction opposite logic prevents instant self collision`() {
        assertTrue(Direction.UP.isOpposite(Direction.DOWN))
        assertTrue(Direction.DOWN.isOpposite(Direction.UP))
        assertTrue(Direction.LEFT.isOpposite(Direction.RIGHT))
        assertTrue(Direction.RIGHT.isOpposite(Direction.LEFT))
        assertFalse(Direction.UP.isOpposite(Direction.LEFT))
        assertFalse(Direction.RIGHT.isOpposite(Direction.DOWN))
    }

    @Test
    fun `all 100 levels exist across 5 tiers with safe spawn corridors`() {
        assertEquals(100, LevelRepository.ALL_LEVELS.size)
        assertEquals(5, LevelTier.entries.size)

        val safeZone = setOf(
            Point(6, 12), Point(5, 12), Point(4, 12),
            Point(7, 12), Point(8, 12), Point(9, 12)
        )
        LevelRepository.ALL_LEVELS.forEach { level ->
            assertTrue(level.targetScore > 0)
            assertTrue(level.rewardCoins > 0)
            val obs = LevelRepository.generateObstaclesForLevel(level, 20, 24, safeZone)
            assertTrue("Obstacles must not overlap safeZone in level ${level.number}", obs.none { safeZone.contains(it) })
        }
    }

    @Test
    fun `30 unique snake skins and 7 playable modes are configured`() {
        assertEquals(30, SnakeSkinRepository.ALL_SKINS.size)
        assertEquals(30, SnakeSkinRepository.ALL_SKINS.map { it.id }.toSet().size)
        assertEquals(7, GameMode.PLAYABLE_MODES.size)
        assertEquals(9, FoodType.entries.size)
        assertEquals(12, AchievementRepository.ALL_ACHIEVEMENTS.size)
        assertEquals(7, DailyRewardsRepository.SEVEN_DAY_REWARDS.size)
        assertEquals(8, MissionsRepository.ALL_MISSIONS.size)
    }

    @Test
    fun `all 7 languages localize cleanly and Arabic enables RTL`() {
        assertEquals(7, AppLanguage.entries.size)
        AppLanguage.entries.forEach { lang ->
            val strings = AppLocalization.get(lang)
            assertTrue(strings.play.isNotBlank())
            assertTrue(strings.settings.isNotBlank())
        }
        assertTrue(AppLanguage.AR.isRtl)
        assertFalse(AppLanguage.TR.isRtl)
        assertFalse(AppLanguage.EN.isRtl)
    }

    @Test
    fun `legacy themes and control modes migrate cleanly`() {
        assertEquals(MaterialThemeType.NEON_GREEN, MaterialThemeType.fromStored("EMERALD"))
        assertEquals(MaterialThemeType.CYBER_BLUE, MaterialThemeType.fromStored("OCEAN"))
        assertEquals(ControlMode.FOUR_BUTTONS, ControlMode.fromStored("TOUCH_OVERLAY"))
        assertTrue(Difficulty.INSANE.initialTickMs < Difficulty.EASY.initialTickMs)
        assertTrue(TipRepository.ALL_TIPS.isNotEmpty())
    }
}
