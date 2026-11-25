package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {

    @Query("SELECT * FROM high_scores ORDER BY score DESC, timestamp ASC LIMIT :limit")
    fun getTopScores(limit: Int = 50): Flow<List<HighScoreEntity>>

    @Query("SELECT * FROM high_scores WHERE timestamp >= :sinceTimestamp ORDER BY score DESC, timestamp ASC LIMIT :limit")
    fun getTopScoresSince(sinceTimestamp: Long, limit: Int = 50): Flow<List<HighScoreEntity>>

    @Query("SELECT * FROM high_scores WHERE gameMode = :mode ORDER BY score DESC, timestamp ASC LIMIT :limit")
    fun getTopScoresByMode(mode: String, limit: Int = 50): Flow<List<HighScoreEntity>>

    @Query("SELECT * FROM high_scores WHERE difficulty = :difficulty ORDER BY score DESC, timestamp ASC LIMIT :limit")
    fun getTopScoresByDifficulty(difficulty: String, limit: Int = 50): Flow<List<HighScoreEntity>>

    @Query("SELECT MAX(score) FROM high_scores")
    fun getOverallHighScore(): Flow<Int?>

    @Query("SELECT MAX(score) FROM high_scores WHERE gameMode = :mode")
    fun getHighScoreForMode(mode: String): Flow<Int?>

    @Query("SELECT MAX(score) FROM high_scores WHERE difficulty = :difficulty")
    fun getHighScoreForDifficulty(difficulty: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: HighScoreEntity): Long

    @Query("DELETE FROM high_scores")
    suspend fun clearAllScores()
}
