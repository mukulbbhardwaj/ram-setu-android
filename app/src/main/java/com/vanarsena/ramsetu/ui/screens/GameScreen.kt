package com.vanarsena.ramsetu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.engine.GameEngine
import com.vanarsena.ramsetu.engine.GameStatus
import com.vanarsena.ramsetu.ui.components.BridgeProgressBar
import com.vanarsena.ramsetu.ui.components.OceanBackground
import com.vanarsena.ramsetu.ui.components.drawComboPopups
import com.vanarsena.ramsetu.ui.components.drawParticles
import com.vanarsena.ramsetu.ui.components.drawStone
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.OceanWaveFoam
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.SindoorRed
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    modifier: Modifier = Modifier
) {
    // High-performance 60/120 FPS game loop
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    val hitSlopPx = with(LocalDensity.current) { 10.dp.toPx() }
    val stoneImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.big_rock)

    LaunchedEffect(gameEngine.status) {
        lastFrameTimeNanos = 0L
        while (gameEngine.status == GameStatus.PLAYING) {
            withFrameNanos { currentNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val deltaNanos = currentNanos - lastFrameTimeNanos
                    gameEngine.update(deltaNanos)
                }
                lastFrameTimeNanos = currentNanos
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Dynamic Ocean Background
        OceanBackground()

        // 2. Play field: taps land here, behind the HUD so Pause stays clickable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hitSlopPx) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (!change.changedToDown() || change.isConsumed) return@forEach
                                change.consume()
                                gameEngine.onTap(
                                    x = change.position.x,
                                    y = change.position.y,
                                    screenWidth = size.width.toFloat(),
                                    screenHeight = size.height.toFloat(),
                                    slopPx = hitSlopPx
                                )
                            }
                        }
                    }
                }
        ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val laneWidth = width / 4f

            // Draw 4 Lane Track Separators
            for (i in 1..3) {
                val x = i * laneWidth
                drawLine(
                    color = GoldAccent.copy(alpha = 0.25f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 2.5f
                )
            }

            // Draw Sweet Hit Zone baseline glow at bottom (0.88f)
            val hitZoneY = height * 0.88f
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        OceanWaveFoam.copy(alpha = 0.6f),
                        GoldAccent.copy(alpha = 0.8f),
                        OceanWaveFoam.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, hitZoneY),
                end = Offset(width, hitZoneY),
                strokeWidth = 4f
            )

            // Draw Falling Stones
            for (stone in gameEngine.stones) {
                drawStone(
                    stone = stone,
                    laneWidth = laneWidth,
                    screenHeight = height,
                    stoneImage = stoneImage
                )
            }

            // Draw Particle Bursts
            drawParticles(
                particles = gameEngine.particles,
                screenWidth = width,
                screenHeight = height
            )

            // Draw Combo & Milestone Popups
            drawComboPopups(
                popups = gameEngine.comboPopups,
                screenWidth = width,
                screenHeight = height
            )
        }
        }

        // 3. Top HUD: Score, Streak, Pause Button, and Bridge Progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .statusBarsPadding()
                .padding(top = 8.dp)
                .pointerInput(Unit) {
                    // Occupy this region so play-field taps do not leak through the HUD.
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        ) {
            // HUD Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .zIndex(2f)
                        .clip(CircleShape)
                        .background(OceanDeep.copy(alpha = 0.8f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            hapticManager.playButtonTap()
                            gameEngine.pauseGame()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pause",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center: Score Counter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardSurfaceDark)
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "पत्थर संग्रह : ",
                            color = TextGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${gameEngine.score}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Keep the score centered against the pause button
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Bridge Construction Progress Meter
            BridgeProgressBar(
                progress = gameEngine.bridgeProgress,
                speedLabel = gameEngine.currentTier.label,
                score = gameEngine.score
            )

            if (gameEngine.combo > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SindoorRed.copy(alpha = 0.9f))
                            .border(1.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${gameEngine.combo}x",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 4. Pause Dialog Overlay
        if (gameEngine.status == GameStatus.PAUSED) {
            Dialog(onDismissRequest = { gameEngine.resumeGame() }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(CardSurfaceDark)
                        .border(2.dp, GoldAccent, RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "खेल रुका हुआ है",
                            color = GoldAccent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Resume Button
                        Button(
                            onClick = {
                                hapticManager.playButtonTap()
                                gameEngine.resumeGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "जारी रखें (Resume)", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Restart Button
                        OutlinedButton(
                            onClick = {
                                hapticManager.playButtonTap()
                                gameEngine.restartGame()
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "पुनः खेलें (Restart)", color = TextGold, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Exit to Menu
                        Text(
                            text = "मुख्य पृष्ठ पर जाएं",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    hapticManager.playButtonTap()
                                    gameEngine.exitToMenu()
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // 5. Game Over Dialog Overlay
        if (gameEngine.status == GameStatus.GAME_OVER) {
            GameOverDialog(
                score = gameEngine.score,
                highScore = preferencesManager.highScore,
                isNewRecord = gameEngine.isNewHighScore,
                maxCombo = gameEngine.maxCombo,
                hapticManager = hapticManager,
                onRestart = { gameEngine.restartGame() },
                onMainMenu = { gameEngine.exitToMenu() }
            )
        }
    }
}
