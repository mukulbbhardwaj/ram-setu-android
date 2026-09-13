package com.vanarsena.ramsetu.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.engine.GOOD_WINDOW
import com.vanarsena.ramsetu.engine.GameEngine
import com.vanarsena.ramsetu.engine.GameStatus
import com.vanarsena.ramsetu.engine.HIT_ZONE_Y
import com.vanarsena.ramsetu.engine.PERFECT_WINDOW
import com.vanarsena.ramsetu.engine.paletteForStage
import com.vanarsena.ramsetu.ui.setuStageBanner
import com.vanarsena.ramsetu.ui.setuStageShortName
import com.vanarsena.ramsetu.ui.components.BridgeProgressBar
import com.vanarsena.ramsetu.ui.components.OceanBackground
import com.vanarsena.ramsetu.ui.components.drawComboPopups
import com.vanarsena.ramsetu.ui.components.drawParticles
import com.vanarsena.ramsetu.ui.components.drawStone
import com.vanarsena.ramsetu.ui.rememberReduceMotion
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.OceanWaveFoam
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.SindoorRed
import com.vanarsena.ramsetu.ui.theme.TextGold

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    modifier: Modifier = Modifier
) {
    val lastFrameTimeNanos = remember { mutableLongStateOf(0L) }
    val hitSlopPx = with(LocalDensity.current) { 20.dp.toPx() }
    val stoneImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.big_rock)
    val focusRequester = remember { FocusRequester() }
    val view = LocalView.current
    val reduceMotion = rememberReduceMotion()
    val pauseCd = stringResource(R.string.pause)
    val keepAwake = gameEngine.status == GameStatus.PLAYING ||
        gameEngine.status == GameStatus.COUNTDOWN
    val stoneTint = paletteForStage(gameEngine.currentStage).stoneTint

    LaunchedEffect(reduceMotion) {
        gameEngine.reduceMotion = reduceMotion
    }

    DisposableEffect(keepAwake) {
        view.keepScreenOn = keepAwake
        onDispose { view.keepScreenOn = false }
    }

    BackHandler(enabled = gameEngine.status != GameStatus.MENU) {
        when (gameEngine.status) {
            GameStatus.PLAYING, GameStatus.COUNTDOWN -> gameEngine.pauseGame()
            GameStatus.PAUSED -> gameEngine.resumeGame()
            GameStatus.GAME_OVER -> gameEngine.exitToMenu()
            else -> Unit
        }
    }

    LaunchedEffect(gameEngine.status) {
        lastFrameTimeNanos.longValue = 0L
        while (
            gameEngine.status == GameStatus.PLAYING ||
            gameEngine.status == GameStatus.COUNTDOWN
        ) {
            withFrameNanos { currentNanos ->
                if (lastFrameTimeNanos.longValue != 0L) {
                    gameEngine.update(currentNanos - lastFrameTimeNanos.longValue)
                }
                lastFrameTimeNanos.longValue = currentNanos
            }
        }
    }

    LaunchedEffect(gameEngine.status) {
        if (gameEngine.status == GameStatus.PLAYING ||
            gameEngine.status == GameStatus.COUNTDOWN
        ) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        OceanBackground(
            stage = gameEngine.currentStage,
            reduceMotion = reduceMotion
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            GameHud(
                gameEngine = gameEngine,
                hapticManager = hapticManager,
                pauseDescription = pauseCd
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                            when (event.key) {
                                Key.Escape -> {
                                    gameEngine.pauseGame()
                                    true
                                }
                                Key.One, Key.NumPad1, Key.A, Key.DirectionLeft -> {
                                    gameEngine.onLaneTap(0)
                                    true
                                }
                                Key.Two, Key.NumPad2, Key.S -> {
                                    gameEngine.onLaneTap(1)
                                    true
                                }
                                Key.Three, Key.NumPad3, Key.D -> {
                                    gameEngine.onLaneTap(2)
                                    true
                                }
                                Key.Four, Key.NumPad4, Key.F, Key.DirectionRight -> {
                                    gameEngine.onLaneTap(3)
                                    true
                                }
                                else -> false
                            }
                        }
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

                        for (i in 1..3) {
                            val x = i * laneWidth
                            drawLine(
                                color = GoldAccent.copy(alpha = 0.25f),
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 2.5f
                            )
                        }

                        val goodTop = height * (HIT_ZONE_Y - GOOD_WINDOW)
                        val goodHeight = height * (GOOD_WINDOW * 2f)
                        drawRect(
                            color = OceanWaveFoam.copy(alpha = 0.08f),
                            topLeft = Offset(0f, goodTop),
                            size = Size(width, goodHeight)
                        )
                        val perfectTop = height * (HIT_ZONE_Y - PERFECT_WINDOW)
                        val perfectHeight = height * (PERFECT_WINDOW * 2f)
                        drawRect(
                            color = GoldAccent.copy(alpha = 0.14f),
                            topLeft = Offset(0f, perfectTop),
                            size = Size(width, perfectHeight)
                        )

                        val hitZoneY = height * HIT_ZONE_Y
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    OceanWaveFoam.copy(alpha = 0.6f),
                                    GoldAccent.copy(alpha = 0.9f),
                                    OceanWaveFoam.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(0f, hitZoneY),
                            end = Offset(width, hitZoneY),
                            strokeWidth = 5f
                        )

                        for (stone in gameEngine.stones) {
                            drawStone(
                                stone = stone,
                                laneWidth = laneWidth,
                                screenHeight = height,
                                stoneImage = stoneImage,
                                stoneTint = stoneTint
                            )
                        }

                        drawParticles(
                            particles = gameEngine.particles,
                            screenWidth = width,
                            screenHeight = height
                        )

                        drawComboPopups(
                            popups = gameEngine.comboPopups,
                            screenWidth = width,
                            screenHeight = height
                        )

                        if (gameEngine.missFlash > 0f) {
                            drawRect(SindoorRed.copy(alpha = 0.22f * gameEngine.missFlash))
                        }
                    }

                    if (gameEngine.status == GameStatus.COUNTDOWN) {
                        val label = when {
                            gameEngine.countdownRemaining > 2f -> "3"
                            gameEngine.countdownRemaining > 1f -> "2"
                            gameEngine.countdownRemaining > 0.35f -> "1"
                            else -> stringResource(R.string.countdown_start)
                        }
                        Text(
                            text = label,
                            color = GoldAccent,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    val bannerStage = gameEngine.stageBannerStage
                    if (bannerStage != null && gameEngine.stageBannerAlpha > 0.05f) {
                        Text(
                            text = setuStageBanner(bannerStage),
                            color = GoldAccent.copy(alpha = gameEngine.stageBannerAlpha),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardSurfaceDark.copy(alpha = 0.85f * gameEngine.stageBannerAlpha))
                                .border(
                                    1.dp,
                                    GoldAccent.copy(alpha = 0.7f * gameEngine.stageBannerAlpha),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }

                    if (gameEngine.isTutorial) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardSurfaceDark.copy(alpha = 0.92f))
                                .border(1.dp, GoldAccent, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.tutorial_title),
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(R.string.tutorial_body),
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (gameEngine.status == GameStatus.PAUSED) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
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
                            text = stringResource(R.string.paused_title),
                            color = GoldAccent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                hapticManager.playButtonTap()
                                gameEngine.resumeGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.resume),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                hapticManager.playButtonTap()
                                gameEngine.restartGame()
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.restart),
                                color = TextGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.main_menu),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    hapticManager.playButtonTap()
                                    gameEngine.exitToMenu()
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }

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

@Composable
private fun GameHud(
    gameEngine: GameEngine,
    hapticManager: HapticManager,
    pauseDescription: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(OceanDeep.copy(alpha = 0.8f))
                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                    .semantics { contentDescription = pauseDescription }
                    .clickable {
                        hapticManager.playButtonTap()
                        gameEngine.pauseGame()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pause),
                    contentDescription = pauseDescription,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurfaceDark)
                    .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.stone_collection_format),
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

            if (gameEngine.combo > 1) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        BridgeProgressBar(
            progress = gameEngine.bridgeProgress,
            stageLabel = setuStageShortName(gameEngine.currentStage),
            lap = gameEngine.bridgeLap
        )
    }
}
