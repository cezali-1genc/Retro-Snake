package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HighScoreEntity
import com.example.data.SettingsManager
import com.example.data.SnakeDatabase
import com.example.i18n.AppLanguage
import com.example.model.ControlMode
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.MaterialThemeType
import com.example.model.MusicStyle
import com.example.model.SnakeColorMode
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
                snakeLength = 22,
                gameMode = GameMode.ENDLESS.id,
                durationSec = 95
            )
        )

        val scores = dao.getTopScores(50).first()
        assertEquals(1, scores.size)
        assertEquals("MATERIAL_CHAMP", scores[0].playerName)
        assertEquals(350, scores[0].score)
        assertEquals(GameMode.ENDLESS.id, scores[0].gameMode)
    }

    @Test
    fun `settings manager persists and restores v7 settings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SettingsManager(context)

        manager.setThemeMode(ThemeMode.DARK)
        manager.setMaterialTheme(MaterialThemeType.CYBER_BLUE)
        manager.setSoundVolume(0.65f)
        manager.setMusicVolume(0.55f)
        manager.setMusicStyle(MusicStyle.NEON_CYBER)
        manager.setControlMode(ControlMode.JOYSTICK)
        manager.setLanguage(AppLanguage.EN)
        manager.setLowPerformanceMode(true)
        manager.setDifficulty(Difficulty.HARD)
        manager.setTipsEnabled(true)

        val reloaded = SettingsManager(context).settings.value

        assertEquals(ThemeMode.DARK, reloaded.themeMode)
        assertEquals(MaterialThemeType.CYBER_BLUE, reloaded.materialTheme)
        assertEquals(0.65f, reloaded.soundVolume, 0.01f)
        assertEquals(0.55f, reloaded.musicVolume, 0.01f)
        assertEquals(MusicStyle.NEON_CYBER, reloaded.musicStyle)
        assertEquals(ControlMode.JOYSTICK, reloaded.controlMode)
        assertEquals(AppLanguage.EN, reloaded.language)
        assertTrue(reloaded.lowPerformanceMode)
        assertEquals(Difficulty.HARD, reloaded.difficulty)
        assertTrue(reloaded.tipsEnabled)
    }

    @Test
    fun `settings manager persists custom snake color and level progress`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SettingsManager(context)

        val customBlue = 0xFF3B82F6.toInt()
        manager.setCustomSnakeColor(customBlue)
        manager.recordLevelCompletion(1, 3, 20)

        val reloaded = SettingsManager(context).settings.value
        assertEquals(SnakeColorMode.CUSTOM, reloaded.snakeColorMode)
        assertEquals(customBlue, reloaded.customSnakeColorArgb)
        assertEquals(3, reloaded.levelStars[1])
        assertEquals(2, reloaded.highestUnlockedLevel)
    }
}
