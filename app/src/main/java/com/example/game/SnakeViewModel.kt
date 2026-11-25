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
import com.example.model.AchievementBadge
import com.example.model.AchievementCategory
import com.example.model.AchievementRepository
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.Food
import com.example.model.FoodType
import com.example.model.GameLevel
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.InGameTip
import com.example.model.LevelRepository
import com.example.model.MaterialPalette
import com.example.model.MaterialPalettes
import com.example.model.MovingObstacle
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
import kotlin.math.abs
import kotlin.random.Random

data class EatenFoodEvent(
    val position: Point,
    val type: FoodType,
    val pointsEarned: Int = 10,
    val comboCount: Int = 1,
    val isFeverActive: Boolean = false,
    val customLabel: String? = null,
    val id: Long = System.nanoTime()
)

data class SnakeGameState(
    val snake: List<Point> = emptyList(),
    val currentDirection: Direction = Direction.RIGHT,
    val pendingDirection: Direction = Direction.RIGHT,
    val regularFood: Food? = null,
    val specialFood: Food? = null,
    val obstacles: List<Point> = emptyList(),
    val movingObstacles: List<MovingObstacle> = emptyList(),
    val portalA: Point? = null,
    val portalB: Point? = null,
    val hasSolidWalls: Boolean = false,
    val gameMode: GameMode = GameMode.ENDLESS,
    val currentLevel: Int = 1,
    val targetScore: Int = 0,
    val lives: Int = 1,
    val maxLives: Int = 1,
    val timeRemainingMs: Long = 0L,
    val elapsedMs: Long = 0L,
    val score: Int = 0,
    val foodEatenCount: Int = 0,
    val bonusesCollectedCount: Int = 0,
    val coinsEarnedInRun: Int = 0,
    val starsEarnedInLevel: Int = 0,
    val comboCount: Int = 0,
    val comboExpiresAtMs: Long = 0L,
    val doubleScoreUntilMs: Long = 0L,
    val slowMotionUntilMs: Long = 0L,
    val shieldUntilMs: Long = 0L,
    val magnetUntilMs: Long = 0L,
    val energyUntilMs: Long = 0L,
    val lastEatenEvent: EatenFoodEvent? = null,
    val sessionUnlockedBadges: List<AchievementBadge> = emptyList(),
    val status: GameStatus = GameStatus.READY,
    val currentTickMs: Long = 165L,
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

    val topScores: StateFlow<List<HighScoreEntity>> = repository.getTopScores(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topScoresForGameOver: StateFlow<List<HighScoreEntity>> = repository.getTopScores(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentHighScore: StateFlow<Int?> = repository.getOverallHighScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _activeTip = MutableStateFlow<InGameTip?>(null)
    val activeTip: StateFlow<InGameTip?> = _activeTip.asStateFlow()

    private val _newlyUnlockedBadge = MutableStateFlow<AchievementBadge?>(null)
    val newlyUnlockedBadge: StateFlow<AchievementBadge?> = _newlyUnlockedBadge.asStateFlow()

    private var gameLoopJob: Job? = null
    private var tipLoopJob: Job? = null
    private var timerLoopJob: Job? = null
    private var achievementBannerJob: Job? = null

    private val directionQueue = ArrayDeque<Direction>()
    private var slowBonusEndTime: Long = 0L
    private var doubleScoreEndTime: Long = 0L
    private var shieldEndTime: Long = 0L
    private var magnetEndTime: Long = 0L
    private var energyEndTime: Long = 0L
    private var lastTickTimeMs: Long = 0L
    private var tickCounter: Int = 0
    private var tipIndex = 0
    private var currentBoardWidth = 20
    private var currentBoardHeight = 24
    private var goldenCollectedInRun = 0
    private var tookDamageInRun = false
    private var runFinalized = false

    init {
        viewModelScope.launch {
            settings.collect { s ->
                soundManager.isSoundEnabled = s.soundEnabled
                soundManager.soundVolume = s.soundVolume
                soundManager.isVibrationEnabled = s.vibrationEnabled
                musicPlayer.updateSettings(
                    enabled = s.musicEnabled,
                    newVolume = s.musicVolume,
                    style = s.musicStyle
                )
                if (!s.tipsEnabled) {
                    _activeTip.value = null
                }
            }
        }
        initGame(
            mode = settings.value.selectedGameMode,
            levelNumber = settings.value.selectedLevel,
            difficulty = settings.value.difficulty
        )
    }

    fun updateBoardDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (currentBoardWidth == width && currentBoardHeight == height) return
        currentBoardWidth = width
        currentBoardHeight = height
        if (_gameState.value.status == GameStatus.READY) {
            val st = _gameState.value
            initGame(st.gameMode, st.currentLevel, st.difficulty)
        }
    }

    fun dismissTip() {
        _activeTip.value = null
    }

    fun dismissAchievementBanner() {
        achievementBannerJob?.cancel()
        _newlyUnlockedBadge.value = null
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
            cancelLoops()
        }
    }

    fun launchMode(mode: GameMode, levelNumber: Int = settings.value.selectedLevel) {
        settingsManager.setSelectedGameMode(mode)
        if (mode == GameMode.LEVELS) {
            settingsManager.setSelectedLevel(levelNumber)
        }
        initGame(mode = mode, levelNumber = levelNumber, difficulty = settings.value.difficulty)
    }

    fun initGame(
        mode: GameMode = settings.value.selectedGameMode,
        levelNumber: Int = settings.value.selectedLevel,
        difficulty: Difficulty = settings.value.difficulty
    ) {
        cancelLoops()
        musicPlayer.resetToStart()
        directionQueue.clear()
        slowBonusEndTime = 0L
        doubleScoreEndTime = 0L
        shieldEndTime = 0L
        magnetEndTime = 0L
        energyEndTime = 0L
        tickCounter = 0
        goldenCollectedInRun = 0
        tookDamageInRun = false
        runFinalized = false

        val width = currentBoardWidth
        val height = currentBoardHeight
        val startY = height / 2
        val startX = (width / 3).coerceAtLeast(4)
        val initialSnake = listOf(
            Point(startX, startY),
            Point(startX - 1, startY),
            Point(startX - 2, startY)
        )

        val safeZone = buildSet {
            addAll(initialSnake)
            for (dx in 1..4) add(Point(startX + dx, startY))
            add(Point(startX, startY - 1))
            add(Point(startX, startY + 1))
        }

        val levelSpec: GameLevel? = if (mode == GameMode.LEVELS) LevelRepository.getLevel(levelNumber) else null

        val obstacles: List<Point> = when {
            levelSpec != null -> LevelRepository.generateObstaclesForLevel(levelSpec, width, height, safeZone)
            mode == GameMode.OBSTACLE || mode == GameMode.SURVIVAL || mode == GameMode.CHALLENGE ->
                generateModeObstacles(width, height, safeZone, if (mode == GameMode.OBSTACLE) 6 else 4)
            difficulty == Difficulty.HARD || difficulty == Difficulty.INSANE ->
                generateModeObstacles(width, height, safeZone, 3)
            else -> emptyList()
        }

        val movingCount = when {
            levelSpec != null -> levelSpec.movingObstacleCount
            mode == GameMode.OBSTACLE -> 3
            else -> 0
        }
        val movingObstacles = generateMovingObstacles(movingCount, width, height, safeZone + obstacles)

        val hasPortals = levelSpec?.hasPortals == true || mode == GameMode.PORTAL
        val (portalA, portalB) = if (hasPortals) {
            placePortals(width, height, safeZone + obstacles + movingObstacles.map { it.position })
        } else {
            Pair(null, null)
        }

        val hasWalls = levelSpec?.hasWalls ?: mode.hasSolidWalls
        val initialTick = levelSpec?.initialTickMs ?: difficulty.initialTickMs
        val targetScore = when {
            levelSpec != null -> levelSpec.targetScore
            mode == GameMode.CHALLENGE -> (280 * difficulty.scoreMultiplier).toInt()
            else -> 0
        }
        val maxLives = if (mode == GameMode.SURVIVAL) 3 else 1
        val timeLimitMs = when (mode) {
            GameMode.TIME_ATTACK -> 60_000L
            GameMode.CHALLENGE -> 75_000L
            else -> 0L
        }

        val occupied = (initialSnake + obstacles + movingObstacles.map { it.position } + listOfNotNull(portalA, portalB)).toSet()
        val regularFood = spawnFood(FoodType.REGULAR, occupied, width, height)

        _gameState.value = SnakeGameState(
            snake = initialSnake,
            currentDirection = Direction.RIGHT,
            pendingDirection = Direction.RIGHT,
            regularFood = regularFood,
            specialFood = null,
            obstacles = obstacles,
            movingObstacles = movingObstacles,
            portalA = portalA,
            portalB = portalB,
            hasSolidWalls = hasWalls,
            gameMode = mode,
            currentLevel = levelNumber,
            targetScore = targetScore,
            lives = maxLives,
            maxLives = maxLives,
            timeRemainingMs = timeLimitMs,
            elapsedMs = 0L,
            score = 0,
            foodEatenCount = 0,
            bonusesCollectedCount = 0,
            coinsEarnedInRun = 0,
            starsEarnedInLevel = 0,
            comboCount = 0,
            comboExpiresAtMs = 0L,
            doubleScoreUntilMs = 0L,
            slowMotionUntilMs = 0L,
            shieldUntilMs = 0L,
            magnetUntilMs = 0L,
            energyUntilMs = 0L,
            sessionUnlockedBadges = emptyList(),
            status = GameStatus.READY,
            currentTickMs = initialTick,
            difficulty = difficulty,
            isNewHighScore = false,
            boardWidth = width,
            boardHeight = height
        )
    }

    fun startGame() {
        if (_gameState.value.status == GameStatus.RUNNING) return
        if (_gameState.value.status == GameStatus.GAME_OVER || _gameState.value.status == GameStatus.LEVEL_COMPLETE) {
            val st = _gameState.value
            initGame(st.gameMode, st.currentLevel, st.difficulty)
        }
        _gameState.value = _gameState.value.copy(status = GameStatus.RUNNING)
        lastTickTimeMs = System.currentTimeMillis()
        soundManager.playButtonClick()
        musicPlayer.start()
        startLoops()
    }

    fun pauseGame() {
        if (_gameState.value.status == GameStatus.RUNNING) {
            _gameState.value = _gameState.value.copy(status = GameStatus.PAUSED)
            cancelLoops()
            musicPlayer.pause()
            soundManager.playButtonClick()
        }
    }

    fun resumeGame() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.value = _gameState.value.copy(status = GameStatus.RUNNING)
            lastTickTimeMs = System.currentTimeMillis()
            soundManager.playButtonClick()
            musicPlayer.start()
            startLoops()
        }
    }

    fun restartGame() {
        soundManager.playButtonClick()
        val st = _gameState.value
        initGame(st.gameMode, st.currentLevel, st.difficulty)
        startGame()
    }

    fun startNextLevel() {
        soundManager.playButtonClick()
        val next = (_gameState.value.currentLevel + 1).coerceAtMost(100)
        settingsManager.setSelectedLevel(next)
        initGame(GameMode.LEVELS, next, settings.value.difficulty)
        startGame()
    }

    fun changeDirection(newDirection: Direction) {
        val state = _gameState.value
        if (state.status != GameStatus.RUNNING) {
            if (state.status == GameStatus.READY && !Direction.RIGHT.isOpposite(newDirection)) {
                directionQueue.clear()
                directionQueue.addLast(newDirection)
                _gameState.value = state.copy(
                    pendingDirection = newDirection,
                    currentDirection = newDirection
                )
                startGame()
            }
            return
        }

        val lastPlanned = directionQueue.lastOrNull() ?: state.currentDirection
        if (newDirection != lastPlanned && !lastPlanned.isOpposite(newDirection) && directionQueue.size < 2) {
            directionQueue.addLast(newDirection)
            _gameState.value = state.copy(pendingDirection = directionQueue.first())
        }
    }

    private fun cancelLoops() {
        gameLoopJob?.cancel()
        tipLoopJob?.cancel()
        timerLoopJob?.cancel()
    }

    private fun startLoops() {
        cancelLoops()

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

        timerLoopJob = viewModelScope.launch {
            var lastStamp = System.currentTimeMillis()
            while (isActive && _gameState.value.status == GameStatus.RUNNING) {
                delay(200L)
                val now = System.currentTimeMillis()
                val delta = (now - lastStamp).coerceIn(0L, 1000L)
                lastStamp = now

                val st = _gameState.value
                if (st.status != GameStatus.RUNNING) break
                val newElapsed = st.elapsedMs + delta
                if (st.gameMode == GameMode.TIME_ATTACK || st.gameMode == GameMode.CHALLENGE) {
                    val remaining = (st.timeRemainingMs - delta).coerceAtLeast(0L)
                    _gameState.value = st.copy(elapsedMs = newElapsed, timeRemainingMs = remaining)
                    if (remaining <= 0L) {
                        handleGameOver()
                        break
                    }
                } else {
                    _gameState.value = st.copy(elapsedMs = newElapsed)
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
        if (now < energyEndTime) {
            tick = (tick * 0.76f).toLong().coerceAtLeast(36L)
        }
        if (now < slowBonusEndTime) {
            tick = (tick + 45L).coerceAtMost(state.difficulty.initialTickMs + 45L)
        }
        return tick
    }

    private fun tick() {
        val state = _gameState.value
        if (state.status != GameStatus.RUNNING) return

        val now = System.currentTimeMillis()
        tickCounter++

        val dir = if (directionQueue.isNotEmpty()) directionQueue.removeFirst() else state.pendingDirection
        val head = state.snake.first()

        var rawX = head.x + dir.dx
        var rawY = head.y + dir.dy
        val hitOuterWall = rawX < 0 || rawX >= state.boardWidth || rawY < 0 || rawY >= state.boardHeight

        if (hitOuterWall && state.hasSolidWalls) {
            if (handleCollisionOrSurvive(state, now, hitWall = true)) return
            // Survived via Shield or Extra Life -> wrap cleanly or bounce safely
            rawX = (rawX + state.boardWidth) % state.boardWidth
            rawY = (rawY + state.boardHeight) % state.boardHeight
        } else if (hitOuterWall) {
            soundManager.playWallCross()
        }

        var newX = (rawX + state.boardWidth) % state.boardWidth
        var newY = (rawY + state.boardHeight) % state.boardHeight
        var newHead = Point(newX, newY)

        // Portal Teleportation
        if (state.portalA != null && state.portalB != null) {
            if (newHead == state.portalA) {
                newHead = Point(
                    (state.portalB.x + dir.dx + state.boardWidth) % state.boardWidth,
                    (state.portalB.y + dir.dy + state.boardHeight) % state.boardHeight
                )
                soundManager.playPortal()
            } else if (newHead == state.portalB) {
                newHead = Point(
                    (state.portalA.x + dir.dx + state.boardWidth) % state.boardWidth,
                    (state.portalA.y + dir.dy + state.boardHeight) % state.boardHeight
                )
                soundManager.playPortal()
            }
        }

        // Move dynamic patrol obstacles in Obstacle Mode / higher levels
        val updatedMovingObstacles = if (state.movingObstacles.isNotEmpty() && tickCounter % 4 == 0) {
            movePatrolObstacles(state.movingObstacles, state.obstacles, state.snake, state.boardWidth, state.boardHeight)
        } else {
            state.movingObstacles
        }

        val bodyToCheck = state.snake.dropLast(1)
        val hitSelf = bodyToCheck.contains(newHead)
        val hitObstacle = state.obstacles.contains(newHead) || updatedMovingObstacles.any { it.position == newHead }

        if (hitSelf || hitObstacle) {
            if (handleCollisionOrSurvive(state, now, hitWall = false)) return
        }

        // Check food collision & active powerups
        var ateFood = false
        var pointsGained = 0
        var bonusCoinsGained = 0
        var bonusTimeMs = 0L
        var newBonusesCount = state.bonusesCollectedCount
        var newRegularFood = state.regularFood
        var newSpecialFood = state.specialFood
        var newEatenEvent: EatenFoodEvent? = null
        var newComboCount = if (now <= state.comboExpiresAtMs) state.comboCount else 0
        var newComboExpiresAtMs = if (now <= state.comboExpiresAtMs) state.comboExpiresAtMs else 0L

        val doubleActive = now < doubleScoreEndTime
        val energyActive = now < energyEndTime
        val magnetActive = now < magnetEndTime || doubleActive
        val scoreMult = (if (doubleActive) 2f else 1f) * (if (energyActive) 1.5f else 1f)

        val occupiedAll = (state.snake + newHead + state.obstacles + updatedMovingObstacles.map { it.position } +
            listOfNotNull(state.portalA, state.portalB)).toSet()

        if (state.regularFood?.position == newHead) {
            ateFood = true
            val fType = state.regularFood.type
            newComboCount = (newComboCount + 1).coerceIn(1, 5)
            newComboExpiresAtMs = now + 4500L
            pointsGained += (fType.points * state.difficulty.scoreMultiplier * newComboCount * scoreMult).toInt()
            newEatenEvent = EatenFoodEvent(
                position = newHead,
                type = fType,
                pointsEarned = pointsGained,
                comboCount = newComboCount,
                isFeverActive = doubleActive
            )
            soundManager.playEat(newComboCount)
            newRegularFood = spawnFood(FoodType.REGULAR, occupiedAll, state.boardWidth, state.boardHeight)

            // Spawn one of the 8 special bonus/power-up items
            if (newSpecialFood == null && Random.nextFloat() < 0.42f) {
                val specialTypes = listOf(
                    FoodType.GOLDEN,
                    FoodType.DIAMOND,
                    FoodType.ENERGY_ORB,
                    FoodType.SHIELD,
                    FoodType.SPEED_SLOW,
                    FoodType.MAGNET,
                    FoodType.DOUBLE_SCORE,
                    FoodType.MYSTERY_BOX
                )
                val specialType = specialTypes[Random.nextInt(specialTypes.size)]
                newSpecialFood = spawnFood(specialType, occupiedAll + newRegularFood.position, state.boardWidth, state.boardHeight)
            }
        } else if (state.specialFood != null && state.specialFood.position == newHead) {
            ateFood = true
            newBonusesCount++
            val fType = state.specialFood.type
            newComboCount = (newComboCount + 1).coerceIn(1, 5)
            newComboExpiresAtMs = now + 4800L
            var customLabel: String? = null

            when (fType) {
                FoodType.GOLDEN -> {
                    goldenCollectedInRun++
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * newComboCount * scoreMult).toInt()
                    bonusCoinsGained += 2
                    soundManager.playBonus()
                }
                FoodType.DIAMOND -> {
                    goldenCollectedInRun++
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * newComboCount * scoreMult).toInt()
                    bonusCoinsGained += 5
                    customLabel = "+$pointsGained • +5 🪙"
                    soundManager.playFever()
                }
                FoodType.ENERGY_ORB -> {
                    energyEndTime = now + 6000L
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * scoreMult).toInt()
                    customLabel = "⚡ HIZ & 1.5X!"
                    soundManager.playBonus()
                }
                FoodType.SHIELD -> {
                    shieldEndTime = now + 10000L
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * scoreMult).toInt()
                    customLabel = "🛡️ KALKAN!"
                    soundManager.playBonus()
                }
                FoodType.SPEED_SLOW -> {
                    slowBonusEndTime = now + 6500L
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * scoreMult).toInt()
                    customLabel = "❄️ YAVAŞLATMA!"
                    soundManager.playBonus()
                }
                FoodType.MAGNET -> {
                    magnetEndTime = now + 8000L
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * scoreMult).toInt()
                    customLabel = "🧲 MIKNATIS!"
                    soundManager.playBonus()
                }
                FoodType.DOUBLE_SCORE -> {
                    doubleScoreEndTime = now + 8000L
                    pointsGained += (fType.points * state.difficulty.scoreMultiplier * 2f).toInt()
                    customLabel = "💎 2X PUAN!"
                    soundManager.playFever()
                }
                FoodType.MYSTERY_BOX -> {
                    when (Random.nextInt(4)) {
                        0 -> {
                            pointsGained += 90
                            bonusCoinsGained += 8
                            customLabel = "🎁 +90 & +8 🪙!"
                        }
                        1 -> {
                            shieldEndTime = now + 9000L
                            doubleScoreEndTime = now + 7000L
                            pointsGained += 40
                            customLabel = "🎁 KALKAN + 2X!"
                        }
                        2 -> {
                            magnetEndTime = now + 8000L
                            energyEndTime = now + 6000L
                            pointsGained += 40
                            customLabel = "🎁 MIKNATIS + HIZ!"
                        }
                        else -> {
                            bonusTimeMs += 8000L
                            pointsGained += 60
                            customLabel = if (state.gameMode == GameMode.TIME_ATTACK || state.gameMode == GameMode.CHALLENGE) {
                                "🎁 +8 SANİYE!"
                            } else {
                                "🎁 MEGA +60!"
                            }
                        }
                    }
                    soundManager.playFever()
                }
                FoodType.REGULAR -> {}
            }

            if (state.gameMode == GameMode.TIME_ATTACK) {
                bonusTimeMs += 3000L
            }

            newEatenEvent = EatenFoodEvent(
                position = newHead,
                type = fType,
                pointsEarned = pointsGained,
                comboCount = newComboCount,
                isFeverActive = (now < doubleScoreEndTime),
                customLabel = customLabel
            )
            newSpecialFood = null
        } else {
            if (newSpecialFood != null && newSpecialFood.isExpired()) {
                newSpecialFood = null
            }

            // Magnetic pull: attract regular food toward snake head within radius 5
            if (magnetActive && newRegularFood != null && tickCounter % 2 == 0) {
                val fx = newRegularFood.position.x
                val fy = newRegularFood.position.y
                val dx = newHead.x - fx
                val dy = newHead.y - fy
                val dist = abs(dx) + abs(dy)
                if (dist in 1..5) {
                    val stepX = if (abs(dx) >= abs(dy)) dx.coerceIn(-1, 1) else 0
                    val stepY = if (stepX == 0) dy.coerceIn(-1, 1) else 0
                    val pulledPos = Point(
                        (fx + stepX + state.boardWidth) % state.boardWidth,
                        (fy + stepY + state.boardHeight) % state.boardHeight
                    )
                    if (!occupiedAll.contains(pulledPos) && pulledPos != newHead) {
                        newRegularFood = newRegularFood.copy(position = pulledPos)
                    }
                }
            }
        }

        if (bonusCoinsGained > 0) {
            settingsManager.addCoins(bonusCoinsGained)
        }

        val newSnake = ArrayList<Point>(state.snake.size + 1)
        newSnake.add(newHead)
        if (ateFood) {
            newSnake.addAll(state.snake)
        } else {
            newSnake.addAll(state.snake.dropLast(1))
        }

        val newFoodCount = if (ateFood) state.foodEatenCount + 1 else state.foodEatenCount
        val newScore = state.score + pointsGained
        val newTimeRemaining = if (bonusTimeMs > 0L) state.timeRemainingMs + bonusTimeMs else state.timeRemainingMs

        val baseInitialTick = if (state.gameMode == GameMode.LEVELS) {
            LevelRepository.getLevel(state.currentLevel).initialTickMs
        } else {
            state.difficulty.initialTickMs
        }
        val baseMinTick = if (state.gameMode == GameMode.LEVELS) {
            LevelRepository.getLevel(state.currentLevel).minTickMs
        } else {
            state.difficulty.minTickMs
        }
        val calculatedTick = (baseInitialTick - newFoodCount * state.difficulty.speedStepMs)
            .coerceAtLeast(baseMinTick)

        val newlyEarnedBadges = evaluateRealtimeAchievements(newScore, newFoodCount)
        val updatedSessionBadges = if (newlyEarnedBadges.isEmpty()) {
            state.sessionUnlockedBadges
        } else {
            (state.sessionUnlockedBadges + newlyEarnedBadges).distinctBy { it.id }
        }

        val updatedState = state.copy(
            snake = newSnake,
            currentDirection = dir,
            regularFood = newRegularFood,
            specialFood = newSpecialFood,
            movingObstacles = updatedMovingObstacles,
            timeRemainingMs = newTimeRemaining,
            score = newScore,
            foodEatenCount = newFoodCount,
            bonusesCollectedCount = newBonusesCount,
            coinsEarnedInRun = state.coinsEarnedInRun + bonusCoinsGained,
            comboCount = newComboCount,
            comboExpiresAtMs = newComboExpiresAtMs,
            doubleScoreUntilMs = doubleScoreEndTime,
            slowMotionUntilMs = slowBonusEndTime,
            shieldUntilMs = shieldEndTime,
            magnetUntilMs = magnetEndTime,
            energyUntilMs = energyEndTime,
            lastEatenEvent = newEatenEvent ?: state.lastEatenEvent,
            sessionUnlockedBadges = updatedSessionBadges,
            currentTickMs = calculatedTick
        )
        _gameState.value = updatedState

        // Check Level or Challenge victory target!
        if ((state.gameMode == GameMode.LEVELS || state.gameMode == GameMode.CHALLENGE) &&
            state.targetScore > 0 &&
            newScore >= state.targetScore
        ) {
            handleLevelOrChallengeVictory(updatedState)
        }
    }

    /**
     * Returns true if the game ended (or skipped step safely); false if Shield/Life absorbed the hit.
     */
    private fun handleCollisionOrSurvive(state: SnakeGameState, now: Long, hitWall: Boolean): Boolean {
        if (now < shieldEndTime) {
            // Shield absorbs collision!
            shieldEndTime = now + 1200L // brief post-break grace window
            tookDamageInRun = true
            soundManager.playShieldBreak()
            _gameState.value = state.copy(shieldUntilMs = shieldEndTime)
            if (hitWall) return false
            // If hitting an internal obstacle or self, turn snake safely or stop current tick
            return true
        }
        if (state.lives > 1) {
            val remainingLives = state.lives - 1
            tookDamageInRun = true
            shieldEndTime = now + 2200L // brief invulnerability grace after losing 1 life
            soundManager.playShieldBreak()
            _gameState.value = state.copy(
                lives = remainingLives,
                shieldUntilMs = shieldEndTime
            )
            return true
        }
        handleGameOver()
        return true
    }

    private fun handleLevelOrChallengeVictory(state: SnakeGameState) {
        if (runFinalized) return
        runFinalized = true
        cancelLoops()
        musicPlayer.pause()
        soundManager.playLevelComplete()

        var coinsWon = state.coinsEarnedInRun
        var stars = 3
        val badgeList = state.sessionUnlockedBadges.toMutableList()

        if (state.gameMode == GameMode.LEVELS) {
            val level = LevelRepository.getLevel(state.currentLevel)
            stars = when {
                !tookDamageInRun && state.elapsedMs < 75_000L -> 3
                !tookDamageInRun -> 2
                else -> 1
            }
            val awarded = settingsManager.recordLevelCompletion(level.number, stars, level.rewardCoins)
            coinsWon += awarded
        } else {
            // Challenge Mode reward
            val challengeBonus = 75
            settingsManager.addCoins(challengeBonus)
            coinsWon += challengeBonus
        }

        finalizeRunPersistence(state, isVictory = true, extraBadges = badgeList)

        val bestScore = currentHighScore.value ?: 0
        val isNewRecord = state.score > bestScore && state.score > 0
        _gameState.value = state.copy(
            status = GameStatus.LEVEL_COMPLETE,
            coinsEarnedInRun = coinsWon,
            starsEarnedInLevel = stars,
            isNewHighScore = isNewRecord,
            sessionUnlockedBadges = badgeList.distinctBy { it.id }
        )
    }

    private fun handleGameOver() {
        if (runFinalized) return
        runFinalized = true
        val state = _gameState.value
        cancelLoops()
        musicPlayer.pause()
        soundManager.playCrash()

        val participationCoins = (state.score / 25).coerceAtLeast(if (state.foodEatenCount > 0) 2 else 0)
        if (participationCoins > 0) {
            settingsManager.addCoins(participationCoins)
        }

        val badgeList = state.sessionUnlockedBadges.toMutableList()
        finalizeRunPersistence(state, isVictory = false, extraBadges = badgeList)

        val bestScore = currentHighScore.value ?: 0
        val isNewRecord = state.score > bestScore && state.score > 0

        _gameState.value = state.copy(
            status = GameStatus.GAME_OVER,
            coinsEarnedInRun = state.coinsEarnedInRun + participationCoins,
            isNewHighScore = isNewRecord,
            sessionUnlockedBadges = badgeList.distinctBy { it.id }
        )
    }

    private fun finalizeRunPersistence(
        state: SnakeGameState,
        isVictory: Boolean,
        extraBadges: MutableList<AchievementBadge>
    ) {
        val elapsedSec = (state.elapsedMs / 1000L).coerceAtLeast(1L)
        settingsManager.recordRunStats(
            score = state.score,
            foodsEaten = state.foodEatenCount,
            bonusesCollected = state.bonusesCollectedCount,
            goldenCollectedCount = goldenCollectedInRun,
            survivalSeconds = elapsedSec
        )

        val s = settings.value
        val alreadyUnlocked = s.unlockedAchievements
        val earnedNow = AchievementRepository.ALL_ACHIEVEMENTS.filter { badge ->
            if (alreadyUnlocked.contains(badge.id)) return@filter false
            when (badge.category) {
                AchievementCategory.FIRST_FOOD -> s.totalFoodsEaten >= badge.requiredValue
                AchievementCategory.SCORE -> state.score >= badge.requiredValue
                AchievementCategory.LEVELS -> s.completedLevelsCount >= badge.requiredValue
                AchievementCategory.SURVIVAL_TIME -> s.totalSurvivalSeconds >= badge.requiredValue
                AchievementCategory.GOLDEN_COLLECTOR -> s.goldenCollected >= badge.requiredValue
                AchievementCategory.PERFECT_RUN -> isVictory && !tookDamageInRun
            }
        }
        if (earnedNow.isNotEmpty()) {
            val bonusCoins = earnedNow.sumOf { it.rewardCoins }
            settingsManager.unlockAchievements(earnedNow.map { it.id }, bonusCoins)
            extraBadges.addAll(earnedNow)
            showUnlockedBadgeBanner(earnedNow.last())
        }

        if (state.score > 0) {
            viewModelScope.launch {
                repository.saveScore(
                    playerName = settings.value.playerName,
                    score = state.score,
                    difficulty = state.difficulty.label,
                    snakeLength = state.snake.size,
                    gameMode = state.gameMode.id,
                    durationSec = elapsedSec.toInt()
                )
            }
        }
    }

    private fun evaluateRealtimeAchievements(newScore: Int, foodCount: Int): List<AchievementBadge> {
        val alreadyUnlocked = settings.value.unlockedAchievements
        val newlyEarned = AchievementRepository.ALL_ACHIEVEMENTS.filter { badge ->
            if (alreadyUnlocked.contains(badge.id)) return@filter false
            when (badge.category) {
                AchievementCategory.FIRST_FOOD -> foodCount >= badge.requiredValue
                AchievementCategory.SCORE -> newScore >= badge.requiredValue
                else -> false
            }
        }
        if (newlyEarned.isNotEmpty()) {
            val bonusCoins = newlyEarned.sumOf { it.rewardCoins }
            settingsManager.unlockAchievements(newlyEarned.map { it.id }, bonusCoins)
            soundManager.playFever()
            showUnlockedBadgeBanner(newlyEarned.last())
        }
        return newlyEarned
    }

    private fun showUnlockedBadgeBanner(badge: AchievementBadge) {
        _newlyUnlockedBadge.value = badge
        achievementBannerJob?.cancel()
        achievementBannerJob = viewModelScope.launch {
            delay(3800L)
            _newlyUnlockedBadge.value = null
        }
    }

    private fun spawnFood(
        type: FoodType,
        occupied: Set<Point>,
        width: Int,
        height: Int
    ): Food {
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

    private fun generateModeObstacles(width: Int, height: Int, safeZone: Set<Point>, clusterCount: Int): List<Point> {
        val list = linkedSetOf<Point>()
        var attempts = 0
        while (list.size < clusterCount * 2 && attempts < 60) {
            attempts++
            val cx = Random.nextInt(2, (width - 3).coerceAtLeast(3))
            val cy = Random.nextInt(2, (height - 3).coerceAtLeast(3))
            val p1 = Point(cx, cy)
            val p2 = Point(cx + 1, cy)
            if (!safeZone.contains(p1) && !safeZone.contains(p2)) {
                list.add(p1)
                list.add(p2)
            }
        }
        return list.toList()
    }

    private fun generateMovingObstacles(
        count: Int,
        width: Int,
        height: Int,
        occupied: Set<Point>
    ): List<MovingObstacle> {
        if (count <= 0) return emptyList()
        val list = mutableListOf<MovingObstacle>()
        var attempts = 0
        while (list.size < count && attempts < 80) {
            attempts++
            val x = Random.nextInt(2, (width - 2).coerceAtLeast(3))
            val y = Random.nextInt(2, (height - 2).coerceAtLeast(3))
            val p = Point(x, y)
            if (!occupied.contains(p) && list.none { it.position == p }) {
                val horizontal = list.size % 2 == 0
                list.add(
                    MovingObstacle(
                        position = p,
                        dx = if (horizontal) 1 else 0,
                        dy = if (horizontal) 0 else 1
                    )
                )
            }
        }
        return list
    }

    private fun movePatrolObstacles(
        movers: List<MovingObstacle>,
        staticObs: List<Point>,
        snake: List<Point>,
        width: Int,
        height: Int
    ): List<MovingObstacle> {
        val blocked = (staticObs + snake).toSet()
        return movers.map { m ->
            var nextPos = Point(m.position.x + m.dx, m.position.y + m.dy)
            var ndx = m.dx
            var ndy = m.dy
            if (nextPos.x !in 1 until (width - 1) || nextPos.y !in 1 until (height - 1) || blocked.contains(nextPos)) {
                ndx = -m.dx
                ndy = -m.dy
                nextPos = Point(m.position.x + ndx, m.position.y + ndy)
                if (nextPos.x !in 1 until (width - 1) || nextPos.y !in 1 until (height - 1) || blocked.contains(nextPos)) {
                    nextPos = m.position
                }
            }
            m.copy(position = nextPos, dx = ndx, dy = ndy)
        }
    }

    private fun placePortals(width: Int, height: Int, occupied: Set<Point>): Pair<Point, Point> {
        val candidatesA = listOf(Point(3, 4), Point(4, 3), Point(3, height / 3))
        val candidatesB = listOf(Point(width - 4, height - 5), Point(width - 5, height - 4), Point(width - 4, (height * 2) / 3))
        val a = candidatesA.firstOrNull { !occupied.contains(it) } ?: Point(2, 2)
        val b = candidatesB.firstOrNull { !occupied.contains(it) && it != a } ?: Point(width - 3, height - 3)
        return Pair(a, b)
    }

    fun getCurrentPalette(isSystemDark: Boolean = true): MaterialPalette =
        MaterialPalettes.getPalette(settings.value.materialTheme, settings.value.themeMode, isSystemDark)

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
