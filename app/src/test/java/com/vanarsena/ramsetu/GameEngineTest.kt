package com.vanarsena.ramsetu

import com.vanarsena.ramsetu.data.HapticIntensity
import com.vanarsena.ramsetu.engine.HitGrade
import com.vanarsena.ramsetu.engine.HIT_ZONE_Y
import com.vanarsena.ramsetu.engine.SpeedTiers
import com.vanarsena.ramsetu.engine.Stone
import com.vanarsena.ramsetu.engine.bridgeProgressFraction
import com.vanarsena.ramsetu.engine.computeBridgeLap
import com.vanarsena.ramsetu.engine.consumeSpawnTime
import com.vanarsena.ramsetu.engine.findLowestStoneInLane
import com.vanarsena.ramsetu.engine.findTappedStone
import com.vanarsena.ramsetu.engine.gradeHit
import com.vanarsena.ramsetu.engine.stoneSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun testSpeedTiersProgression() {
        assertEquals(5, SpeedTiers.size)

        // Tier 0 (Default)
        val tier0 = SpeedTiers[0]
        assertEquals(0, tier0.minScore)
        assertEquals(1000L, tier0.spawnIntervalMs)
        assertEquals(3400L, tier0.fallDurationMs)

        // Tier 1 (Score 70)
        val tier1 = SpeedTiers[1]
        assertEquals(70, tier1.minScore)
        assertEquals(850L, tier1.spawnIntervalMs)

        // Tier 2 (Score 150)
        val tier2 = SpeedTiers[2]
        assertEquals(150, tier2.minScore)
        assertEquals(720L, tier2.spawnIntervalMs)

        // Tier 3 (Score 400)
        val tier3 = SpeedTiers[3]
        assertEquals(400, tier3.minScore)
        assertEquals(600L, tier3.spawnIntervalMs)

        // Tier 4 (Score 800)
        val tier4 = SpeedTiers[4]
        assertEquals(800, tier4.minScore)
        assertEquals(500L, tier4.spawnIntervalMs)
    }

    @Test
    fun testStoneStateInitialization() {
        val stone = Stone(id = 1L, lane = 2, yProgress = 0f)
        assertEquals(1L, stone.id)
        assertEquals(2, stone.lane)
        assertEquals(0f, stone.yProgress, 0.001f)
        assertFalse(stone.isTapped)
        assertEquals(1f, stone.alpha, 0.001f)
    }

    @Test
    fun testHapticIntensityMultipliers() {
        assertEquals(0f, HapticIntensity.OFF.multiplier, 0.001f)
        assertEquals(0.4f, HapticIntensity.SUBTLE.multiplier, 0.001f)
        assertEquals(0.8f, HapticIntensity.MEDIUM.multiplier, 0.001f)
        assertEquals(1.0f, HapticIntensity.STRONG.multiplier, 0.001f)
    }

    @Test
    fun hitOnGoldLineIsPerfect() {
        assertEquals(HitGrade.PERFECT, gradeHit(HIT_ZONE_Y))
        assertEquals(HitGrade.PERFECT, gradeHit(HIT_ZONE_Y + 0.05f))
        assertEquals(HitGrade.GOOD, gradeHit(HIT_ZONE_Y + 0.10f))
        assertEquals(HitGrade.OK, gradeHit(0.2f))
    }

    @Test
    fun spawnAccumulatorKeepsRemainder() {
        val tick = consumeSpawnTime(accumulatedMs = 2500L, intervalMs = 1000L)
        assertEquals(2, tick.spawnCount)
        assertEquals(500L, tick.remainderMs)
    }

    @Test
    fun spawnAccumulatorCapsCatchUp() {
        val tick = consumeSpawnTime(accumulatedMs = 10_000L, intervalMs = 500L, maxSpawns = 3)
        assertEquals(3, tick.spawnCount)
        assertEquals(8500L, tick.remainderMs)
    }

    @Test
    fun bridgeLapDoesNotResetAtHundred() {
        assertEquals(1, computeBridgeLap(0))
        assertEquals(1, computeBridgeLap(99))
        assertEquals(1, computeBridgeLap(100))
        assertEquals(2, computeBridgeLap(101))
        assertEquals(1f, bridgeProgressFraction(100), 0.001f)
        assertEquals(0.01f, bridgeProgressFraction(101), 0.001f)
    }

    @Test
    fun stoneKeepsSpawnFallDuration() {
        val stone = Stone(id = 1L, lane = 0, fallDurationMs = 3400L)
        assertEquals(3400L, stone.fallDurationMs)
        val faster = Stone(id = 2L, lane = 1, fallDurationMs = 1650L)
        assertEquals(1650L, faster.fallDurationMs)
    }

    @Test
    fun keyboardLanePicksLowestStone() {
        val upper = Stone(id = 1L, lane = 2, yProgress = 0.4f)
        val lower = Stone(id = 2L, lane = 2, yProgress = 0.8f)
        val other = Stone(id = 3L, lane = 1, yProgress = 0.9f)
        val picked = findLowestStoneInLane(listOf(upper, lower, other), 2)
        assertNotNull(picked)
        assertEquals(2L, picked!!.id)
    }
}

