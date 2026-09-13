package com.vanarsena.ramsetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.Achievement
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.ui.ProvideAppLanguage
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanNavy
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun AchievementsDialog(
    language: String,
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    onDismiss: () -> Unit
) {
    val unlocked = preferencesManager.unlockedAchievementIds()

    Dialog(onDismissRequest = onDismiss) {
        ProvideAppLanguage(language) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurfaceDark)
                .border(2.dp, GoldAccent.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Achievement.entries.forEach { achievement ->
                    val isUnlocked = achievement.id in unlocked
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isUnlocked) OceanNavy else OceanNavy.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                if (isUnlocked) GoldAccent.copy(alpha = 0.7f) else GoldAccent.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(achievement.titleRes),
                                color = if (isUnlocked) GoldAccent else TextGold.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (!isUnlocked) {
                                Text(
                                    text = stringResource(R.string.ach_locked),
                                    color = TextGold.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = stringResource(achievement.descRes),
                            color = TextPrimary.copy(alpha = if (isUnlocked) 0.9f else 0.55f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        hapticManager.playButtonTap()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.accept),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }
    }
}
