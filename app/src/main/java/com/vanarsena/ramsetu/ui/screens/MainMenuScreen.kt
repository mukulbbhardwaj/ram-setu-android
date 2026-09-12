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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.AudioEngine
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.ui.components.OceanBackground
import com.vanarsena.ramsetu.ui.rememberReduceMotion
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun MainMenuScreen(
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    audioEngine: AudioEngine,
    language: String,
    onLanguageChange: (String) -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showKathaDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    val reduceMotion = rememberReduceMotion()

    val infiniteTransition = rememberInfiniteTransition(label = "titleFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = if (reduceMotion) 0f else -8f,
        targetValue = if (reduceMotion) 0f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rockY"
    )

    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (reduceMotion) 1.0f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btnPulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        OceanBackground(reduceMotion = reduceMotion)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(OceanDeep.copy(alpha = 0.75f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable {
                            hapticManager.playButtonTap()
                            showKathaDialog = true
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = stringResource(R.string.katha),
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.katha),
                            color = TextGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OceanDeep.copy(alpha = 0.75f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                hapticManager.playButtonTap()
                                showAchievementsDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = stringResource(R.string.achievements),
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OceanDeep.copy(alpha = 0.75f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                hapticManager.playButtonTap()
                                showSettingsDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings),
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = floatOffset.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ram_setu_pathar),
                    contentDescription = stringResource(R.string.logo_cd),
                    modifier = Modifier
                        .size(220.dp, 120.dp)
                        .shadow(16.dp, RoundedCornerShape(100.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurfaceDark)
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = stringResource(R.string.crown_cd),
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                R.string.high_score_format,
                                preferencesManager.highScore
                            ),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.stone_start),
                    contentDescription = stringResource(R.string.start_game_cd),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .graphicsLayer {
                            scaleX = buttonPulse
                            scaleY = buttonPulse
                        }
                        .clickable {
                            hapticManager.playStoneTap()
                            onStartGame()
                        }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(
                        R.string.total_stones_format,
                        preferencesManager.totalStones
                    ),
                    color = TextGold.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showKathaDialog) {
            KathaDialog(
                hapticManager = hapticManager,
                onDismiss = { showKathaDialog = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                preferencesManager = preferencesManager,
                hapticManager = hapticManager,
                audioEngine = audioEngine,
                language = language,
                onLanguageChange = onLanguageChange,
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showAchievementsDialog) {
            AchievementsDialog(
                preferencesManager = preferencesManager,
                hapticManager = hapticManager,
                onDismiss = { showAchievementsDialog = false }
            )
        }
    }
}
