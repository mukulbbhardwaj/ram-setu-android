package com.vanarsena.ramsetu.engine

import androidx.compose.ui.graphics.Color
import com.vanarsena.ramsetu.ui.theme.DivineGlow
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.OceanNavy
import com.vanarsena.ramsetu.ui.theme.OceanSurface
import com.vanarsena.ramsetu.ui.theme.OceanWaveFoam
import com.vanarsena.ramsetu.ui.theme.SaffronDark
import com.vanarsena.ramsetu.ui.theme.SandGolden
import com.vanarsena.ramsetu.ui.theme.SunsetOrange
import kotlin.math.roundToLong

enum class SetuStage(val minScore: Int, val yojanasBuilt: Int) {
    DAY1(0, 14),
    DAY2(20, 20),
    DAY3(50, 21),
    DAY4(85, 22),
    DAY5(125, 23),
    YATRA(170, 23);

    fun next(): SetuStage? = entries.getOrNull(ordinal + 1)
}

fun setuStageForScore(score: Int): SetuStage =
    SetuStage.entries.lastOrNull { score >= it.minScore } ?: SetuStage.DAY1

data class StagePalette(
    val skyTop: Color,
    val skyMid: Color,
    val oceanNavy: Color,
    val oceanDeep: Color,
    val waveSurface: Color,
    val foamAlpha: Float,
    val sunCenterYFraction: Float,
    val sunAlpha: Float,
    val showLightning: Boolean,
    val showStars: Boolean,
    val stoneTint: Color,
    val waveAmplitudeScale: Float
)

fun paletteForStage(stage: SetuStage): StagePalette = when (stage) {
    SetuStage.DAY1 -> StagePalette(
        skyTop = SunsetOrange.copy(alpha = 0.9f),
        skyMid = SandGolden.copy(alpha = 0.45f),
        oceanNavy = OceanNavy,
        oceanDeep = OceanDeep,
        waveSurface = OceanSurface.copy(alpha = 0.85f),
        foamAlpha = 0.22f,
        sunCenterYFraction = 0.22f,
        sunAlpha = 0.35f,
        showLightning = false,
        showStars = false,
        stoneTint = Color.White,
        waveAmplitudeScale = 0.85f
    )
    SetuStage.DAY2 -> StagePalette(
        skyTop = SunsetOrange.copy(alpha = 0.55f),
        skyMid = OceanNavy.copy(alpha = 0.35f),
        oceanNavy = OceanNavy,
        oceanDeep = OceanDeep,
        waveSurface = OceanSurface,
        foamAlpha = 0.28f,
        sunCenterYFraction = 0.18f,
        sunAlpha = 0.45f,
        showLightning = false,
        showStars = false,
        stoneTint = Color(0xFFF5F0E8),
        waveAmplitudeScale = 1.0f
    )
    SetuStage.DAY3 -> StagePalette(
        skyTop = OceanNavy.copy(alpha = 0.7f),
        skyMid = SaffronDark.copy(alpha = 0.25f),
        oceanNavy = Color(0xFF102848),
        oceanDeep = OceanDeep,
        waveSurface = OceanSurface.copy(alpha = 0.9f),
        foamAlpha = 0.32f,
        sunCenterYFraction = 0.2f,
        sunAlpha = 0.28f,
        showLightning = false,
        showStars = false,
        stoneTint = Color(0xFFD8CEC4),
        waveAmplitudeScale = 1.15f
    )
    SetuStage.DAY4 -> StagePalette(
        skyTop = Color(0xFF2A1A3D),
        skyMid = SaffronDark.copy(alpha = 0.35f),
        oceanNavy = Color(0xFF0A1628),
        oceanDeep = Color(0xFF050A14),
        waveSurface = OceanSurface.copy(alpha = 0.75f),
        foamAlpha = 0.38f,
        sunCenterYFraction = 0.28f,
        sunAlpha = 0.15f,
        showLightning = true,
        showStars = false,
        stoneTint = Color(0xFFC4B8AE),
        waveAmplitudeScale = 1.25f
    )
    SetuStage.DAY5, SetuStage.YATRA -> StagePalette(
        skyTop = Color(0xFF1A1030),
        skyMid = Color(0xFF2D1B4E),
        oceanNavy = Color(0xFF081428),
        oceanDeep = Color(0xFF030810),
        waveSurface = OceanSurface.copy(alpha = 0.65f),
        foamAlpha = 0.3f,
        sunCenterYFraction = 0.15f,
        sunAlpha = 0.55f,
        showLightning = false,
        showStars = true,
        stoneTint = DivineGlow.copy(alpha = 0.35f),
        waveAmplitudeScale = 1.05f
    )
}

private data class DifficultyKeyframe(
    val score: Int,
    val spawnIntervalMs: Long,
    val fallDurationMs: Long
)

private val DIFFICULTY_KEYFRAMES = listOf(
    DifficultyKeyframe(0, 900L, 2800L),
    DifficultyKeyframe(20, 780L, 2300L),
    DifficultyKeyframe(50, 640L, 1900L),
    DifficultyKeyframe(85, 520L, 1600L),
    DifficultyKeyframe(125, 430L, 1350L),
    DifficultyKeyframe(170, 380L, 1200L),
    DifficultyKeyframe(300, 320L, 1050L)
)

data class RunDifficulty(
    val spawnIntervalMs: Long,
    val fallDurationMs: Long
)

fun difficultyAt(score: Int): RunDifficulty {
    val keyframes = DIFFICULTY_KEYFRAMES
    if (score <= keyframes.first().score) {
        val k = keyframes.first()
        return RunDifficulty(k.spawnIntervalMs, k.fallDurationMs)
    }
    val upper = keyframes.lastOrNull { score >= it.score } ?: keyframes.first()
    val upperIndex = keyframes.indexOf(upper)
    if (upperIndex >= keyframes.size - 1) {
        return RunDifficulty(upper.spawnIntervalMs, upper.fallDurationMs)
    }
    val lower = keyframes[upperIndex]
    val next = keyframes[upperIndex + 1]
    val span = (next.score - lower.score).coerceAtLeast(1)
    val t = ((score - lower.score).toFloat() / span).coerceIn(0f, 1f)
    val spawn = lerpLong(lower.spawnIntervalMs, next.spawnIntervalMs, t)
    val fall = lerpLong(lower.fallDurationMs, next.fallDurationMs, t)
    return RunDifficulty(spawn, fall)
}

private fun lerpLong(from: Long, to: Long, t: Float): Long =
    (from + (to - from) * t).roundToLong()

fun stageAllowsDoubleSpawn(stage: SetuStage): Boolean =
    stage.ordinal >= SetuStage.DAY3.ordinal

fun stageAllowsStoneBob(stage: SetuStage, reduceMotion: Boolean = false): Boolean =
    !reduceMotion && stage.ordinal >= SetuStage.DAY4.ordinal

const val DOUBLE_SPAWN_CHANCE = 0.18f
const val STONE_BOB_LANE_FRACTION = 0.045f
const val STAGE_BANNER_SECONDS = 1.1f
