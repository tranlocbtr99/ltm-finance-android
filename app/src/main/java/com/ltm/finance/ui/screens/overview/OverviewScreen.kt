package com.ltm.finance.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.data.model.TransactionType
import com.ltm.finance.data.storage.CloudSyncStatus
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.*
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal

@Composable
fun OverviewScreen(
    viewModel: MainViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToForecast: () -> Unit,
    onNavigateToSimulation: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenAddTransaction: () -> Unit
) {
    val overview by viewModel.overviewState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var isBalanceHidden by remember { mutableStateOf(false) }
    var selectedChartPeriod by remember { mutableIntStateOf(1) } // 0: Tuần, 1: Tháng, 2: Quý, 3: Năm
    val chartPeriods = listOf("Tuần", "Tháng", "Quý", "Năm")
    val recentTransactions = remember(transactions) { transactions.take(5) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. iOS Glass Header with Settings Icon (Research is hidden from normal user UI)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LTM FINANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = LiquidCyan
                    )
                    Text(
                        text = "Chào ${userProfile.displayName.split(" ").last()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${userProfile.university} • Năm ${userProfile.academicYear}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val cloudState by viewModel.cloudSyncState.collectAsState()
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToSettings() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when (cloudState.status) {
                                        CloudSyncStatus.SYNCED -> LiquidEmerald
                                        CloudSyncStatus.SYNCING -> LiquidCyan
                                        CloudSyncStatus.ERROR -> LiquidRose
                                        CloudSyncStatus.OFFLINE -> TextSecondary
                                    }
                                )
                        )
                        Text(
                            text = when (cloudState.status) {
                                CloudSyncStatus.SYNCED -> "Đám mây: Đã đồng bộ"
                                CloudSyncStatus.SYNCING -> "Đang lưu lên đám mây..."
                                CloudSyncStatus.ERROR -> "Lỗi đám mây (Chạm để xem)"
                                CloudSyncStatus.OFFLINE -> "Chưa bật đám mây (Chạm để đăng nhập)"
                            },
                            fontSize = 10.sp,
                            color = when (cloudState.status) {
                                CloudSyncStatus.SYNCED -> LiquidEmerald
                                CloudSyncStatus.SYNCING -> LiquidCyan
                                CloudSyncStatus.ERROR -> LiquidRose
                                CloudSyncStatus.OFFLINE -> TextSecondary
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GlassBase)
                            .border(1.dp, GlassBorderStroke, CircleShape)
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LiquidCyan.copy(alpha = 0.2f))
                            .border(1.dp, LiquidCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Hồ sơ",
                            tint = LiquidCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Hero Balance Card (LTMsav Silver / Liquid Glass design)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFF131D31).copy(alpha = 0.85f)
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
                        Text(
                            text = "TIỀN KHẢ DỤNG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        // Eye Toggle Button (LTMsav hero-eye)
                        IconButton(
                            onClick = { isBalanceHidden = !isBalanceHidden },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isBalanceHidden) "Hiện số dư" else "Ẩn số dư",
                                tint = LiquidCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isBalanceHidden) "••••••••" else FinanceEngine.formatVnd(overview.liquidCash),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Split Card (LTMsav silver-balance-secondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, GlassBorderStroke, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tổng tài sản", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceHidden) "••••••" else FinanceEngine.formatVnd(overview.totalAssets),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidEmerald,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(GlassBorderStroke)
                        )

                        Column(
                            modifier = Modifier.weight(1f).padding(start = 12.dp)
                        ) {
                            Text("Nợ cần trả", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceHidden) "••••••" else FinanceEngine.formatVnd(overview.totalDebts),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (overview.totalDebts > BigDecimal.ZERO) LiquidRose else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Two Primary Action Buttons (LTMsav silver-home-actions)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Blue Gradient Button (+ Thêm giao dịch)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF188DFF), Color(0xFF0071ED))
                            )
                        )
                        .border(1.dp, Color(0xFF3D9DFF), RoundedCornerShape(999.dp))
                        .clickable { onOpenAddTransaction() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Thêm giao dịch",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Glass Button (✨ Trợ lý AI)
                LiquidGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onNavigateToAi() },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LiquidCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trợ lý AI",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4. Monthly In/Out Metric Tiles (LTMsav metric-tile)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income Tile
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xFF11733F).copy(alpha = 0.12f),
                    borderColor = LiquidEmerald.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = LiquidEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thu vào tháng", fontSize = 11.sp, color = LiquidEmerald, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBalanceHidden) "••••••" else "+${FinanceEngine.formatVnd(overview.monthIncome)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidEmerald,
                            maxLines = 1
                        )
                    }
                }

                // Expense Tile
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xFFBA3151).copy(alpha = 0.12f),
                    borderColor = LiquidRose.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = LiquidRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chi ra tháng", fontSize = 11.sp, color = LiquidRose, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBalanceHidden) "••••••" else "-${FinanceEngine.formatVnd(overview.monthExpense)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidRose,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. Proactive AI Intelligence Card (if alert exists)
        if (overview.topProactiveRecommendations.isNotEmpty()) {
            val alert = overview.topProactiveRecommendations.first()
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = LiquidIndigo.copy(alpha = 0.20f),
                    borderColor = LiquidIndigo.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LiquidCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CẢNH BÁO AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = LiquidCyan,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = alert.category,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = alert.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = alert.content,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.rejectAiRecommendation(alert.id) }
                            ) {
                                Text("Bỏ qua", color = TextTertiary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { viewModel.applyAiBudgetRecommendation(alert) },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidIndigo),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Tiếp nhận", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Action Glass Pill Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Default.Add,
                    title = "Ghi chép",
                    color = LiquidEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAddTransaction
                )
                QuickActionItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Hỏi AI",
                    color = LiquidIndigo,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAi
                )
                QuickActionItem(
                    icon = Icons.Default.BarChart,
                    title = "Báo cáo",
                    color = LiquidCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
                QuickActionItem(
                    icon = Icons.Default.Calculate,
                    title = "Mô phỏng",
                    color = LiquidAmber,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSimulation
                )
            }
        }

        // 5. Monthly Cashflow & Budget Progress Glass Card
        item {
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
                        Text(
                            text = "NGÂN SÁCH THÁNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = "${overview.budgetUsagePercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (overview.budgetUsagePercent > 100) LiquidRose else if (overview.budgetUsagePercent >= 80) LiquidAmber else LiquidCyan,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LiquidProgressBar(
                        progressPercent = overview.budgetUsagePercent,
                        height = 8.dp,
                        barColor = LiquidCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Chi: ${FinanceEngine.formatVnd(overview.totalBudgetSpent)}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = "Hạn mức: ${FinanceEngine.formatVnd(overview.totalBudget)}",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 6. Upcoming Debt & Cashflow Health Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upcoming Debt Card
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = LiquidRose,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kỳ nợ tới", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val debt = overview.upcomingDebtDueSoon
                        if (debt != null) {
                            Text(
                                text = FinanceEngine.formatVnd(debt.totalDue),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Hạn: ${debt.dueDate}",
                                fontSize = 10.sp,
                                color = LiquidRose,
                                maxLines = 1
                            )
                        } else {
                            Text("Không có nợ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LiquidEmerald, maxLines = 1)
                            Text("Đã hoàn tất", fontSize = 10.sp, color = TextSecondary, maxLines = 1)
                        }
                    }
                }

                // Monthly Savings Card
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = LiquidEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thặng dư tháng", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val isPositive = overview.netMonthlySavings >= BigDecimal.ZERO
                        Text(
                            text = FinanceEngine.formatVnd(overview.netMonthlySavings),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) LiquidEmerald else LiquidRose,
                            maxLines = 1
                        )
                        Text(
                            text = if (isPositive) "Dương an toàn" else "Chi vượt thu!",
                            fontSize = 10.sp,
                            color = if (isPositive) LiquidEmerald else LiquidRose,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 7. Recent Transactions (5 latest - LTMsav style)
        item {
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
                        Text(
                            text = "GIAO DỊCH GẦN ĐÂY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Xem tất cả ➔",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidCyan,
                            modifier = Modifier.clickable { onNavigateToTransactions() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (recentTransactions.isEmpty()) {
                        Text(
                            text = "Chưa có giao dịch nào được ghi chép",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        recentTransactions.forEachIndexed { index, tx ->
                            val iconColor = when (tx.type) {
                                TransactionType.INCOME -> LiquidEmerald
                                TransactionType.EXPENSE -> LiquidRose
                                TransactionType.TRANSFER -> LiquidCyan
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(iconColor.copy(alpha = 0.15f))
                                            .border(1.dp, iconColor.copy(alpha = 0.35f), CircleShape),
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
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tx.category,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tx.note.ifBlank { tx.date },
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                val amountPrefix = when (tx.type) {
                                    TransactionType.INCOME -> "+"
                                    TransactionType.EXPENSE -> "-"
                                    TransactionType.TRANSFER -> ""
                                }
                                Text(
                                    text = "$amountPrefix${FinanceEngine.formatVnd(tx.amount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = iconColor,
                                    maxLines = 1
                                )
                            }
                            if (index < recentTransactions.size - 1) {
                                HorizontalDivider(color = GlassBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.20f))
                    .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}
