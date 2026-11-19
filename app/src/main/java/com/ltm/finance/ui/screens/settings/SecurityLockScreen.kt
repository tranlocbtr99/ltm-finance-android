package com.ltm.finance.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.ui.components.LiquidGlassBackground
import com.ltm.finance.ui.theme.*

@Composable
fun SecurityLockScreen(
    onVerifyPin: (String) -> Boolean
) {
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(LiquidCyan.copy(alpha = 0.2f))
                    .border(1.dp, LiquidCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = LiquidCyan, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("LTM Finance", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isError) "Mã PIN không chính xác, thử lại!" else "Nhập mã PIN để mở khóa",
                fontSize = 13.sp,
                color = if (isError) LiquidRose else TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4 Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val isFilled = i < pinText.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) LiquidCyan else GlassBaseLight)
                            .border(1.dp, if (isFilled) LiquidCyan else GlassBorderStroke, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Keypad (1 to 9, 0, Backspace)
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(68.dp))
                            } else if (key == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(GlassBaseLight)
                                        .border(1.dp, GlassBorderStroke, CircleShape)
                                        .clickable {
                                            if (pinText.isNotEmpty()) {
                                                pinText = pinText.dropLast(1)
                                                isError = false
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(GlassBase)
                                        .border(1.dp, GlassBorderStroke, CircleShape)
                                        .clickable {
                                            if (pinText.length < 4) {
                                                val nextPin = pinText + key
                                                pinText = nextPin
                                                isError = false
                                                if (nextPin.length == 4) {
                                                    if (!onVerifyPin(nextPin)) {
                                                        isError = true
                                                        pinText = ""
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(key, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
