package com.ltm.finance.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.ltm.finance.data.model.FinancialAccount
import com.ltm.finance.data.model.Transaction
import com.ltm.finance.data.model.TransactionType
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidSegmentedControl
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddTransactionDialog(
    accounts: List<FinancialAccount>,
    onDismiss: () -> Unit,
    onSave: (type: TransactionType, amount: BigDecimal, date: String, accountId: String, targetAccountId: String?, category: String, note: String) -> Unit
) {
    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    val types = listOf("Chi tiêu", "Thu nhập", "Chuyển tiền")

    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Transaction.DEFAULT_EXPENSE_CATEGORIES.first()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedTargetAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }

    val dateStr = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(26.dp),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ghi chép giao dịch mới",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Type switcher
                LiquidSegmentedControl(
                    items = types,
                    selectedIndex = selectedTypeIndex,
                    onItemSelected = {
                        selectedTypeIndex = it
                        selectedCategory = when (it) {
                            0 -> Transaction.DEFAULT_EXPENSE_CATEGORIES.first()
                            1 -> Transaction.DEFAULT_INCOME_CATEGORIES.first()
                            else -> "Chuyển tiền nội bộ"
                        }
                    }
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Số tiền (VNĐ)", color = TextSecondary) },
                    placeholder = { Text("Ví dụ: 50000", color = TextTertiary) },
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

                // Category selector (for Expense / Income)
                if (selectedTypeIndex != 2) {
                    val categories = if (selectedTypeIndex == 0) Transaction.DEFAULT_EXPENSE_CATEGORIES else Transaction.DEFAULT_INCOME_CATEGORIES
                    Column {
                        Text("Danh mục:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                val isSel = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) LiquidCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isSel) LiquidCyan else GlassBorderStroke, RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        color = if (isSel) LiquidCyan else TextSecondary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Account selection
                Column {
                    Text(
                        text = if (selectedTypeIndex == 2) "Tài khoản nguồn (Trừ tiền):" else "Tài khoản thanh toán:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.take(3).forEach { acc ->
                            val isSel = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) LiquidCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isSel) LiquidCyan else GlassBorderStroke, RoundedCornerShape(12.dp))
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name.take(12),
                                    fontSize = 11.sp,
                                    color = if (isSel) LiquidCyan else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Target account (if Transfer)
                if (selectedTypeIndex == 2) {
                    Column {
                        Text("Tài khoản đích (Cộng tiền):", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            accounts.filter { it.id != selectedAccountId }.take(3).forEach { acc ->
                                val isSel = selectedTargetAccountId == acc.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) LiquidEmerald.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isSel) LiquidEmerald else GlassBorderStroke, RoundedCornerShape(12.dp))
                                        .clickable { selectedTargetAccountId = acc.id }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = acc.name.take(12),
                                        fontSize = 11.sp,
                                        color = if (isSel) LiquidEmerald else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Note input
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Ghi chú / Nội dung", color = TextSecondary) },
                    placeholder = { Text("Ví dụ: Tiền cơm trưa, mua giáo trình...", color = TextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = GlassBorderStroke
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            if (parsedAmount > BigDecimal.ZERO) {
                                val txType = when (selectedTypeIndex) {
                                    1 -> TransactionType.INCOME
                                    2 -> TransactionType.TRANSFER
                                    else -> TransactionType.EXPENSE
                                }
                                onSave(
                                    txType,
                                    parsedAmount,
                                    dateStr,
                                    selectedAccountId,
                                    if (txType == TransactionType.TRANSFER) selectedTargetAccountId else null,
                                    selectedCategory,
                                    noteText
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu giao dịch", color = DarkObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

