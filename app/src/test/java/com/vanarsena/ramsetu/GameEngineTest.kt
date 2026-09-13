package com.vanarsena.ramsetu

import com.vanarsena.ramsetu.data.HapticIntensity
import com.vanarsena.ramsetu.engine.BRIDGE_STONES_PER_LAP
import com.vanarsena.ramsetu.engine.HitGrade
import com.vanarsena.ramsetu.engine.HIT_ZONE_Y
import com.vanarsena.ramsetu.engine.SetuStage
import com.vanarsena.ramsetu.engine.Stone
import com.vanarsena.ramsetu.engine.bridgeProgressFraction
import com.vanarsena.ramsetu.engine.computeBridgeLap
import com.vanarsena.ramsetu.engine.consumeSpawnTime
import com.vanarsena.ramsetu.engine.crossingProgressToLanka
import com.vanarsena.ramsetu.engine.difficultyAt
import com.vanarsena.ramsetu.engine.findLowestStoneInLane
import com.vanarsena.ramsetu.engine.findTappedStone
import com.vanarsena.ramsetu.engine.gradeHit
import com.vanarsena.ramsetu.engine.isNearLanka
import com.vanarsena.ramsetu.engine.reachedLanka
import com.vanarsena.ramsetu.engine.setuStageForScore
import com.vanarsena.ramsetu.engine.stageAllowsDoubleSpawn
import com.vanarsena.ramsetu.engine.stoneSize
import com.vanarsena.ramsetu.engine.stonesToLanka
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun difficultyStartsFasterThanLegacyTierZero() {
        val start = difficultyAt(0)
        assertEquals(900L, start.spawnIntervalMs)
        assertEquals(2800L, start.fallDurationMs)
        assertTrue(start.spawnIntervalMs < 1000L)
        assertTrue(start.fallDurationMs < 3400L)
    }

    @Test
    fun difficultyIncreasesWithScore() {
        val early = difficultyAt(0)
        val mid = difficultyAt(50)
        val late = difficultyAt(300)
        assertTrue(mid.spawnIntervalMs < early.spawnIntervalMs)
        assertTrue(mid.fallDurationMs < early.fallDurationMs)
        assertTrue(late.spawnIntervalMs <= mid.spawnIntervalMs)
        assertTrue(late.fallDurationMs <= mid.fallDurationMs)
        assertEquals(320L, late.spawnIntervalMs)
        assertEquals(1050L, late.fallDurationMs)
    }

    @Test
    fun setuStageBoundariesMatchFiveDayCampaign() {
        assertEquals(SetuStage.DAY1, setuStageForScore(0))
        assertEquals(SetuStage.DAY1, setuStageForScore(19))
        assertEquals(SetuStage.DAY2, setuStageForScore(20))
        assertEquals(SetuStage.DAY3, setuStageForScore(50))
        assertEquals(SetuStage.DAY4, setuStageForScore(85))
        assertEquals(SetuStage.DAY5, setuStageForScore(125))
        assertEquals(SetuStage.YATRA, setuStageForScore(170))
    }

    @Test
    fun doubleSpawnOnlyFromDayThree() {
        assertFalse(stageAllowsDoubleSpawn(SetuStage.DAY1))
        assertFalse(stageAllowsDoubleSpawn(SetuStage.DAY2))
        assertTrue(stageAllowsDoubleSpawn(SetuStage.DAY3))
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
    fun bridgeLapUsesOneHundredSeventyStoneCrossing() {
        assertEquals(170, BRIDGE_STONES_PER_LAP)
        assertEquals(1, computeBridgeLap(0))
        assertEquals(1, computeBridgeLap(169))
        assertEquals(1, computeBridgeLap(170))
        assertEquals(2, computeBridgeLap(171))
        assertEquals(1f, bridgeProgressFraction(170), 0.001f)
        assertEquals(1f / 170f, bridgeProgressFraction(171), 0.001f)
    }

    @Test
    fun lankaIsTheWinAndNearMissPullsReplay() {
        assertFalse(reachedLanka(0))
        assertFalse(reachedLanka(169))
        assertTrue(reachedLanka(170))
        assertTrue(reachedLanka(300))

        assertEquals(170, stonesToLanka(0))
        assertEquals(1, stonesToLanka(169))
        assertEquals(0, stonesToLanka(170))
        assertEquals(0, stonesToLanka(400))

        assertFalse(isNearLanka(0))
        assertFalse(isNearLanka(144))
        assertTrue(isNearLanka(145))
        assertTrue(isNearLanka(169))
        assertFalse(isNearLanka(170))

        assertEquals(0f, crossingProgressToLanka(0), 0.001f)
        assertEquals(169f / 170f, crossingProgressToLanka(169), 0.001f)
        assertEquals(1f, crossingProgressToLanka(170), 0.001f)
        assertEquals(1f, crossingProgressToLanka(250), 0.001f)
    }

    @Test
    fun stoneKeepsSpawnFallDuration() {
        val stone = Stone(id = 1L, lane = 0, fallDurationMs = 2800L)
        assertEquals(2800L, stone.fallDurationMs)
        val faster = Stone(id = 2L, lane = 1, fallDurationMs = 1200L)
        assertEquals(1200L, faster.fallDurationMs)
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
