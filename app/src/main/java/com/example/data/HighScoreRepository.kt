package com.example.data

import kotlinx.coroutines.flow.Flow

class HighScoreRepository(private val dao: HighScoreDao) {

    fun getTopScores(limit: Int = 50): Flow<List<HighScoreEntity>> = dao.getTopScores(limit)

    fun getTopScoresByDifficulty(difficulty: String, limit: Int = 50): Flow<List<HighScoreEntity>> =
        dao.getTopScoresByDifficulty(difficulty, limit)

    fun getHighScoreForDifficulty(difficulty: String): Flow<Int?> =
        dao.getHighScoreForDifficulty(difficulty)

    fun getOverallHighScore(): Flow<Int?> = dao.getOverallHighScore()

    suspend fun saveScore(
        playerName: String,
        score: Int,
        difficulty: String,
        snakeLength: Int
    ): Long {
        return dao.insertScore(
            HighScoreEntity(
                playerName = playerName.ifBlank { "ANONYMOUS" },
                score = score,
                difficulty = difficulty,
                snakeLength = snakeLength
            )
        )
    }

    suspend fun clearScores() {
        dao.clearAllScores()
    }
}
