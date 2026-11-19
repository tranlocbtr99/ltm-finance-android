package com.ltm.finance.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.ltm.finance.data.model.Budget
import com.ltm.finance.data.model.Transaction
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
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var deleteConfirmBudgetId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(LiquidBlue)
                        .clickable { showAddBudgetDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Thêm", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Empty state
        if (budgets.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChartOutline,
                            contentDescription = null,
                            tint = LiquidCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Chưa có ngân sách nào",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hãy thiết lập định mức chi tiêu cho từng danh mục như Ăn uống, Đi lại, Nhà ở để kiểm soát dòng tiền.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddBudgetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tạo ngân sách đầu tiên", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // List of budgets
            items(budgets, key = { it.id }) { budget ->
                val statusColor = when {
                    budget.isOverBudget -> LiquidRose
                    budget.isNearLimit -> LiquidAmber
                    else -> LiquidCyan
                }

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = budget.category,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (budget.isOverBudget) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiquidRose.copy(alpha = 0.2f))
                                            .border(1.dp, LiquidRose.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text("Vượt!", fontSize = 10.sp, color = LiquidRose, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                } else if (budget.isNearLimit) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiquidAmber.copy(alpha = 0.2f))
                                            .border(1.dp, LiquidAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(">80%", fontSize = 10.sp, color = LiquidAmber, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }

                            // Actions: History, Edit, Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (budget.adjustments.isNotEmpty()) {
                                    IconButton(
                                        onClick = { historyBudget = budget },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = "Lịch sử", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { editingBudget = budget },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = LiquidCyan, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { deleteConfirmBudgetId = budget.id },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                Text("Đã chi", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                Text(
                                    text = FinanceEngine.formatVnd(budget.spentAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    maxLines = 1
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tỷ lệ", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                Text(
                                    text = "${budget.usagePercent}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Hạn mức", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                Text(
                                    text = FinanceEngine.formatVnd(budget.currentAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Thêm ngân sách mới
    if (showAddBudgetDialog) {
        var selectedCat by remember { mutableStateOf(Transaction.DEFAULT_EXPENSE_CATEGORIES.first()) }
        var amountText by remember { mutableStateOf("") }
        var thresholdText by remember { mutableStateOf("80") }

        Dialog(onDismissRequest = { showAddBudgetDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = GlassBaseCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tạo hạn mức ngân sách mới", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Column {
                        Text("Chọn danh mục:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(Transaction.DEFAULT_EXPENSE_CATEGORIES) { cat ->
                                val isSel = selectedCat == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) LiquidBlue.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSel) LiquidBlue else GlassBorderStroke, RoundedCornerShape(10.dp))
                                        .clickable { selectedCat = cat }
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Text(cat, fontSize = 11.sp, color = if (isSel) LiquidBlue else TextSecondary)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Hạn mức tháng (VNĐ)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                        label = { Text("Ngưỡng cảnh báo % (Mặc định 80%)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddBudgetDialog = false }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val amt = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                if (amt > BigDecimal.ZERO) {
                                    viewModel.addBudget(
                                        category = selectedCat,
                                        amount = amt,
                                        warningThreshold = thresholdText.toIntOrNull()?.coerceIn(10, 100) ?: 80
                                    )
                                    showAddBudgetDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Lưu ngân sách", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Sửa ngân sách
    editingBudget?.let { b ->
        var newAmountText by remember { mutableStateOf(b.currentAmount.toPlainString()) }
        var reasonText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { editingBudget = null }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = GlassBaseCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Sửa ngân sách ${b.category}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = newAmountText,
                        onValueChange = { newAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền mới (VNĐ)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Lý do điều chỉnh", color = TextSecondary) },
                        placeholder = { Text("Ví dụ: Cân đối chi tiêu...", color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingBudget = null }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val amt = newAmountText.toBigDecimalOrNull() ?: b.currentAmount
                                viewModel.adjustBudget(b.id, amt, reasonText.ifBlank { "Người dùng cập nhật" })
                                editingBudget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // History dialog
    historyBudget?.let { b ->
        Dialog(onDismissRequest = { historyBudget = null }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .padding(vertical = 14.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = GlassBaseCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Lịch sử: ${b.category}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Gốc ban đầu: ${FinanceEngine.formatVnd(b.originalAmount)}", fontSize = 12.sp, color = TextSecondary)

                    b.adjustments.forEach { adj ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${FinanceEngine.formatVnd(adj.previousAmount)} ➔ ${FinanceEngine.formatVnd(adj.newAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = LiquidBlue,
                                        maxLines = 1
                                    )
                                    Text(adj.modifiedBy, fontSize = 10.sp, color = TextTertiary, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Lý do: ${adj.reason}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { historyBudget = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBaseLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Đóng", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Delete Confirmation
    deleteConfirmBudgetId?.let { id ->
        Dialog(onDismissRequest = { deleteConfirmBudgetId = null }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = GlassBaseCard
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Xóa ngân sách?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidRose)
                    Text("Bạn có chắc chắn muốn xóa ngân sách danh mục này không?", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { deleteConfirmBudgetId = null }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                viewModel.deleteBudget(id)
                                deleteConfirmBudgetId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidRose),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
