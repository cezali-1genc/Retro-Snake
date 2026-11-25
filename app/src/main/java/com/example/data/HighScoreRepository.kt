package com.example.data

import kotlinx.coroutines.flow.Flow

class HighScoreRepository(private val dao: HighScoreDao) {

    fun getTopScores(limit: Int = 50): Flow<List<HighScoreEntity>> = dao.getTopScores(limit)

    fun getTopScoresSince(sinceTimestamp: Long, limit: Int = 50): Flow<List<HighScoreEntity>> =
        dao.getTopScoresSince(sinceTimestamp, limit)

    fun getTopScoresByMode(mode: String, limit: Int = 50): Flow<List<HighScoreEntity>> =
        dao.getTopScoresByMode(mode, limit)

    fun getTopScoresByDifficulty(difficulty: String, limit: Int = 50): Flow<List<HighScoreEntity>> =
        dao.getTopScoresByDifficulty(difficulty, limit)

    fun getHighScoreForDifficulty(difficulty: String): Flow<Int?> =
        dao.getHighScoreForDifficulty(difficulty)

    fun getOverallHighScore(): Flow<Int?> = dao.getOverallHighScore()

    suspend fun saveScore(
        playerName: String,
        score: Int,
        difficulty: String,
        snakeLength: Int,
        gameMode: String = "ENDLESS",
        durationSec: Int = 0
    ): Long {
        return dao.insertScore(
            HighScoreEntity(
                playerName = playerName.ifBlank { "ANONYMOUS" },
                score = score,
                difficulty = difficulty,
                snakeLength = snakeLength,
                gameMode = gameMode,
                durationSec = durationSec
            )
        )
    }

    suspend fun clearScores() {
        dao.clearAllScores()
    }
}
