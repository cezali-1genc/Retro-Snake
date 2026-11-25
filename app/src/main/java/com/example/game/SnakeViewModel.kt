package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RetroMusicPlayer
import com.example.audio.RetroSoundManager
import com.example.data.HighScoreEntity
import com.example.data.HighScoreRepository
import com.example.data.SettingsManager
import com.example.data.SnakeDatabase
import com.example.data.UserSettings
import com.example.model.ControlMode
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.Food
import com.example.model.FoodType
import com.example.model.GameStatus
import com.example.model.InGameTip
import com.example.model.MaterialPalette
import com.example.model.MaterialPalettes
import com.example.model.Point
import com.example.model.TipRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class EatenFoodEvent(
    val position: Point,
    val type: FoodType,
    val pointsEarned: Int = 10,
    val comboCount: Int = 1,
    val isFeverActive: Boolean = false,
    val id: Long = System.nanoTime()
)

data class SnakeGameState(
    val snake: List<Point> = emptyList(),
    val currentDirection: Direction = Direction.RIGHT,
    val pendingDirection: Direction = Direction.RIGHT,
    val regularFood: Food? = null,
    val specialFood: Food? = null,
    val obstacles: List<Point> = emptyList(),
    val score: Int = 0,
    val foodEatenCount: Int = 0,
    val comboCount: Int = 0,
    val comboExpiresAtMs: Long = 0L,
    val doubleScoreUntilMs: Long = 0L,
    val slowMotionUntilMs: Long = 0L,
    val lastEatenEvent: EatenFoodEvent? = null,
    val status: GameStatus = GameStatus.READY,
    val currentTickMs: Long = 170L,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isNewHighScore: Boolean = false,
    val boardWidth: Int = 20,
    val boardHeight: Int = 24
)

class SnakeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SnakeDatabase.getInstance(application)
    val repository = HighScoreRepository(db.highScoreDao())
    val settingsManager = SettingsManager(application)
    val soundManager = RetroSoundManager(application)
    val musicPlayer = RetroMusicPlayer()

    val settings: StateFlow<UserSettings> = settingsManager.settings

    private val _gameState = MutableStateFlow(SnakeGameState())
    val gameState: StateFlow<SnakeGameState> = _gameState.asStateFlow()

    // Observable High Scores from Room
    val topScores: StateFlow<List<HighScoreEntity>> = repository.getTopScores(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topScoresForGameOver: StateFlow<List<HighScoreEntity>> = repository.getTopScores(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentHighScore: StateFlow<Int?> = repository.getOverallHighScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _activeTip = MutableStateFlow<InGameTip?>(null)
    val activeTip: StateFlow<InGameTip?> = _activeTip.asStateFlow()

    private var gameLoopJob: Job? = null
    private var tipLoopJob: Job? = null
    private var slowBonusEndTime: Long = 0L
    private var doubleScoreEndTime: Long = 0L
    private var tickCounter: Int = 0
    private var tipIndex = 0
    private var currentBoardWidth = 20
    private var currentBoardHeight = 24

    init {
        // Sync sound, music & vibration managers with settings
        viewModelScope.launch {
            settings.collect { s ->
                soundManager.isSoundEnabled = s.soundEnabled
                soundManager.soundVolume = s.soundVolume
                soundManager.isVibrationEnabled = s.vibrationEnabled
                musicPlayer.updateSettings(
                    enabled = s.soundEnabled && s.musicEnabled,
                    newVolume = s.soundVolume
                )
                if (!s.tipsEnabled) {
                    _activeTip.value = null
                }
                if (_gameState.value.status == GameStatus.READY) {
                    initGame(s.difficulty)
                }
            }
        }
        initGame(settings.value.difficulty)
    }

    fun updateBoardDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (currentBoardWidth == width && currentBoardHeight == height) return
        currentBoardWidth = width
        currentBoardHeight = height
        if (_gameState.value.status == GameStatus.READY) {
            initGame(_gameState.value.difficulty)
        }
    }

    fun dismissTip() {
        _activeTip.value = null
    }

    fun cycleNextTip() {
        if (!settings.value.tipsEnabled) return
        val tips = TipRepository.ALL_TIPS
        tipIndex = (tipIndex + 1) % tips.size
        _activeTip.value = tips[tipIndex]
    }

    fun showTip(tip: InGameTip) {
        if (settings.value.tipsEnabled) {
            _activeTip.value = tip
        }
    }

    fun onAppBackgrounded() {
        musicPlayer.pause()
        if (_gameState.value.status == GameStatus.RUNNING) {
            _gameState.value = _gameState.value.copy(status = GameStatus.PAUSED)
            gameLoopJob?.cancel()
            tipLoopJob?.cancel()
        }
    }

    fun initGame(difficulty: Difficulty = settings.value.difficulty) {
        gameLoopJob?.cancel()
        musicPlayer.resetToStart()
        slowBonusEndTime = 0L
        doubleScoreEndTime = 0L
        tickCounter = 0
        val width = currentBoardWidth
        val height = currentBoardHeight

        // Initial snake centered
        val startY = height / 2
        val startX = width / 3
        val initialSnake = listOf(
            Point(startX, startY),
            Point(startX - 1, startY),
            Point(startX - 2, startY)
        )

        // Generate obstacles if difficulty requires
        val obstacles = if (difficulty.hasObstacles) {
            generateObstacles(width, height, initialSnake)
        } else {
            emptyList()
        }

        val initialSpeed = difficulty.initialTickMs

        val regularFood = spawnFood(FoodType.REGULAR, initialSnake, obstacles, width, height)

        _gameState.value = SnakeGameState(
            snake = initialSnake,
            currentDirection = Direction.RIGHT,
            pendingDirection = Direction.RIGHT,
            regularFood = regularFood,
            specialFood = null,
            obstacles = obstacles,
            score = 0,
            foodEatenCount = 0,
            comboCount = 0,
            comboExpiresAtMs = 0L,
            doubleScoreUntilMs = 0L,
            slowMotionUntilMs = 0L,
            status = GameStatus.READY,
            currentTickMs = initialSpeed,
            difficulty = difficulty,
            isNewHighScore = false,
            boardWidth = width,
            boardHeight = height
        )
    }

    fun startGame() {
        if (_gameState.value.status == GameStatus.RUNNING) return
        if (_gameState.value.status == GameStatus.GAME_OVER) {
            initGame(_gameState.value.difficulty)
        }
        _gameState.value = _gameState.value.copy(status = GameStatus.RUNNING)
        soundManager.playButtonClick()
        musicPlayer.start()
        startGameLoop()
    }

    fun pauseGame() {
        if (_gameState.value.status == GameStatus.RUNNING) {
            _gameState.value = _gameState.value.copy(status = GameStatus.PAUSED)
            gameLoopJob?.cancel()
            tipLoopJob?.cancel()
            musicPlayer.pause()
            soundManager.playButtonClick()
        }
    }

    fun resumeGame() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.value = _gameState.value.copy(status = GameStatus.RUNNING)
            soundManager.playButtonClick()
            musicPlayer.start()
            startGameLoop()
        }
    }

    fun restartGame() {
        soundManager.playButtonClick()
        initGame(_gameState.value.difficulty)
        startGame()
    }

    fun changeDirection(newDirection: Direction) {
        val state = _gameState.value
        if (state.status != GameStatus.RUNNING) {
            if (state.status == GameStatus.READY) {
                // Starting on first swipe or button press
                _gameState.value = state.copy(
                    pendingDirection = newDirection,
                    currentDirection = newDirection
                )
                startGame()
            }
            return
        }

        // Prevent 180-degree instant suicide
        if (!state.currentDirection.isOpposite(newDirection)) {
            _gameState.value = state.copy(pendingDirection = newDirection)
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        tipLoopJob?.cancel()

        if (settings.value.tipsEnabled && _activeTip.value == null) {
            _activeTip.value = TipRepository.ALL_TIPS.first()
        }

        tipLoopJob = viewModelScope.launch {
            while (isActive && _gameState.value.status == GameStatus.RUNNING) {
                delay(14000L)
                if (settings.value.tipsEnabled && _gameState.value.status == GameStatus.RUNNING) {
                    val tips = TipRepository.ALL_TIPS
                    tipIndex = (tipIndex + 1) % tips.size
                    _activeTip.value = tips[tipIndex]
                }
            }
        }

        gameLoopJob = viewModelScope.launch {
            while (isActive && _gameState.value.status == GameStatus.RUNNING) {
                val delayTime = calculateEffectiveTick()
                delay(delayTime)
                tick()
            }
        }
    }

    private fun calculateEffectiveTick(): Long {
        val state = _gameState.value
        val now = System.currentTimeMillis()
        var tick = state.currentTickMs
        // If slow potion is active, give temporary slowdown relief
        if (now < slowBonusEndTime) {
            tick = (tick + 40L).coerceAtMost(state.difficulty.initialTickMs + 40L)
        }
        return tick
    }

    private fun tick() {
        val state = _gameState.value
        if (state.status != GameStatus.RUNNING) return

        val now = System.currentTimeMillis()
        tickCounter++
        val dir = state.pendingDirection
        val head = state.snake.first()

        val rawX = head.x + dir.dx
        val rawY = head.y + dir.dy
        val crossedBoundary = rawX < 0 || rawX >= state.boardWidth || rawY < 0 || rawY >= state.boardHeight

        // Infinite borderless wrap-around: snake passes through screen boundaries seamlessly
        val newX = (rawX + state.boardWidth) % state.boardWidth
        val newY = (rawY + state.boardHeight) % state.boardHeight

        val newHead = Point(newX, newY)

        // Collision with snake body (ignore tail if moving)
        val bodyToCheck = state.snake.dropLast(1)
        if (bodyToCheck.contains(newHead)) {
            handleGameOver()
            return
        }

        // Collision with obstacles (inner walls)
        if (state.obstacles.contains(newHead)) {
            handleGameOver()
            return
        }

        if (crossedBoundary) {
            soundManager.playWallCross()
        }

        // Check food collision & active powerups
        var ateFood = false
        var pointsGained = 0
        var newRegularFood = state.regularFood
        var newSpecialFood = state.specialFood
        var newEatenEvent: EatenFoodEvent? = null
        var newComboCount = if (now <= state.comboExpiresAtMs) state.comboCount else 0
        var newComboExpiresAtMs = if (now <= state.comboExpiresAtMs) state.comboExpiresAtMs else 0L
        val feverActive = now < doubleScoreEndTime
        val feverMultiplier = if (feverActive) 2 else 1

        if (state.regularFood?.position == newHead) {
            ateFood = true
            val fType = state.regularFood.type
            newComboCount = (newComboCount + 1).coerceIn(1, 5)
            newComboExpiresAtMs = now + 4500L
            pointsGained += (fType.points * state.difficulty.scoreMultiplier * newComboCount * feverMultiplier).toInt()
            newEatenEvent = EatenFoodEvent(
                position = newHead,
                type = fType,
                pointsEarned = pointsGained,
                comboCount = newComboCount,
                isFeverActive = feverActive
            )
            soundManager.playEat(newComboCount)
            newRegularFood = spawnFood(FoodType.REGULAR, state.snake + newHead, state.obstacles, state.boardWidth, state.boardHeight)

            // Spawn special bonus food randomly (Golden star, Cyan potion, Purple berry, Rainbow gem)
            if (newSpecialFood == null && Random.nextFloat() < 0.36f) {
                val rand = Random.nextInt(4)
                val specialType = when (rand) {
                    0 -> FoodType.GOLDEN
                    1 -> FoodType.SPEED_SLOW
                    2 -> FoodType.BERRY
                    else -> FoodType.RAINBOW_GEM
                }
                newSpecialFood = spawnFood(specialType, state.snake + newHead, state.obstacles, state.boardWidth, state.boardHeight)

                when (specialType) {
                    FoodType.GOLDEN -> TipRepository.ALL_TIPS.find { it.id == "golden_star" }?.let { showTip(it) }
                    FoodType.SPEED_SLOW -> TipRepository.ALL_TIPS.find { it.id == "blue_potion" }?.let { showTip(it) }
                    FoodType.BERRY -> TipRepository.ALL_TIPS.find { it.id == "purple_berry" }?.let { showTip(it) }
                    FoodType.RAINBOW_GEM -> TipRepository.ALL_TIPS.find { it.id == "rainbow_gem" }?.let { showTip(it) }
                    else -> {}
                }
            }
        } else if (state.specialFood != null && state.specialFood.position == newHead) {
            ateFood = true
            val fType = state.specialFood.type
            newComboCount = (newComboCount + 1).coerceIn(1, 5)
            newComboExpiresAtMs = now + 4500L
            pointsGained += (fType.points * state.difficulty.scoreMultiplier * newComboCount * feverMultiplier).toInt()

            if (fType == FoodType.SPEED_SLOW) {
                slowBonusEndTime = now + 6500L
                soundManager.playBonus()
            } else if (fType == FoodType.RAINBOW_GEM) {
                doubleScoreEndTime = now + 6500L
                soundManager.playFever()
            } else {
                soundManager.playBonus()
            }

            newEatenEvent = EatenFoodEvent(
                position = newHead,
                type = fType,
                pointsEarned = pointsGained,
                comboCount = newComboCount,
                isFeverActive = (now < doubleScoreEndTime)
            )
            newSpecialFood = null
        } else {
            // Check special food expiration
            if (newSpecialFood != null && newSpecialFood.isExpired()) {
                newSpecialFood = null
            }

            // Magnetic pull during Rainbow Gem Fever mode: gently attract nearby regular food!
            if (feverActive && newRegularFood != null && tickCounter % 2 == 0) {
                val fx = newRegularFood.position.x
                val fy = newRegularFood.position.y
                val dx = newHead.x - fx
                val dy = newHead.y - fy
                val dist = kotlin.math.abs(dx) + kotlin.math.abs(dy)
                if (dist in 1..4) {
                    val stepX = if (kotlin.math.abs(dx) >= kotlin.math.abs(dy)) dx.coerceIn(-1, 1) else 0
                    val stepY = if (stepX == 0) dy.coerceIn(-1, 1) else 0
                    val pulledPos = Point(
                        (fx + stepX + state.boardWidth) % state.boardWidth,
                        (fy + stepY + state.boardHeight) % state.boardHeight
                    )
                    if (!state.snake.contains(pulledPos) && !state.obstacles.contains(pulledPos) && pulledPos != newHead) {
                        newRegularFood = newRegularFood.copy(position = pulledPos)
                    }
                }
            }
        }

        // Build new snake
        val newSnake = ArrayList<Point>(state.snake.size + 1)
        newSnake.add(newHead)
        if (ateFood) {
            newSnake.addAll(state.snake)
        } else {
            newSnake.addAll(state.snake.dropLast(1))
        }

        // Calculate dynamic speed up!
        val newFoodCount = if (ateFood) state.foodEatenCount + 1 else state.foodEatenCount
        val newScore = state.score + pointsGained

        // Dynamic speed equation: decreases tick delay progressively with eaten food
        val speedDecrement = newFoodCount * state.difficulty.speedStepMs
        val calculatedTick = (state.difficulty.initialTickMs - speedDecrement)
            .coerceAtLeast(state.difficulty.minTickMs)

        _gameState.value = state.copy(
            snake = newSnake,
            currentDirection = dir,
            regularFood = newRegularFood,
            specialFood = newSpecialFood,
            score = newScore,
            foodEatenCount = newFoodCount,
            comboCount = newComboCount,
            comboExpiresAtMs = newComboExpiresAtMs,
            doubleScoreUntilMs = doubleScoreEndTime,
            slowMotionUntilMs = slowBonusEndTime,
            lastEatenEvent = newEatenEvent ?: state.lastEatenEvent,
            currentTickMs = calculatedTick
        )
    }

    private fun handleGameOver() {
        val state = _gameState.value
        musicPlayer.pause()
        soundManager.playCrash()
        gameLoopJob?.cancel()
        tipLoopJob?.cancel()

        val bestScore = currentHighScore.value ?: 0
        val isNewRecord = state.score > bestScore && state.score > 0

        _gameState.value = state.copy(
            status = GameStatus.GAME_OVER,
            isNewHighScore = isNewRecord
        )

        // Save to Room DB asynchronously
        if (state.score > 0) {
            viewModelScope.launch {
                repository.saveScore(
                    playerName = settings.value.playerName,
                    score = state.score,
                    difficulty = state.difficulty.label,
                    snakeLength = state.snake.size
                )
            }
        }
    }

    private fun spawnFood(
        type: FoodType,
        occupiedSnake: List<Point>,
        obstacles: List<Point>,
        width: Int,
        height: Int
    ): Food {
        val occupied = (occupiedSnake + obstacles).toSet()
        val emptySlots = ArrayList<Point>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                val p = Point(x, y)
                if (!occupied.contains(p)) {
                    emptySlots.add(p)
                }
            }
        }

        val point = if (emptySlots.isNotEmpty()) {
            emptySlots[Random.nextInt(emptySlots.size)]
        } else {
            Point(0, 0)
        }

        return Food(position = point, type = type)
    }

    private fun generateObstacles(width: Int, height: Int, snake: List<Point>): List<Point> {
        val list = mutableListOf<Point>()
        val safeZone = snake.map { it.y }.toSet()

        // Place small clusters of 2-3 retro barrier blocks
        val clusterCount = 3
        for (i in 0 until clusterCount) {
            val cx = Random.nextInt(2, width - 2)
            val cy = Random.nextInt(2, height - 2)
            if (!safeZone.contains(cy)) {
                list.add(Point(cx, cy))
                list.add(Point(cx + 1, cy))
            }
        }
        return list
    }

    fun getCurrentPalette(isSystemDark: Boolean = true): MaterialPalette {
        return MaterialPalettes.getPalette(
            settings.value.materialTheme,
            settings.value.themeMode,
            isSystemDark
        )
    }

    fun clearHighScores() {
        viewModelScope.launch {
            repository.clearScores()
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayer.release()
    }
}
