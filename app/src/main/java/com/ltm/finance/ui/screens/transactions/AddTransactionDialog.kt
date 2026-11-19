package com.ltm.finance.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val types = listOf("Chi tiêu", "Thu nhập", "Chuyển khoản")

    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Transaction.DEFAULT_EXPENSE_CATEGORIES.first()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedTargetAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dateStr = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = GlassBaseCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ghi chép giao dịch",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Type switcher
                LiquidSegmentedControl(
                    items = types,
                    selectedIndex = selectedTypeIndex,
                    onItemSelected = {
                        selectedTypeIndex = it
                        validationError = null
                        selectedCategory = when (it) {
                            0 -> Transaction.DEFAULT_EXPENSE_CATEGORIES.first()
                            1 -> Transaction.DEFAULT_INCOME_CATEGORIES.first()
                            else -> "Chuyển khoản nội bộ"
                        }
                    }
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() }
                        validationError = null
                    },
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

                // Category selector with horizontal scroll (LazyRow)
                if (selectedTypeIndex != 2) {
                    val categories = if (selectedTypeIndex == 0) Transaction.DEFAULT_EXPENSE_CATEGORIES else Transaction.DEFAULT_INCOME_CATEGORIES
                    Column {
                        Text("Danh mục:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(categories) { cat ->
                                val isSel = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) LiquidBlue.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSel) LiquidBlue else GlassBorderStroke, RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        color = if (isSel) LiquidBlue else TextSecondary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Account selection with LazyRow
                Column {
                    Text(
                        text = if (selectedTypeIndex == 2) "Từ tài khoản (Nguồn):" else "Tài khoản thanh toán:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(accounts) { acc ->
                            val isSel = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) LiquidBlue.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                                    .border(1.dp, if (isSel) LiquidBlue else GlassBorderStroke, RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedAccountId = acc.id
                                        validationError = null
                                    }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    fontSize = 11.sp,
                                    color = if (isSel) LiquidBlue else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Target account (for Transfer)
                if (selectedTypeIndex == 2) {
                    Column {
                        Text("Đến tài khoản (Đích):", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(accounts.filter { it.id != selectedAccountId }) { acc ->
                                val isSel = selectedTargetAccountId == acc.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) LiquidEmerald.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSel) LiquidEmerald else GlassBorderStroke, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedTargetAccountId = acc.id
                                            validationError = null
                                        }
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = acc.name,
                                        fontSize = 11.sp,
                                        color = if (isSel) LiquidEmerald else TextSecondary,
                                        maxLines = 1
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
                    label = { Text("Ghi chú", color = TextSecondary) },
                    placeholder = { Text("Nội dung giao dịch...", color = TextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = GlassBorderStroke
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error alert
                validationError?.let { err ->
                    Text(text = err, color = LiquidRose, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            if (parsedAmount <= BigDecimal.ZERO) {
                                validationError = "Vui lòng nhập số tiền hợp lệ (> 0)"
                                return@Button
                            }
                            if (selectedTypeIndex == 2 && selectedAccountId == selectedTargetAccountId) {
                                validationError = "Tài khoản nguồn và đích phải khác nhau"
                                return@Button
                            }
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}
