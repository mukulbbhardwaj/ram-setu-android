package com.vanarsena.ramsetu.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vanarsena.ramsetu.audio.AudioEngine
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.Achievement
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanWaveFoam
import com.vanarsena.ramsetu.ui.theme.SaffronLight
import com.vanarsena.ramsetu.ui.theme.SindoorRed
import kotlin.random.Random

class GameEngine(
    private val hapticManager: HapticManager,
    private val audioEngine: AudioEngine,
    private val preferencesManager: PreferencesManager
) {
    var status by mutableStateOf(GameStatus.MENU)
        private set

    var score by mutableIntStateOf(0)
        private set

    var combo by mutableIntStateOf(0)
        private set

    var maxCombo by mutableIntStateOf(0)
        private set

    var currentTier by mutableStateOf(SpeedTiers[0])
        private set

    var bridgeProgress by mutableFloatStateOf(0f)
        private set

    var bridgeLap by mutableIntStateOf(1)
        private set

    var isNewHighScore by mutableStateOf(false)
        private set

    var countdownRemaining by mutableFloatStateOf(0f)
        private set

    var isTutorial by mutableStateOf(false)
        private set

    var missFlash by mutableFloatStateOf(0f)
        private set

    var lastUnlockedAchievement by mutableStateOf<Achievement?>(null)

    val stones = mutableStateListOf<Stone>()
    val particles = mutableStateListOf<Particle>()
    val comboPopups = mutableStateListOf<ComboPopup>()

    private var nextStoneId = 1L
    private var timeSinceLastSpawnMs = 0L
    private var lastSpawnedLane = -1
    private var physicsAccumulator = 0f

    fun startGame() {
        resetRunState()
        isTutorial = !preferencesManager.hasSeenTutorial
        countdownRemaining = COUNTDOWN_SECONDS
        status = GameStatus.COUNTDOWN
        hapticManager.playStoneTap()
        audioEngine.playStartSound()
        audioEngine.startMusic()

        if (isTutorial) {
            spawnStone(
                lane = 1,
                yProgress = 0.72f,
                fallDurationMs = 24_000L
            )
        } else {
            timeSinceLastSpawnMs = currentTier.spawnIntervalMs
        }
    }

    fun pauseGame() {
        if (status == GameStatus.PLAYING || status == GameStatus.COUNTDOWN) {
            status = GameStatus.PAUSED
            audioEngine.pauseMusic()
            persistInterruptedRun()
        }
    }

    fun resumeGame() {
        if (status == GameStatus.PAUSED) {
            countdownRemaining = COUNTDOWN_SECONDS
            status = GameStatus.COUNTDOWN
        }
    }

    fun restartGame() {
        preferencesManager.clearInterruptedRun()
        startGame()
    }

    fun exitToMenu() {
        status = GameStatus.MENU
        stones.clear()
        particles.clear()
        comboPopups.clear()
        isTutorial = false
        audioEngine.pauseMusic()
        preferencesManager.clearInterruptedRun()
    }

    fun persistInterruptedRun() {
        if (status == GameStatus.PLAYING ||
            status == GameStatus.PAUSED ||
            status == GameStatus.COUNTDOWN
        ) {
            preferencesManager.saveInterruptedRun(
                score = score,
                combo = combo,
                maxCombo = maxCombo,
                tierLevel = currentTier.level
            )
        } else {
            preferencesManager.clearInterruptedRun()
        }
    }

    fun restoreInterruptedRunIfNeeded() {
        val snapshot = preferencesManager.interruptedRun() ?: return
        resetRunState()
        score = snapshot.score
        combo = snapshot.combo
        maxCombo = snapshot.maxCombo
        currentTier = SpeedTiers.firstOrNull { it.level == snapshot.tierLevel } ?: SpeedTiers[0]
        bridgeProgress = bridgeProgressFraction(score)
        bridgeLap = computeBridgeLap(score)
        status = GameStatus.PAUSED
        preferencesManager.clearInterruptedRun()
    }

    fun update(deltaNanos: Long) {
        if (status != GameStatus.PLAYING && status != GameStatus.COUNTDOWN) return

        val frameSec = (deltaNanos / 1_000_000_000f).coerceIn(0f, 0.05f)
        physicsAccumulator += frameSec
        var steps = 0
        while (physicsAccumulator >= FIXED_STEP_SEC && steps < MAX_PHYSICS_STEPS) {
            step(FIXED_STEP_SEC)
            physicsAccumulator -= FIXED_STEP_SEC
            steps++
            if (status != GameStatus.PLAYING && status != GameStatus.COUNTDOWN) break
        }
    }

    fun onTap(
        x: Float,
        y: Float,
        screenWidth: Float,
        screenHeight: Float,
        slopPx: Float = 0f
    ): Boolean {
        if (status != GameStatus.PLAYING) return false

        val targetStone = findTappedStone(
            stones = stones,
            x = x,
            y = y,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            slopPx = slopPx
        )

        return if (targetStone != null) {
            collectStone(targetStone)
            true
        } else {
            registerMissTap()
            false
        }
    }

    fun onLaneTap(lane: Int): Boolean {
        if (status != GameStatus.PLAYING) return false
        if (lane !in 0 until LANE_COUNT) return false
        val target = findLowestStoneInLane(stones, lane)
        return if (target != null) {
            collectStone(target)
            true
        } else {
            registerMissTap()
            false
        }
    }

    private fun step(dt: Float) {
        missFlash = (missFlash - dt * 3.2f).coerceAtLeast(0f)

        if (status == GameStatus.COUNTDOWN) {
            countdownRemaining = (countdownRemaining - dt).coerceAtLeast(0f)
            if (countdownRemaining <= 0f) {
                status = GameStatus.PLAYING
                audioEngine.resumeMusic()
            }
            updateTappedStoneFade(dt)
            updateParticles(dt)
            updatePopups(dt)
            return
        }

        if (status != GameStatus.PLAYING) return

        if (!isTutorial) {
            timeSinceLastSpawnMs += (dt * 1000f).toLong()
            val tick = consumeSpawnTime(timeSinceLastSpawnMs, currentTier.spawnIntervalMs)
            timeSinceLastSpawnMs = tick.remainderMs
            repeat(tick.spawnCount) { spawnStone() }
        }

        val iterator = stones.listIterator()
        while (iterator.hasNext()) {
            val stone = iterator.next()
            if (stone.isTapped) {
                stone.alpha -= dt * 3.5f
                stone.scale += dt * 0.8f
                if (stone.alpha <= 0f) iterator.remove()
            } else {
                val fallSpeedPerSec = 1.0f / (stone.fallDurationMs / 1000f)
                stone.yProgress += fallSpeedPerSec * dt
                stone.rotation += stone.spinDegPerSec * dt
                if (!isTutorial && stone.yProgress >= MISS_Y) {
                    triggerGameOver()
                    return
                }
            }
        }

        updateParticles(dt)
        updatePopups(dt)
    }

    private fun updateTappedStoneFade(dt: Float) {
        val iterator = stones.listIterator()
        while (iterator.hasNext()) {
            val stone = iterator.next()
            if (!stone.isTapped) continue
            stone.alpha -= dt * 3.5f
            stone.scale += dt * 0.8f
            if (stone.alpha <= 0f) iterator.remove()
        }
    }

    private fun updateParticles(dt: Float) {
        val particleIterator = particles.listIterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.vy += PARTICLE_GRAVITY * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.life <= 0f) particleIterator.remove()
        }
    }

    private fun updatePopups(dt: Float) {
        val popupIterator = comboPopups.listIterator()
        while (popupIterator.hasNext()) {
            val popup = popupIterator.next()
            popup.offsetY -= dt * 60f
            popup.alpha -= dt * 1.5f
            if (popup.alpha <= 0f) popupIterator.remove()
        }
    }

    private fun collectStone(targetStone: Stone) {
        val grade = gradeHit(targetStone.yProgress)
        targetStone.isTapped = true
        score++
        combo++
        if (grade == HitGrade.PERFECT) combo++
        if (combo > maxCombo) maxCombo = combo

        bridgeProgress = bridgeProgressFraction(score)
        val lap = computeBridgeLap(score)
        val completedLap = score > 0 && score % BRIDGE_STONES_PER_LAP == 0
        if (lap != bridgeLap || completedLap) {
            bridgeLap = lap
        }

        hapticManager.playStoneTap()
        audioEngine.playStoneTap(combo)

        spawnTapParticles(lane = targetStone.lane, yProgress = targetStone.yProgress)
        comboPopups.add(
            ComboPopup(
                text = hitGradeLabel(grade),
                x = (targetStone.lane + 0.5f) / LANE_COUNT,
                y = targetStone.yProgress - 0.04f
            )
        )
        handleComboMilestone(lane = targetStone.lane, yProgress = targetStone.yProgress)
        if (completedLap) {
            hapticManager.playComboMilestone()
            comboPopups.add(
                ComboPopup(
                    text = "सेतु $bridgeLap पूर्ण!",
                    x = 0.5f,
                    y = 0.38f
                )
            )
        }
        checkSpeedTier()
        if (isTutorial) {
            isTutorial = false
            preferencesManager.hasSeenTutorial = true
            timeSinceLastSpawnMs = 0L
        }
        checkAchievements()
    }

    private fun registerMissTap() {
        if (isTutorial) return
        combo = 0
        missFlash = 1f
        hapticManager.playMiss()
    }

    private fun spawnStone(
        lane: Int = nextLane(),
        yProgress: Float = -0.15f,
        fallDurationMs: Long = currentTier.fallDurationMs
    ) {
        lastSpawnedLane = lane
        val spin = if (nextStoneId % 2L == 0L) 18f else -22f
        stones.add(
            Stone(
                id = nextStoneId++,
                lane = lane,
                yProgress = yProgress,
                rotation = Random.nextFloat() * 16f - 8f,
                fallDurationMs = fallDurationMs,
                spinDegPerSec = spin
            )
        )
    }

    private fun nextLane(): Int {
        var lane: Int
        do {
            lane = Random.nextInt(LANE_COUNT)
        } while (lane == lastSpawnedLane && Random.nextFloat() < 0.65f)
        return lane
    }

    private fun spawnTapParticles(lane: Int, yProgress: Float) {
        val laneCenter = (lane + 0.5f) / LANE_COUNT
        val particleColors = listOf(GoldAccent, SaffronLight, SindoorRed, OceanWaveFoam)

        for (i in 0 until 14) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextFloat() * 0.38f + 0.12f
            val vx = (kotlin.math.cos(angle) * speed).toFloat()
            val vy = (kotlin.math.sin(angle) * speed).toFloat()
            val life = Random.nextFloat() * 0.45f + 0.25f

            particles.add(
                Particle(
                    x = laneCenter,
                    y = yProgress,
                    vx = vx,
                    vy = vy,
                    color = particleColors.random(),
                    size = Random.nextFloat() * 6f + 4f,
                    maxLife = life,
                    life = life
                )
            )
        }
    }

    private fun handleComboMilestone(lane: Int, yProgress: Float) {
        val praise = when {
            combo == 10 -> "शानदार! (x10)"
            combo == 25 -> "अद्भुत! (x25)"
            combo == 50 -> "दिव्य सेतु! (x50)"
            combo == 100 -> "जय श्री राम! (x100)"
            combo > 0 && combo % 50 == 0 -> "महापराक्रमी! (x$combo)"
            else -> null
        }

        if (praise != null) {
            hapticManager.playComboMilestone()
            comboPopups.add(
                ComboPopup(
                    text = praise,
                    x = (lane + 0.5f) / LANE_COUNT,
                    y = yProgress - 0.05f
                )
            )
        }
    }

    private fun checkSpeedTier() {
        val nextTier = SpeedTiers.lastOrNull { score >= it.minScore } ?: SpeedTiers[0]
        if (nextTier.level > currentTier.level) {
            currentTier = nextTier
            hapticManager.playSpeedLevelUp()
            comboPopups.add(
                ComboPopup(
                    text = "गति बढ़ी: ${currentTier.label}!",
                    x = 0.5f,
                    y = 0.35f
                )
            )
        }
    }

    private fun checkAchievements() {
        val candidates = listOf(
            Achievement.FIRST_STONE to (score >= 1),
            Achievement.SCORE_TEN to (score >= 10),
            Achievement.COMBO_TEN to (maxCombo >= 10),
            Achievement.COMBO_FIFTY to (maxCombo >= 50),
            Achievement.COMBO_HUNDRED to (maxCombo >= 100),
            Achievement.SPEED_FIFTEEN to (currentTier.level >= 1),
            Achievement.SPEED_TWO to (currentTier.level >= 2),
            Achievement.SPEED_THREE to (currentTier.level >= 4),
            Achievement.TOTAL_FIVE_HUNDRED to (preferencesManager.totalStones + score >= 500)
        )
        for ((achievement, unlocked) in candidates) {
            if (unlocked && preferencesManager.unlockAchievement(achievement.id)) {
                lastUnlockedAchievement = achievement
                comboPopups.add(
                    ComboPopup(
                        text = "प्रशस्ति!",
                        x = 0.5f,
                        y = 0.28f
                    )
                )
            }
        }
    }

    private fun triggerGameOver() {
        status = GameStatus.GAME_OVER
        hapticManager.playGameOver()
        audioEngine.playGameOverSound()
        audioEngine.pauseMusic()
        isNewHighScore = preferencesManager.updateScore(score)
        if (isNewHighScore) {
            preferencesManager.unlockAchievement(Achievement.NEW_RECORD.id)
        }
        checkAchievements()
        preferencesManager.clearInterruptedRun()
    }

    private fun resetRunState() {
        stones.clear()
        particles.clear()
        comboPopups.clear()
        score = 0
        combo = 0
        maxCombo = 0
        currentTier = SpeedTiers[0]
        bridgeProgress = 0f
        bridgeLap = 1
        isNewHighScore = false
        timeSinceLastSpawnMs = 0L
        lastSpawnedLane = -1
        nextStoneId = 1L
        physicsAccumulator = 0f
        countdownRemaining = 0f
        missFlash = 0f
        lastUnlockedAchievement = null
        isTutorial = false
    }
}