class StoneHitTest {
    private val screenWidth = 1080f
    private val screenHeight = 1920f
    private val laneWidth = screenWidth / 4f

    private fun stone(lane: Int, yProgress: Float, id: Long = 1L, tapped: Boolean = false) =
        Stone(id = id, lane = lane, yProgress = yProgress, isTapped = tapped)

    private fun tapOn(stone: Stone, dx: Float = 0f, dy: Float = 0f): Pair<Float, Float> {
        val x = (stone.lane + 0.5f) * laneWidth + dx
        val y = stone.yProgress * screenHeight + dy
        return x to y
    }

    @Test
    fun tapOnStoneCenterHits() {
        val target = stone(lane = 2, yProgress = 0.6f)
        val (x, y) = tapOn(target)

        val hit = findTappedStone(listOf(target), x, y, screenWidth, screenHeight)

        assertNotNull(hit)
        assertEquals(target.id, hit!!.id)
    }

    @Test
    fun tapNeighborLaneAtSameHeightMisses() {
        val target = stone(lane = 0, yProgress = 0.6f)
        val x = (1 + 0.5f) * laneWidth
        val y = target.yProgress * screenHeight

        assertNull(findTappedStone(listOf(target), x, y, screenWidth, screenHeight))
    }

    @Test
    fun tapOnSameLaneButAwayFromStoneMisses() {
        val target = stone(lane = 1, yProgress = 0.35f)
        val x = (target.lane + 0.5f) * laneWidth
        val y = screenHeight * 0.85f

        assertNull(findTappedStone(listOf(target), x, y, screenWidth, screenHeight))
    }

    @Test
    fun tapJustOutsideStoneBoundsMisses() {
        val target = stone(lane = 0, yProgress = 0.5f)
        val (_, stoneHeight) = stoneSize(laneWidth)
        val (x, y) = tapOn(target, dy = stoneHeight / 2f + 8f)

        assertNull(findTappedStone(listOf(target), x, y, screenWidth, screenHeight))
    }

    @Test
    fun alreadyCollectedStoneIsIgnored() {
        val target = stone(lane = 3, yProgress = 0.7f, tapped = true)
        val (x, y) = tapOn(target)

        assertNull(findTappedStone(listOf(target), x, y, screenWidth, screenHeight))
    }

    @Test
    fun overlappingStonesPreferClosestCenter() {
        val upper = stone(lane = 2, yProgress = 0.55f, id = 1L)
        val lower = stone(lane = 2, yProgress = 0.62f, id = 2L)
        val (x, y) = tapOn(lower)

        val hit = findTappedStone(listOf(upper, lower), x, y, screenWidth, screenHeight)

        assertNotNull(hit)
        assertEquals(2L, hit!!.id)
    }
}
