package com.ltm.finance.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.window.Dialog
import com.ltm.finance.data.model.Budget
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidProgressBar
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal

@Composable
fun BudgetsScreen(
    viewModel: MainViewModel
) {
    val budgets by viewModel.budgets.collectAsState()
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var historyBudget by remember { mutableStateOf<Budget?>(null) }

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
                    text = "QUẢN LÝ NGÂN SÁCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Định mức chi tiêu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Hệ thống tự động theo dõi và cảnh báo khi chạm ngưỡng 80% và 100%",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // List of budgets
        items(budgets, key = { it.id }) { budget ->
            val statusColor = when {
                budget.isOverBudget -> LiquidRose
                budget.isNearLimit -> LiquidAmber
                else -> LiquidCyan
            }

            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = budget.category,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (budget.isOverBudget) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LiquidRose.copy(alpha = 0.2f))
                                        .border(1.dp, LiquidRose.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Vượt hạn mức!", fontSize = 10.sp, color = LiquidRose, fontWeight = FontWeight.Bold)
                                }
                            } else if (budget.isNearLimit) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LiquidAmber.copy(alpha = 0.2f))
                                        .border(1.dp, LiquidAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Cảnh báo 80%", fontSize = 10.sp, color = LiquidAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Edit / History actions
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (budget.adjustments.isNotEmpty()) {
                                IconButton(
                                    onClick = { historyBudget = budget },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Lịch sử điều chỉnh",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { editingBudget = budget },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Điều chỉnh",
                                    tint = LiquidCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidProgressBar(
                        progressPercent = budget.usagePercent,
                        height = 8.dp,
                        barColor = statusColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Đã chi tiêu", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = FinanceEngine.formatVnd(budget.spentAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tỷ lệ", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${budget.usagePercent}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Hạn mức ngân sách", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = FinanceEngine.formatVnd(budget.currentAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // FR-016: Budget Adjustment Dialog
    editingBudget?.let { b ->
        var newAmountText by remember { mutableStateOf(b.currentAmount.toPlainString()) }
        var reasonText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { editingBudget = null }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Điều chỉnh ngân sách ${b.category}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Theo quy định FR-016, mọi thay đổi ngân sách sẽ được lưu vào lịch sử truy vết.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = newAmountText,
                        onValueChange = { newAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền ngân sách mới (VNĐ)", color = TextSecondary) },
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

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Lý do điều chỉnh", color = TextSecondary) },
                        placeholder = { Text("Ví dụ: Bổ sung kinh phí liên hoan lớp...", color = TextTertiary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = LiquidCyan,
                            unfocusedBorderColor = GlassBorderStroke
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingBudget = null }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = newAmountText.toBigDecimalOrNull() ?: b.currentAmount
                                viewModel.adjustBudget(b.id, amt, reasonText.ifBlank { "Người dùng cập nhật" })
                                editingBudget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Xác nhận", color = DarkObsidian, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // FR-016: History View Dialog
    historyBudget?.let { b ->
        Dialog(onDismissRequest = { historyBudget = null }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lịch sử điều chỉnh: ${b.category}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Ngân sách ban đầu: ${FinanceEngine.formatVnd(b.originalAmount)}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    b.adjustments.forEach { adj ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${FinanceEngine.formatVnd(adj.previousAmount)} ➔ ${FinanceEngine.formatVnd(adj.newAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = LiquidCyan
                                    )
                                    Text(adj.modifiedBy, fontSize = 11.sp, color = TextTertiary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lý do: ${adj.reason}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    Button(
                        onClick = { historyBudget = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBaseLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Đóng", color = TextPrimary)
                    }
                }
            }
        }
    }
}

