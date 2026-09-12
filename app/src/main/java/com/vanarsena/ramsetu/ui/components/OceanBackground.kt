package com.vanarsena.ramsetu.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.OceanNavy
import com.vanarsena.ramsetu.ui.theme.OceanSurface
import com.vanarsena.ramsetu.ui.theme.OceanWaveFoam
import com.vanarsena.ramsetu.ui.theme.SaffronDark
import com.vanarsena.ramsetu.ui.theme.SunsetOrange
import kotlin.math.sin

@Composable
fun OceanBackground(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
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
    val wavePhase1 = if (reduceMotion) 0f else animatedPhase1
    val wavePhase2 = if (reduceMotion) 0f else animatedPhase2

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Sky & Ocean Deep Gradient
        val skyGradient = Brush.verticalGradient(
            colors = listOf(
                SunsetOrange.copy(alpha = 0.85f),
                SaffronDark.copy(alpha = 0.5f),
                OceanNavy,
                OceanDeep
            ),
            startY = 0f,
            endY = height
        )
        drawRect(brush = skyGradient)

        // 2. Animated Deep Water Waves
        val wavePath1 = Path().apply {
            moveTo(0f, height)
            val waveYBase = height * 0.45f
            for (x in 0..width.toInt() step 12) {
                val waveHeight = 18f * sin((x * 0.015f) + wavePhase1)
                lineTo(x.toFloat(), waveYBase + waveHeight + (height * 0.55f * (x / width) * 0.1f))
            }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = wavePath1,
            color = OceanNavy.copy(alpha = 0.65f)
        )

        // 3. Rolling Middle Waves
        val wavePath2 = Path().apply {
            moveTo(0f, height)
            val waveYBase = height * 0.62f
            for (x in 0..width.toInt() step 10) {
                val waveHeight = 22f * sin((x * 0.02f) - wavePhase2)
                lineTo(x.toFloat(), waveYBase + waveHeight)
            }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = wavePath2,
            color = OceanSurface.copy(alpha = 0.7f)
        )

        // 4. Foreground Crest Foam
        val wavePath3 = Path().apply {
            moveTo(0f, height)
            val waveYBase = height * 0.78f
            for (x in 0..width.toInt() step 8) {
                val waveHeight = 16f * sin((x * 0.028f) + wavePhase1 * 1.3f)
                lineTo(x.toFloat(), waveYBase + waveHeight)
            }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = wavePath3,
            color = OceanWaveFoam.copy(alpha = 0.25f)
        )

        // 5. Sun Glimmer on Water
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x33FFD54F), Color.Transparent),
                center = Offset(width * 0.5f, height * 0.25f),
                radius = width * 0.6f
            ),
            radius = width * 0.6f,
            center = Offset(width * 0.5f, height * 0.25f)
        )
    }
}
