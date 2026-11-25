package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HighScoreEntity
import com.example.data.SettingsManager
import com.example.data.SnakeDatabase
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.MaterialThemeType
import com.example.model.SnakeColorMode
import com.example.model.ThemeMode
import com.example.model.TipRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Retro Snake", appName)
    }

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
    fun `difficulty properties configuration verified`() {
        assertTrue(Difficulty.INSANE.initialTickMs < Difficulty.EASY.initialTickMs)
        assertTrue(Difficulty.INSANE.scoreMultiplier > Difficulty.EASY.scoreMultiplier)
        assertTrue(Difficulty.HARD.hasObstacles)
        assertFalse(Difficulty.EASY.hasObstacles)
    }

    @Test
    fun `database insert and query top scores`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SnakeDatabase.getInstance(context)
        val dao = db.highScoreDao()

        dao.clearAllScores()

        dao.insertScore(
            HighScoreEntity(
                playerName = "MATERIAL_CHAMP",
                score = 350,
                difficulty = Difficulty.MEDIUM.label,
                snakeLength = 22
            )
        )

        val scores = dao.getTopScores().first()
        assertEquals(1, scores.size)
        assertEquals("MATERIAL_CHAMP", scores[0].playerName)
        assertEquals(350, scores[0].score)
    }

    @Test
    fun `settings manager persists and restores material theme volume and tips`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SettingsManager(context)

        manager.setThemeMode(ThemeMode.DARK)
        manager.setMaterialTheme(MaterialThemeType.OCEAN)
        manager.setSoundVolume(0.65f)
        manager.setDifficulty(Difficulty.HARD)
        manager.setTipsEnabled(true)
        manager.setFpsOverlayEnabled(true)

        // Simulate app restart by creating a new instance reading from disk
        val reloadedManager = SettingsManager(context)
        val state = reloadedManager.settings.value

        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals(MaterialThemeType.OCEAN, state.materialTheme)
        assertEquals(0.65f, state.soundVolume, 0.01f)
        assertEquals(Difficulty.HARD, state.difficulty)
        assertTrue(state.tipsEnabled)
        assertTrue(state.fpsOverlayEnabled)
    }

    @Test
    fun `beginner tips repository has essential mechanics`() {
        val tips = TipRepository.ALL_TIPS
        assertTrue(tips.isNotEmpty())
        assertTrue(tips.any { it.id == "speed_increase" })
        assertTrue(tips.any { it.id == "golden_star" })
        assertTrue(tips.any { it.id == "blue_potion" })
    }

    @Test
    fun `settings manager persists and restores custom snake color`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SettingsManager(context)

        val customBlue = 0xFF3B82F6.toInt()
        manager.setCustomSnakeColor(customBlue)

        val reloaded = SettingsManager(context)
        assertEquals(SnakeColorMode.CUSTOM, reloaded.settings.value.snakeColorMode)
        assertEquals(customBlue, reloaded.settings.value.customSnakeColorArgb)

        reloaded.setSnakeColorMode(SnakeColorMode.THEME)
        val reloadedTheme = SettingsManager(context)
        assertEquals(SnakeColorMode.THEME, reloadedTheme.settings.value.snakeColorMode)
    }
}
