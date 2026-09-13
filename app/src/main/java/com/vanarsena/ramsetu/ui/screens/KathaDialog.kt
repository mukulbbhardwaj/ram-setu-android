package com.vanarsena.ramsetu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanarsena.ramsetu.R
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.ui.ProvideAppLanguage
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun KathaDialog(
    language: String,
    hapticManager: HapticManager,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ProvideAppLanguage(language) {
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
                    text = stringResource(R.string.katha_title),
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.katha_body),
                    color = TextPrimary.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.how_to_play_title),
                    color = TextGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.how_to_play_body),
                    color = TextPrimary.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.katha_disclaimer),
                    color = TextGold.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        hapticManager.playButtonTap()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.katha_close),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        }
    }
}
