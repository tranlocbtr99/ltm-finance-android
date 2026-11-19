package com.ltm.finance.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.data.model.TransactionType
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidSegmentedControl
import com.ltm.finance.ui.theme.*

@Composable
fun TransactionsScreen(
    viewModel: MainViewModel,
    onOpenAddTransaction: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    // Concise filter titles to fit on all screens
    val filterOptions = listOf("Tất cả", "Chi tiêu", "Thu nhập", "Chuyển khoản")

    val filteredList = remember(transactions, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> transactions.filter { it.type == TransactionType.EXPENSE }
            2 -> transactions.filter { it.type == TransactionType.INCOME }
            3 -> transactions.filter { it.type == TransactionType.TRANSFER }
            else -> transactions
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "SỔ GIAO DỊCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = LiquidCyan
                    )
                    Text(
                        text = "Nhật ký thu chi",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Add button pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(LiquidCyan)
                        .clickable { onOpenAddTransaction() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DarkObsidian,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Ghi chép",
                            color = DarkObsidian,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // iOS Liquid Segmented Filter
        item {
            LiquidSegmentedControl(
                items = filterOptions,
                selectedIndex = selectedFilterIndex,
                onItemSelected = { selectedFilterIndex = it }
            )
        }

        // Transactions List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có giao dịch trong danh mục này",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { tx ->
                val account = accounts.find { it.id == tx.accountId }
                val targetAccount = if (tx.targetAccountId != null) accounts.find { it.id == tx.targetAccountId } else null

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Category Icon Bubble
                            val iconColor = when (tx.type) {
                                TransactionType.INCOME -> LiquidEmerald
                                TransactionType.EXPENSE -> LiquidRose
                                TransactionType.TRANSFER -> LiquidCyan
                            }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.18f))
                                    .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        TransactionType.INCOME -> Icons.Default.ArrowDownward
                                        TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                    },
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.category,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = if (tx.type == TransactionType.TRANSFER && targetAccount != null) {
                                        "${account?.name ?: "Ví"} ➔ ${targetAccount.name}"
                                    } else {
                                        tx.note.ifBlank { account?.name ?: "Ví" }
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = tx.date,
                                    fontSize = 10.sp,
                                    color = TextTertiary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Amount
                        val amountPrefix = when (tx.type) {
                            TransactionType.INCOME -> "+"
                            TransactionType.EXPENSE -> "-"
                            TransactionType.TRANSFER -> ""
                        }
                        val amountColor = when (tx.type) {
                            TransactionType.INCOME -> LiquidEmerald
                            TransactionType.EXPENSE -> LiquidRose
                            TransactionType.TRANSFER -> LiquidCyan
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$amountPrefix${FinanceEngine.formatVnd(tx.amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor,
                                maxLines = 1
                            )
                            if (tx.type == TransactionType.TRANSFER) {
                                Text(
                                    text = "Chuyển nội bộ",
                                    fontSize = 9.sp,
                                    color = TextTertiary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
