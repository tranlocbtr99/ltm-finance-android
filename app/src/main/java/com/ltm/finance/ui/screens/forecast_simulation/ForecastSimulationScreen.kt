package com.ltm.finance.ui.screens.forecast_simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
    val mainTabs = listOf("Dự báo dòng tiền (FR-019)", "Mô phỏng quyết định (FR-021)")

    val forecast by viewModel.currentForecast.collectAsState()
    val simulationResult by viewModel.purchaseSimulation.collectAsState()

    var itemName by remember { mutableStateOf("Laptop phục vụ học tập") }
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
                    text = "DỰ BÁO & MÔ PHỎNG TƯƠNG LAI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Lập kế hoạch chủ động",
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
            // 1. Forecast Periods Filter
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
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) LiquidCyan else TextSecondary
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
                            text = "KẾT QUẢ DỰ BÁO ${forecast.daysCount} NGÀY TỚI",
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
                                Text("Số dư hiện tại", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = FinanceEngine.formatVnd(forecast.startBalance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Điểm thấp nhất", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = FinanceEngine.formatVnd(forecast.minBalance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forecast.minBalance < BigDecimal.ZERO) LiquidRose else LiquidCyan
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Dự kiến kết thúc", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = FinanceEngine.formatVnd(forecast.endBalance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forecast.endBalance > forecast.startBalance) LiquidEmerald else LiquidRose
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
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CẢNH BÁO THIẾU HỤT DÒNG TIỀN (${alert.date})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidRose
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Dự kiến số dư âm khoảng ${FinanceEngine.formatVnd(alert.estimatedShortage)}. Nguyên nhân: ${alert.reason}",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Giải pháp khuyến nghị:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
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
                    text = "Dòng sự kiện tài chính theo ngày:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(forecast.dailyForecasts.take(15)) { day ->
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
                        Column {
                            Text(day.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (day.notes.isNotEmpty()) {
                                Text(
                                    text = day.notes.first(),
                                    fontSize = 11.sp,
                                    color = if (day.isShortage) LiquidRose else TextSecondary
                                )
                            } else {
                                Text("Duy trì chi tiêu sinh hoạt bình thường", fontSize = 11.sp, color = TextTertiary)
                            }
                        }

                        Text(
                            text = FinanceEngine.formatVnd(day.projectedBalance),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day.isShortage) LiquidRose else LiquidCyan
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
                            label = { Text("Tên sản phẩm / Thiết bị", color = TextSecondary) },
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
                            label = { Text("Giá tiền dự kiến (VNĐ)", color = TextSecondary) },
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
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Chạy mô phỏng 4 phương án", color = DarkObsidian, fontWeight = FontWeight.Bold)
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
                                Text(opt.optionName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (opt.isAffordable) LiquidEmerald.copy(alpha = 0.2f) else LiquidRose.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (opt.isAffordable) "Khả thi" else "Rủi ro cao",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (opt.isAffordable) LiquidEmerald else LiquidRose
                                    )
                                }
                            }

                            Text("• Thanh toán ngay: ${FinanceEngine.formatVnd(opt.immediatePayment)}", fontSize = 12.sp, color = TextSecondary)
                            if (opt.monthlyPayment > BigDecimal.ZERO) {
                                Text("• Chi trả hàng tháng: ${FinanceEngine.formatVnd(opt.monthlyPayment)}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("• Số dư còn lại sau quyết định: ${FinanceEngine.formatVnd(opt.remainingBalanceAfterPayment)}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(opt.riskSummary, fontSize = 11.sp, color = TextTertiary, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

