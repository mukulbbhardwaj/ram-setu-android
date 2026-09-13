package com.vanarsena.ramsetu.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.vanarsena.ramsetu.engine.Stone
import com.vanarsena.ramsetu.engine.stoneSize
import com.vanarsena.ramsetu.ui.theme.DivineGlow
import com.vanarsena.ramsetu.ui.theme.OceanWaveFoam
import kotlin.math.roundToInt

fun DrawScope.drawStone(
    stone: Stone,
    laneWidth: Float,
    screenHeight: Float,
    stoneImage: ImageBitmap,
    stoneTint: Color = Color.White
) {
    val (stoneWidth, stoneHeight) = stoneSize(laneWidth)

    val xCenter = (stone.lane + 0.5f + stone.bobLaneOffset) * laneWidth
    val yCenter = stone.yProgress * screenHeight

    val left = xCenter - stoneWidth / 2f
    val top = yCenter - stoneHeight / 2f

    rotate(degrees = stone.rotation, pivot = Offset(xCenter, yCenter)) {
        scale(scale = stone.scale, pivot = Offset(xCenter, yCenter)) {

            val rippleWidth = stoneWidth * 1.25f
            val rippleHeight = stoneWidth * 0.38f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OceanWaveFoam.copy(alpha = 0.35f * stone.alpha),
                        Color.Transparent
                    ),
                    center = Offset(xCenter, top + stoneHeight * 0.92f),
                    radius = rippleWidth * 0.55f
                ),
                topLeft = Offset(xCenter - rippleWidth / 2f, top + stoneHeight * 0.82f),
                size = Size(rippleWidth, rippleHeight)
            )

            if (stone.yProgress in 0.6f..1.0f && !stone.isTapped) {
                val auraIntensity = ((stone.yProgress - 0.6f) / 0.4f).coerceIn(0f, 1f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DivineGlow.copy(alpha = 0.4f * auraIntensity * stone.alpha),
                            Color.Transparent
                        ),
                        center = Offset(xCenter, yCenter),
                        radius = stoneWidth * 0.95f
                    ),
                    topLeft = Offset(left - 8f, top - 8f),
                    size = Size(stoneWidth + 16f, stoneHeight + 16f)
                )
            }

            val tintFilter = if (stoneTint == Color.White) {
                null
            } else {
                ColorFilter.tint(stoneTint, BlendMode.Modulate)
            }

            drawImage(
                image = stoneImage,
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize(
                    stoneWidth.roundToInt().coerceAtLeast(1),
                    stoneHeight.roundToInt().coerceAtLeast(1)
                ),
                alpha = stone.alpha,
                colorFilter = tintFilter
            )
        }
    }
}
