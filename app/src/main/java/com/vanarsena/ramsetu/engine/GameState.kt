package com.vanarsena.ramsetu.engine

import androidx.compose.ui.graphics.Color

enum class GameStatus {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class SpeedTier(
    val level: Int,
    val minScore: Int,
    val spawnIntervalMs: Long,
    val fallDurationMs: Long,
    val label: String
)

val SpeedTiers = listOf(
    SpeedTier(level = 0, minScore = 0, spawnIntervalMs = 1000L, fallDurationMs = 3400L, label = "1.0x"),
    SpeedTier(level = 1, minScore = 70, spawnIntervalMs = 850L, fallDurationMs = 2900L, label = "1.5x"),
    SpeedTier(level = 2, minScore = 150, spawnIntervalMs = 720L, fallDurationMs = 2400L, label = "2.0x"),
    SpeedTier(level = 3, minScore = 400, spawnIntervalMs = 600L, fallDurationMs = 2000L, label = "2.5x"),
    SpeedTier(level = 4, minScore = 800, spawnIntervalMs = 500L, fallDurationMs = 1650L, label = "3.0x")
)

data class Stone(
    val id: Long,
    val lane: Int,            // 0, 1, 2, 3
    var yProgress: Float = 0f, // 0f (top) to 1.15f (bottom boundary)
    var isTapped: Boolean = false,
    var alpha: Float = 1f,
    var scale: Float = 1f,
    var rotation: Float = 0f
)

/** Shared visual size so rendering and hit-testing stay in lockstep. */
object StoneVisual {
    /** Tablet does not fill the lane — matches the web floating-stone size. */
    const val WIDTH_FRACTION_OF_LANE = 0.62f
    /** Native big_rock.png aspect (1998 / 1160). */
    const val HEIGHT_TO_WIDTH = 1.722f
}

data class StoneBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom

    fun centerX(): Float = (left + right) * 0.5f
    fun centerY(): Float = (top + bottom) * 0.5f
}

fun stoneSize(laneWidth: Float): Pair<Float, Float> {
    val width = laneWidth * StoneVisual.WIDTH_FRACTION_OF_LANE
    val height = width * StoneVisual.HEIGHT_TO_WIDTH
    return width to height
}

fun Stone.bounds(laneWidth: Float, screenHeight: Float, slopPx: Float = 0f): StoneBounds {
    val (stoneWidth, stoneHeight) = stoneSize(laneWidth)
    val xCenter = (lane + 0.5f) * laneWidth
    val yCenter = yProgress * screenHeight
    return StoneBounds(
        left = xCenter - stoneWidth / 2f - slopPx,
        top = yCenter - stoneHeight / 2f - slopPx,
        right = xCenter + stoneWidth / 2f + slopPx,
        bottom = yCenter + stoneHeight / 2f + slopPx
    )
}

/**
 * Returns the untapped stone whose rectangle contains (x, y).
 * If two stones overlap, the one whose center is closest to the tap wins.
 */
fun findTappedStone(
    stones: List<Stone>,
    x: Float,
    y: Float,
    screenWidth: Float,
    screenHeight: Float,
    slopPx: Float = 0f
): Stone? {
    if (screenWidth <= 0f || screenHeight <= 0f) return null
    val laneWidth = screenWidth / 4f
    return stones
        .asSequence()
        .filter { !it.isTapped }
        .map { stone -> stone to stone.bounds(laneWidth, screenHeight, slopPx) }
        .filter { (_, bounds) -> bounds.contains(x, y) }
        .minByOrNull { (_, bounds) ->
            val dx = x - bounds.centerX()
            val dy = y - bounds.centerY()
            dx * dx + dy * dy
        }
        ?.first
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    val color: Color,
    val size: Float,
    var maxLife: Float = 1f,
    var life: Float = 1f
)

data class ComboPopup(
    val text: String,
    val x: Float,
    val y: Float,
    var alpha: Float = 1f,
    var offsetY: Float = 0f,
    var scale: Float = 1f
)
