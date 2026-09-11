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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.ui.theme.CardSurfaceDark
import com.vanarsena.ramsetu.ui.theme.GoldAccent
import com.vanarsena.ramsetu.ui.theme.SaffronLight
import com.vanarsena.ramsetu.ui.theme.SaffronPrimary
import com.vanarsena.ramsetu.ui.theme.TextGold
import com.vanarsena.ramsetu.ui.theme.TextPrimary

@Composable
fun KathaDialog(
    hapticManager: HapticManager,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "॥ श्री राम सेतु कथा ॥",
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "रामायण के अनुसार, जब मर्यादा पुरुषोत्तम भगवान श्री राम को माता सीता को रावण के चंगुल से मुक्त कराने लंका जाना था, तब समुद्र पार करने के लिए एक विशाल सेतु की आवश्यकता हुई।\n\nवानर सेना के प्रमुख शिल्पी नल और नील को ऋषि मुनियों से यह वरदान प्राप्त था कि उनके द्वारा छुए गए पत्थर जल पर तैरेंगे।\n\nसमस्त वानर सेना ने पत्थरों पर भगवान का पावन नाम 'श्री राम' लिखकर अथाह समुद्र में प्रवाहित किया। प्रभु कृपा से वे पाषाण जलमग्न न होकर तैरने लगे और इस प्रकार 100 योजन लंबा 'राम सेतु' निर्मित हुआ।\n\nकैसे खेलें:\n• नीचे की ओर तैरते पत्थरों पर स्पर्श (Tap) करें।\n• किसी भी पत्थर को समुद्र की गहराइयों में डूबने न दें।\n• जैसे-जैसे संग्रह बढ़ेगा, गति और परीक्षा तीव्र होगी!",
                    color = TextPrimary.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
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
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = "जय श्री राम",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
