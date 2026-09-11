package com.vanarsena.ramsetu.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.vanarsena.ramsetu.engine.ComboPopup
import com.vanarsena.ramsetu.engine.Particle
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.SunsetOrange

fun DrawScope.drawParticles(
    particles: List<Particle>,
    screenWidth: Float,
    screenHeight: Float
) {
    for (p in particles) {
        val px = p.x * screenWidth
        val py = p.y * screenHeight
        drawCircle(
            color = p.color.copy(alpha = p.alpha),
            radius = p.size,
            center = Offset(px, py)
        )
    }
}

fun DrawScope.drawComboPopups(
    popups: List<ComboPopup>,
    screenWidth: Float,
    screenHeight: Float
) {
    for (popup in popups) {
        val px = popup.x * screenWidth
        val py = (popup.y * screenHeight) + popup.offsetY

        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = GoldAccent.copy(alpha = popup.alpha).toArgb()
                textSize = 46f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(
                    12f, 0f, 4f,
                    SunsetOrange.copy(alpha = popup.alpha * 0.9f).toArgb()
                )
            }
            drawText(popup.text, px, py, paint)
        }
    }
}
