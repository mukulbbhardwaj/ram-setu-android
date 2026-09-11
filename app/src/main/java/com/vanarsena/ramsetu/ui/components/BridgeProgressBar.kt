package com.vanarsena.ramsetu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.OceanNavy
import com.vanarsena.ramsetu.ui.theme.OceanSurface
import com.vanarsena.ramsetu.ui.theme.StoneColor
import com.vanarsena.ramsetu.ui.theme.StoneHighlight
import com.vanarsena.ramsetu.ui.theme.StoneShadow
import com.vanarsena.ramsetu.ui.theme.TextGold

private const val BRIDGE_STONE_COUNT = 14

private val GraniteFill = Brush.verticalGradient(
    colors = listOf(StoneHighlight, StoneColor, StoneShadow)
)

private val StraitWater = Brush.horizontalGradient(
    colors = listOf(
        OceanDeep,
        OceanNavy,
        OceanSurface,
        OceanNavy,
        OceanDeep
    )
)

@Composable
fun BridgeProgressBar(
    progress: Float,
    speedLabel: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "bridgeProgress"
    )
    val percent = (progress * 100).toInt()
    val filledStones = (animatedProgress * BRIDGE_STONE_COUNT).toInt().coerceIn(0, BRIDGE_STONE_COUNT)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShoreLabel(text = "रामेश्वरम्")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "सेतु  $percent%",
                    color = TextGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                SpeedChip(label = speedLabel)
            }

            ShoreLabel(text = "लंका")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .shadow(6.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(StraitWater)
                .border(1.dp, StoneShadow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 4.dp, vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(BRIDGE_STONE_COUNT) { index ->
                    if (index < filledStones) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(percent = 35))
                                .background(GraniteFill)
                                .border(
                                    0.7.dp,
                                    StoneShadow.copy(alpha = 0.75f),
                                    RoundedCornerShape(percent = 35)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoreLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GraniteFill)
            .border(1.dp, StoneShadow.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF4E3A2F),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SpeedChip(label: String) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 20.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GraniteFill)
            .border(1.dp, GoldAccent.copy(alpha = 0.55f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF5C4033),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
