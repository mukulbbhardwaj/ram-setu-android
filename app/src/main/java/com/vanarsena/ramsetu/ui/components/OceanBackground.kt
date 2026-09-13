package com.vanarsena.ramsetu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.vanarsena.ramsetu.engine.SetuStage
import com.vanarsena.ramsetu.engine.paletteForStage
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun OceanBackground(
    modifier: Modifier = Modifier,
    stage: SetuStage = SetuStage.DAY1,
    reduceMotion: Boolean = false
) {
    val palette = paletteForStage(stage)
    val blendMs = if (reduceMotion) 0 else 800

    val skyTop by animateColorAsState(palette.skyTop, tween(blendMs), label = "skyTop")
    val skyMid by animateColorAsState(palette.skyMid, tween(blendMs), label = "skyMid")
    val oceanNavy by animateColorAsState(palette.oceanNavy, tween(blendMs), label = "oceanNavy")
    val oceanDeep by animateColorAsState(palette.oceanDeep, tween(blendMs), label = "oceanDeep")
    val waveSurface by animateColorAsState(palette.waveSurface, tween(blendMs), label = "waveSurface")
    val foamAlpha by animateFloatAsState(palette.foamAlpha, tween(blendMs), label = "foamAlpha")
    val sunAlpha by animateFloatAsState(palette.sunAlpha, tween(blendMs), label = "sunAlpha")

    val infiniteTransition = rememberInfiniteTransition(label = "oceanWaves")
    val animatedPhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val animatedPhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightning"
    )
    val wavePhase1 = if (reduceMotion) 0f else animatedPhase1
    val wavePhase2 = if (reduceMotion) 0f else animatedPhase2
    val ampScale = palette.waveAmplitudeScale

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val skyGradient = Brush.verticalGradient(
            colors = listOf(skyTop, skyMid, oceanNavy, oceanDeep),
            startY = 0f,
            endY = height
        )
        drawRect(brush = skyGradient)

        if (palette.showLightning && !reduceMotion && lightningFlash > 0.92f) {
            drawRect(Color.White.copy(alpha = 0.12f))
        }

        val wavePath1 = Path().apply {
            moveTo(0f, height)
            val waveYBase = height * 0.45f
            for (x in 0..width.toInt() step 12) {
                val waveHeight = 18f * ampScale * sin((x * 0.015f) + wavePhase1)
                lineTo(x.toFloat(), waveYBase + waveHeight + (height * 0.55f * (x / width) * 0.1f))
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = wavePath1, color = oceanNavy.copy(alpha = 0.65f))

        val wavePath2 = Path().apply {
            moveTo(0f, height)
            val waveYBase = height * 0.62f
            for (x in 0..width.toInt() step 10) {
                val waveHeight = 22f * ampScale * sin((x * 0.02f) - wavePhase2)
                lineTo(x.toFloat(), waveYBase + waveHeight)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = wavePath2, color = waveSurface.copy(alpha = 0.7f))

        val foamColor = Color(0xFF8AD1D8)
        val wavePath3 = Path().apply {
            moveTo(0f, height)
            val waveYBase = height * 0.78f
            for (x in 0..width.toInt() step 8) {
                val waveHeight = 16f * ampScale * sin((x * 0.028f) + wavePhase1 * 1.3f)
                lineTo(x.toFloat(), waveYBase + waveHeight)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = wavePath3, color = foamColor.copy(alpha = foamAlpha))

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x33FFD54F), Color.Transparent),
                center = Offset(width * 0.5f, height * palette.sunCenterYFraction),
                radius = width * 0.6f
            ),
            radius = width * 0.6f,
            center = Offset(width * 0.5f, height * palette.sunCenterYFraction),
            alpha = sunAlpha
        )

        if (palette.showStars) {
            val starSeed = stage.ordinal * 31
            repeat(48) { index ->
                val rx = Random(starSeed + index).nextFloat()
                val ry = Random(starSeed + index + 17).nextFloat()
                val twinkle = if (reduceMotion) {
                    0.5f
                } else {
                    0.35f + 0.25f * sin(wavePhase1 + index)
                }
                drawCircle(
                    color = Color(0xFFFFE082).copy(alpha = twinkle * 0.85f),
                    radius = 1.2f + (index % 3),
                    center = Offset(rx * width, ry * height * 0.42f)
                )
            }
        }
    }
}
