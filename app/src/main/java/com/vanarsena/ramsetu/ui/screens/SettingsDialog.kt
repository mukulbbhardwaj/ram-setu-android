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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.AudioEngine
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.HapticIntensity
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.OceanNavy
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.SindoorRed
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun SettingsDialog(
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    audioEngine: AudioEngine,
    language: String,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isMusicEnabled by remember { mutableStateOf(preferencesManager.isMusicEnabled) }
    var isSoundEnabled by remember { mutableStateOf(preferencesManager.isSoundEnabled) }
    var selectedIntensity by remember { mutableStateOf(preferencesManager.hapticIntensity) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(
            hapticManager = hapticManager,
            onDismiss = { showPrivacyPolicy = false }
        )
    }

    if (showResetConfirm) {
        ResetDataDialog(
            hapticManager = hapticManager,
            onConfirm = {
                preferencesManager.clearLocalData()
                isMusicEnabled = preferencesManager.isMusicEnabled
                isSoundEnabled = preferencesManager.isSoundEnabled
                selectedIntensity = preferencesManager.hapticIntensity
                hapticManager.intensity = selectedIntensity
                audioEngine.isMusicEnabled = isMusicEnabled
                audioEngine.isSoundEnabled = isSoundEnabled
                onLanguageChange(preferencesManager.language)
                showResetConfirm = false
            },
            onDismiss = { showResetConfirm = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurfaceDark)
                .border(2.dp, GoldAccent.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                SettingsToggleRow(
                    icon = R.drawable.ic_music_note,
                    iconDescription = stringResource(R.string.music),
                    label = stringResource(R.string.music),
                    checked = isMusicEnabled,
                    onCheckedChange = {
                        isMusicEnabled = it
                        preferencesManager.isMusicEnabled = it
                        audioEngine.isMusicEnabled = it
                        hapticManager.playButtonTap()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    icon = if (isSoundEnabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off,
                    iconDescription = stringResource(R.string.sound),
                    label = stringResource(R.string.sound_effects),
                    checked = isSoundEnabled,
                    onCheckedChange = {
                        isSoundEnabled = it
                        preferencesManager.isSoundEnabled = it
                        audioEngine.isSoundEnabled = it
                        hapticManager.playButtonTap()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vibration),
                        contentDescription = stringResource(R.string.haptics),
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "  ${stringResource(R.string.haptic_strength)}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val levels = listOf(
                        HapticIntensity.OFF to stringResource(R.string.haptic_off),
                        HapticIntensity.SUBTLE to stringResource(R.string.haptic_subtle),
                        HapticIntensity.MEDIUM to stringResource(R.string.haptic_medium),
                        HapticIntensity.STRONG to stringResource(R.string.haptic_strong)
                    )

                    levels.forEach { (intensity, label) ->
                        val isSelected = selectedIntensity == intensity
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
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
                                    hapticManager.playStoneTap()
                                },
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

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.language),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageChip(
                        label = stringResource(R.string.language_hindi),
                        selected = language == "hi",
                        onClick = {
                            onLanguageChange("hi")
                            hapticManager.playButtonTap()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageChip(
                        label = stringResource(R.string.language_english),
                        selected = language == "en",
                        onClick = {
                            onLanguageChange("en")
                            hapticManager.playButtonTap()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.privacy_policy),
                    color = TextGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            hapticManager.playButtonTap()
                            showPrivacyPolicy = true
                        }
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.reset_data),
                    color = SindoorRed.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            hapticManager.playButtonTap()
                            showResetConfirm = true
                        }
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: Int,
    iconDescription: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = iconDescription,
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "  $label",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SaffronPrimary,
                checkedTrackColor = GoldAccent
            )
        )
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SaffronPrimary else OceanNavy)
            .border(
                1.dp,
                if (selected) GoldAccent else Color.Gray.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextGold,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ResetDataDialog(
    hapticManager: HapticManager,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = stringResource(R.string.reset_data_title),
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.reset_data_body),
                    color = TextPrimary.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        hapticManager.playButtonTap()
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SindoorRed),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reset_data_confirm),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        hapticManager.playButtonTap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reset_data_cancel),
                        color = TextGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
