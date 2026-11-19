package com.ltm.finance.ui.screens.assets_debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.data.model.AccountType
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidSegmentedControl
import com.ltm.finance.ui.theme.*

@Composable
fun AccountsDebtsScreen(
    viewModel: MainViewModel
) {
    val accounts by viewModel.accounts.collectAsState()
    val debts by viewModel.debts.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tài khoản & Tài sản", "Khoản nợ & Trả góp")

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
                    text = "DANH MỤC TÀI CHÍNH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Tài sản & Nghĩa vụ nợ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Tab switcher
        item {
            LiquidSegmentedControl(
                items = tabs,
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )
        }

        if (selectedTab == 0) {
            // Summary Banner: Total Assets vs Liquid Cash (FR-010)
            item {
                val totalAssets = FinanceEngine.calculateTotalAssets(accounts)
                val liquidCash = FinanceEngine.calculateLiquidCash(accounts)

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = LiquidEmerald.copy(alpha = 0.15f),
                    borderColor = LiquidEmerald.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tổng giá trị tài sản", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FinanceEngine.formatVnd(totalAssets),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Khả dụng ngay (Thanh khoản)", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FinanceEngine.formatVnd(liquidCash),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LiquidEmerald
                            )
                        }
                    }
                }
            }

            // Accounts List
            items(accounts, key = { it.id }) { acc ->
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (acc.isLiquid) LiquidCyan.copy(alpha = 0.2f) else LiquidAmber.copy(alpha = 0.2f))
                                    .border(1.dp, if (acc.isLiquid) LiquidCyan.copy(alpha = 0.5f) else LiquidAmber.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (acc.type) {
                                        AccountType.BANK -> Icons.Default.AccountBalance
                                        AccountType.E_WALLET -> Icons.Default.Smartphone
                                        AccountType.SAVINGS -> Icons.Default.Savings
                                        else -> Icons.Default.Money
                                    },
                                    contentDescription = null,
                                    tint = if (acc.isLiquid) LiquidCyan else LiquidAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = acc.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${acc.provider} • ${acc.type.displayName}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                if (!acc.isLiquid && acc.maturityDate != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Khóa đến: ${acc.maturityDate} (Lãi ${acc.interestRateYearly}%/năm)",
                                        fontSize = 11.sp,
                                        color = LiquidAmber
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FinanceEngine.formatVnd(acc.balance),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (acc.isLiquid) LiquidEmerald.copy(alpha = 0.15f) else LiquidAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (acc.isLiquid) "Thanh khoản" else "Tiết kiệm khóa",
                                    fontSize = 9.sp,
                                    color = if (acc.isLiquid) LiquidEmerald else LiquidAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Debts List (FR-011, FR-012, FR-013)
            val totalDebts = FinanceEngine.calculateTotalDebts(debts)
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = LiquidRose.copy(alpha = 0.15f),
                    borderColor = LiquidRose.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text("Tổng dư nợ hiện tại", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FinanceEngine.formatVnd(totalDebts),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LiquidRose
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hạn mức tín dụng không được tính vào tài sản của người dùng (theo FR-013).",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            items(debts, key = { it.id }) { debt ->
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
                            Column {
                                Text(
                                    text = debt.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${debt.type.displayName} • Lãi suất ${debt.interestRateYearly}%/năm",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FinanceEngine.formatVnd(debt.currentBalance),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidRose
                                )
                                Text(
                                    text = "Gốc ban đầu: ${FinanceEngine.formatVnd(debt.initialAmount)}",
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                            }
                        }

                        // Repayment Schedules (FR-012)
                        if (debt.schedules.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Lịch thanh toán kỳ tới:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            debt.schedules.forEach { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Ngày đến hạn: ${s.dueDate}", fontSize = 12.sp, color = TextPrimary)
                                        Text("Gốc: ${FinanceEngine.formatVnd(s.principal)}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text(
                                        text = "${FinanceEngine.formatVnd(s.totalDue)} (${s.status.displayName})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidRose
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

