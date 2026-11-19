package com.ltm.finance.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        // 2. Hero Glass Card: Net Worth & Clean Available Cash
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFF131D31).copy(alpha = 0.75f)
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
                            text = "TÀI SẢN RÒNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(LiquidEmerald.copy(alpha = 0.2f))
                                .border(1.dp, LiquidEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Finance Engine",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidEmerald,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = FinanceEngine.formatVnd(overview.netWorth),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Liquid Cash vs Debt Breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(LiquidEmerald)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Khả dụng", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FinanceEngine.formatVnd(overview.liquidCash),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidEmerald,
                                maxLines = 1
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(LiquidRose)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Tổng nợ", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FinanceEngine.formatVnd(overview.totalDebts),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidRose,
                                maxLines = 1
                            )
                        }
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
