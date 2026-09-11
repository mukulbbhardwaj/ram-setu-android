package com.vanarsena.ramsetu.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.HapticIntensity
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanNavy
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun SettingsDialog(
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    onDismiss: () -> Unit
) {
    var isMusicEnabled by remember { mutableStateOf(preferencesManager.isMusicEnabled) }
    var isSoundEnabled by remember { mutableStateOf(preferencesManager.isSoundEnabled) }
    var selectedIntensity by remember { mutableStateOf(preferencesManager.hapticIntensity) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurfaceDark)
                .border(2.dp, GoldAccent.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "सेटिंग्स (Settings)",
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Music Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_music_note),
                            contentDescription = "Music",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "  संगीत (Music)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isMusicEnabled,
                        onCheckedChange = {
                            isMusicEnabled = it
                            preferencesManager.isMusicEnabled = it
                            hapticManager.playButtonTap()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SaffronPrimary,
                            checkedTrackColor = GoldAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Sound Effects Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(
                                id = if (isSoundEnabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off
                            ),
                            contentDescription = "Sound",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "  ध्वनि प्रभाव (SFX)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            isSoundEnabled = it
                            preferencesManager.isSoundEnabled = it
                            hapticManager.playButtonTap()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SaffronPrimary,
                            checkedTrackColor = GoldAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Haptic Intensity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vibration),
                        contentDescription = "Haptics",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "  हैप्टिक्स तीव्रता (Haptics)",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Haptic Intensity Options: OFF, SUBTLE, MEDIUM, STRONG
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val levels = listOf(
                        HapticIntensity.OFF to "बंद",
                        HapticIntensity.SUBTLE to "हल्का",
                        HapticIntensity.MEDIUM to "मध्यम",
                        HapticIntensity.STRONG to "प्रबल"
                    )

                    levels.forEach { (intensity, label) ->
                        val isSelected = selectedIntensity == intensity
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SaffronPrimary else OceanNavy)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldAccent else Color.Gray.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedIntensity = intensity
                                    preferencesManager.hapticIntensity = intensity
                                    hapticManager.intensity = intensity
                                    // Trigger test haptic impulse
                                    hapticManager.playStoneTap()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextGold,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        hapticManager.playButtonTap()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text(text = "स्वीकारें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
