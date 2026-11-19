package com.ltm.finance.ui.screens.forecast_simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidSegmentedControl
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal

@Composable
fun ForecastSimulationScreen(
    viewModel: MainViewModel
) {
    var mainTab by remember { mutableIntStateOf(0) }
    // Concise tab titles to prevent text wrapping on mobile
    val mainTabs = listOf("Dự báo dòng tiền", "Mô phỏng mua sắm")

    val forecast by viewModel.currentForecast.collectAsState()
    val simulationResult by viewModel.purchaseSimulation.collectAsState()

    var itemName by remember { mutableStateOf("Laptop học tập") }
    var itemPriceText by remember { mutableStateOf("20000000") }
    var installmentMonths by remember { mutableIntStateOf(6) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "DỰ BÁO & MÔ PHỎNG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Lập kế hoạch tương lai",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        item {
            LiquidSegmentedControl(
                items = mainTabs,
                selectedIndex = mainTab,
                onItemSelected = { mainTab = it }
            )
        }

        if (mainTab == 0) {
            // 1. Forecast Periods Filter with interactive click
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30 to "30 ngày", 90 to "90 ngày", 365 to "12 tháng").forEach { (days, label) ->
                        val isSelected = forecast.daysCount == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) LiquidCyan.copy(alpha = 0.25f) else GlassBaseLight)
                                .border(1.dp, if (isSelected) LiquidCyan else GlassBorderStroke, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setForecastDays(days) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) LiquidCyan else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 2. Forecast Summary Cards
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "KẾT QUẢ DỰ BÁO ${forecast.daysCount} NGÀY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Số dư đầu", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                Text(
                                    text = FinanceEngine.formatVnd(forecast.startBalance),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Thấp nhất", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                Text(
                                    text = FinanceEngine.formatVnd(forecast.minBalance),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forecast.minBalance < BigDecimal.ZERO) LiquidRose else LiquidCyan,
                                    maxLines = 1
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Dự kiến cuối", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                Text(
                                    text = FinanceEngine.formatVnd(forecast.endBalance),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forecast.endBalance >= forecast.startBalance) LiquidEmerald else LiquidRose,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. Shortage Alert (FR-020)
            if (forecast.alerts.isNotEmpty()) {
                items(forecast.alerts) { alert ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = LiquidRose.copy(alpha = 0.2f),
                        borderColor = LiquidRose.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LiquidRose,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NGUY CƠ THIẾU TIỀN (${alert.date})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidRose,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Thiếu khoảng ${FinanceEngine.formatVnd(alert.estimatedShortage)}. ${alert.reason}",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Khuyến nghị:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            alert.solutions.forEach { sol ->
                                Text("• $sol", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // 4. Daily Timeline Items
            item {
                Text(
                    text = "Dòng tiền theo ngày:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(forecast.dailyForecasts.take(20)) { day ->
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = if (day.isShortage) LiquidRose.copy(alpha = 0.12f) else GlassBase
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(day.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = day.notes.firstOrNull() ?: "Duy trì sinh hoạt đều",
                                fontSize = 11.sp,
                                color = if (day.isShortage) LiquidRose else TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = FinanceEngine.formatVnd(day.projectedBalance),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day.isShortage) LiquidRose else LiquidCyan,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            // TAB 2: Mô phỏng quyết định mua sắm (FR-021)
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Thiết lập kịch bản mua sắm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Tên sản phẩm", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = LiquidCyan,
                                unfocusedBorderColor = GlassBorderStroke
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = itemPriceText,
                            onValueChange = { itemPriceText = it.filter { c -> c.isDigit() } },
                            label = { Text("Giá tiền (VNĐ)", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = LiquidCyan,
                                unfocusedBorderColor = GlassBorderStroke
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val price = itemPriceText.toBigDecimalOrNull() ?: BigDecimal("20000000")
                                viewModel.runPurchaseSimulation(itemName, price, installmentMonths)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Chạy mô phỏng 4 phương án", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }

            // Results of simulation
            simulationResult?.let { sim ->
                item {
                    Text(
                        text = "Khuyến nghị từ Simulation Engine:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidCyan
                    )
                }

                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = LiquidIndigo.copy(alpha = 0.2f),
                        borderColor = LiquidIndigo.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LiquidCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(sim.recommendation, fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }

                items(sim.options) { opt ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        borderColor = if (opt.isAffordable) GlassBorderStroke else LiquidRose.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(opt.optionName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (opt.isAffordable) LiquidEmerald.copy(alpha = 0.2f) else LiquidRose.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (opt.isAffordable) "Khả thi" else "Rủi ro",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (opt.isAffordable) LiquidEmerald else LiquidRose,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text("• Trả ngay: ${FinanceEngine.formatVnd(opt.immediatePayment)}", fontSize = 12.sp, color = TextSecondary)
                            if (opt.monthlyPayment > BigDecimal.ZERO) {
                                Text("• Trả hàng tháng: ${FinanceEngine.formatVnd(opt.monthlyPayment)}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("• Số dư còn lại: ${FinanceEngine.formatVnd(opt.remainingBalanceAfterPayment)}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(opt.riskSummary, fontSize = 11.sp, color = TextTertiary, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
