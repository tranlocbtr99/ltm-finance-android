package com.ltm.finance.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.data.model.TransactionType
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidProgressBar
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(
    viewModel: MainViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val currentMonth = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) }

    val monthIncome = remember(transactions) { FinanceEngine.calculateMonthIncome(transactions, currentMonth) }
    val monthExpense = remember(transactions) { FinanceEngine.calculateMonthExpense(transactions, currentMonth) }
    val netSavings = monthIncome.subtract(monthExpense)

    // Calculate category expense breakdown
    val categoryExpenses = remember(transactions) {
        transactions
            .filter { it.type == TransactionType.EXPENSE && it.date.startsWith(currentMonth) }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) } }
            .toList()
            .sortedByDescending { it.second }
    }

    val savingsRate = if (monthIncome > BigDecimal.ZERO) {
        netSavings.multiply(BigDecimal(100)).divide(monthIncome, 0, RoundingMode.HALF_UP).toInt().coerceAtLeast(0)
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "BÁO CÁO TÀI CHÍNH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Phân tích dòng tiền & Cơ cấu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Summary Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text("TỔNG QUAN THÁNG NÀY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = LiquidEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thu nhập", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(FinanceEngine.formatVnd(monthIncome), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LiquidEmerald)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = LiquidRose, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chi tiêu", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(FinanceEngine.formatVnd(monthExpense), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LiquidRose)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tỷ lệ tiết kiệm", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$savingsRate%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LiquidCyan)
                        }
                    }
                }
            }
        }

        // Category Breakdown Card
        item {
            Text(
                text = "Cơ cấu chi tiêu theo danh mục:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (categoryExpenses.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có dữ liệu chi tiêu trong tháng này.", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(categoryExpenses) { (category, amount) ->
                val percent = if (monthExpense > BigDecimal.ZERO) {
                    amount.multiply(BigDecimal(100)).divide(monthExpense, 0, RoundingMode.HALF_UP).toInt()
                } else 0

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${FinanceEngine.formatVnd(amount)} ($percent%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LiquidCyan)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidProgressBar(
                            progressPercent = percent,
                            height = 6.dp,
                            barColor = LiquidCyan
                        )
                    }
                }
            }
        }
    }
}
