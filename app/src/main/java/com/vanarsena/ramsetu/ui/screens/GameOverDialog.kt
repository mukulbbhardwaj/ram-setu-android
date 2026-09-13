package com.vanarsena.ramsetu.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.engine.SetuStage
import com.vanarsena.ramsetu.engine.crossingProgressToLanka
import com.vanarsena.ramsetu.engine.isNearLanka
import com.vanarsena.ramsetu.engine.reachedLanka
import com.vanarsena.ramsetu.engine.stonesToLanka
import com.vanarsena.ramsetu.ui.components.BridgeProgressBar
import com.vanarsena.ramsetu.ui.rememberReduceMotion
import com.vanarsena.ramsetu.ui.setuStageShortName
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.SindoorRed
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun GameOverDialog(
    score: Int,
    stage: SetuStage,
    highScore: Int,
    isNewRecord: Boolean,
    maxCombo: Int,
    hapticManager: HapticManager,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    val reduceMotion = rememberReduceMotion()
    val completedCrossing = reachedLanka(score)
    val remaining = stonesToLanka(score)
    val nearMiss = isNearLanka(score)
    val infiniteTransition = rememberInfiniteTransition(label = "crownGlow")
    val crownScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (reduceMotion || !completedCrossing) 1.0f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownPulse"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(CardSurfaceDark)
                .border(
                    2.dp,
                    if (completedCrossing) GoldAccent else SindoorRed.copy(alpha = 0.85f),
                    RoundedCornerShape(32.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (completedCrossing) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(crownScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = stringResource(R.string.crown_cd),
                            tint = GoldAccent,
                            modifier = Modifier.size(68.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = stringResource(
                        if (completedCrossing) R.string.lanka_reached else R.string.setu_unfinished
                    ),
                    color = if (completedCrossing) GoldAccent else SindoorRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        if (completedCrossing) {
                            R.string.lanka_reached_body
                        } else {
                            R.string.setu_unfinished_body
                        }
                    ),
                    color = TextPrimary.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                if (nearMiss) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.near_lanka, remaining),
                        color = GoldAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (isNewRecord) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SindoorRed)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.new_record),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BridgeProgressBar(
                    progress = crossingProgressToLanka(score),
                    stageLabel = setuStageShortName(stage),
                    lap = 1,
                    compact = true
                )

                if (!completedCrossing && !nearMiss) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.stones_from_lanka, remaining),
                        color = TextGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F2646))
                        .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.stones_collected_format, score),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.best),
                                color = TextGold.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$highScore",
                                color = TextGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.max_combo),
                                color = TextGold.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${maxCombo}x",
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.stone_replay),
                    contentDescription = stringResource(R.string.replay),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(96.dp)
                        .clickable {
                            hapticManager.playStoneTap()
                            onRestart()
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.return_main_menu),
                    color = TextGold.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            hapticManager.playButtonTap()
                            onMainMenu()
                        }
                        .padding(12.dp)
                )
            }
        }
    }
}
