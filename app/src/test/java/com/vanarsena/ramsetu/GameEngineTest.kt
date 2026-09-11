package com.vanarsena.ramsetu

import com.vanarsena.ramsetu.data.HapticIntensity
import com.vanarsena.ramsetu.engine.SpeedTiers
import com.vanarsena.ramsetu.engine.Stone
import com.vanarsena.ramsetu.engine.findTappedStone
import com.vanarsena.ramsetu.engine.stoneSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
