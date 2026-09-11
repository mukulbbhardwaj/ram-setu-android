package com.vanarsena.ramsetu.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.vanarsena.ramsetu.audio.AudioEngine
import com.vanarsena.ramsetu.audio.HapticManager
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

    var isNewHighScore by mutableStateOf(false)
        private set

    val stones = mutableStateListOf<Stone>()
    val particles = mutableStateListOf<Particle>()
    val comboPopups = mutableStateListOf<ComboPopup>()

    private var nextStoneId = 1L
    private var timeSinceLastSpawnMs = 0L
    private var lastSpawnedLane = -1

    fun startGame() {
        stones.clear()
        particles.clear()
        comboPopups.clear()
        score = 0
        combo = 0
        maxCombo = 0
        currentTier = SpeedTiers[0]
        bridgeProgress = 0f
        isNewHighScore = false
        timeSinceLastSpawnMs = 0L
        lastSpawnedLane = -1
        nextStoneId = 1L

        status = GameStatus.PLAYING
        hapticManager.playStoneTap()
        audioEngine.playStartSound()
        audioEngine.startMusic()
    }

    fun pauseGame() {
        if (status == GameStatus.PLAYING) {
            status = GameStatus.PAUSED
            audioEngine.pauseMusic()
        }
    }

    fun resumeGame() {
        if (status == GameStatus.PAUSED) {
            status = GameStatus.PLAYING
            audioEngine.resumeMusic()
        }
    }

    fun restartGame() {
        startGame()
    }

    fun exitToMenu() {
        status = GameStatus.MENU
        stones.clear()
        particles.clear()
        comboPopups.clear()
        audioEngine.pauseMusic()
    }

    /**
     * Called on each frame clock tick (60/120 FPS).
     */
    fun update(deltaNanos: Long) {
        if (status != GameStatus.PLAYING) return

        val deltaSec = (deltaNanos / 1_000_000_000f).coerceIn(0f, 0.05f)
        val deltaMs = (deltaNanos / 1_000_000L).coerceIn(0L, 50L)

        // 1. Spawning Stones
        timeSinceLastSpawnMs += deltaMs
        if (timeSinceLastSpawnMs >= currentTier.spawnIntervalMs) {
            spawnStone()
            timeSinceLastSpawnMs = 0L
        }

        // 2. Update Stones Movement
        val fallSpeedPerSec = 1.0f / (currentTier.fallDurationMs / 1000f)
        val iterator = stones.listIterator()

        while (iterator.hasNext()) {
            val stone = iterator.next()

            if (stone.isTapped) {
                // Animate collected stone
                stone.alpha -= deltaSec * 3.5f
                stone.scale += deltaSec * 0.8f
                if (stone.alpha <= 0f) {
                    iterator.remove()
                }
            } else {
                stone.yProgress += fallSpeedPerSec * deltaSec

                // Miss Detection: if stone passes the bottom threshold (1.05f)
                if (stone.yProgress >= 1.05f) {
                    triggerGameOver()
                    return
                }
            }
        }

        // 3. Update Particles
        val particleIterator = particles.listIterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.x += p.vx * deltaSec
            p.y += p.vy * deltaSec
            p.life -= deltaSec
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.life <= 0f) {
                particleIterator.remove()
            }
        }

        // 4. Update Combo Popups
        val popupIterator = comboPopups.listIterator()
        while (popupIterator.hasNext()) {
            val popup = popupIterator.next()
            popup.offsetY -= deltaSec * 60f
            popup.alpha -= deltaSec * 1.5f
            if (popup.alpha <= 0f) {
                popupIterator.remove()
            }
        }
    }

    /**
     * Collects a stone only if the tap lands on that stone's rectangle.
     * Empty-space taps break the combo and do not collect anything.
     */
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
            combo = 0
            hapticManager.playButtonTap()
            false
        }
    }

    private fun collectStone(targetStone: Stone) {
        targetStone.isTapped = true
        score++
        combo++
        if (combo > maxCombo) maxCombo = combo

        bridgeProgress = (score % 100) / 100f

        hapticManager.playStoneTap()
        audioEngine.playStoneTap(combo)

        spawnTapParticles(lane = targetStone.lane, yProgress = targetStone.yProgress)
        handleComboMilestone(lane = targetStone.lane, yProgress = targetStone.yProgress)
        checkSpeedTier()
    }

    private fun spawnStone() {
        var lane: Int
        do {
            lane = Random.nextInt(4)
        } while (lane == lastSpawnedLane && Random.nextFloat() < 0.65f)
        lastSpawnedLane = lane

        val stone = Stone(
            id = nextStoneId++,
            lane = lane,
            yProgress = -0.15f,
            rotation = 0f
        )
        stones.add(stone)
    }

    private fun spawnTapParticles(lane: Int, yProgress: Float) {
        val laneCenter = (lane + 0.5f) / 4f
        val particleColors = listOf(GoldAccent, SaffronLight, SindoorRed, OceanWaveFoam)

        for (i in 0 until 14) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextFloat() * 280f + 60f
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
                    x = (lane + 0.5f) / 4f,
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

    private fun triggerGameOver() {
        status = GameStatus.GAME_OVER
        hapticManager.playGameOver()
        audioEngine.playGameOverSound()
        audioEngine.pauseMusic()

        isNewHighScore = preferencesManager.updateScore(score)
    }
}
