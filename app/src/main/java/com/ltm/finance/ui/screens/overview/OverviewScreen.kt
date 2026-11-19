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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onOpenAddTransaction: () -> Unit
) {
    val overview by viewModel.overviewState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. iOS Glass Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LTM FINANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = LiquidCyan
                    )
                    Text(
                        text = "Chào ${userProfile.displayName.split(" ").last()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${userProfile.university} • Năm ${userProfile.academicYear}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Student Avatar Pill
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(GlassBase)
                        .border(1.dp, GlassBorderStroke, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Sinh viên",
                        tint = LiquidCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Hero Glass Card: The 5 Core PRD Answers
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = Color(0xFF131D31).copy(alpha = 0.70f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TÀI SẢN RÒNG HIỆN HÀNH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(LiquidEmerald.copy(alpha = 0.2f))
                                .border(1.dp, LiquidEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Finance Engine",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = FinanceEngine.formatVnd(overview.netWorth),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Liquid Cash vs Debt Breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LiquidEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tiền mặt khả dụng", fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = FinanceEngine.formatVnd(overview.liquidCash),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidEmerald
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LiquidRose)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tổng nghĩa vụ nợ", fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = FinanceEngine.formatVnd(overview.totalDebts),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidRose
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
                    shape = RoundedCornerShape(22.dp),
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CẢNH BÁO AI CHỦ ĐỘNG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = LiquidCyan
                                )
                            }
                            Text(
                                text = alert.category,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = alert.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = alert.content,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.rejectAiRecommendation(alert.id) }
                            ) {
                                Text("Bỏ qua", color = TextTertiary, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.applyAiBudgetRecommendation(alert) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LiquidIndigo
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Tiếp nhận", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    icon = Icons.Default.Timeline,
                    title = "Dự báo",
                    color = LiquidCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToForecast
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

        // 5. Monthly Cashflow & Budget Progress Glass Cards
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NGÂN SÁCH THÁNG NÀY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${overview.budgetUsagePercent}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (overview.budgetUsagePercent > 100) LiquidRose else if (overview.budgetUsagePercent >= 80) LiquidAmber else LiquidCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidProgressBar(
                        progressPercent = overview.budgetUsagePercent,
                        height = 10.dp,
                        barColor = LiquidCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Đã chi: ${FinanceEngine.formatVnd(overview.totalBudgetSpent)}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Hạn mức: ${FinanceEngine.formatVnd(overview.totalBudget)}",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 6. Upcoming Debt & Cashflow Health Question Answers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Upcoming Debt Card
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = LiquidRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kỳ nợ sắp tới", fontSize = 11.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val debt = overview.upcomingDebtDueSoon
                        if (debt != null) {
                            Text(
                                text = FinanceEngine.formatVnd(debt.totalDue),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hạn: ${debt.dueDate}",
                                fontSize = 11.sp,
                                color = LiquidRose
                            )
                        } else {
                            Text("Không có nợ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LiquidEmerald)
                            Text("Tất cả đã thanh toán", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                // Monthly Savings Card
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = LiquidEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thặng dư tháng", fontSize = 11.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val isPositive = overview.netMonthlySavings >= BigDecimal.ZERO
                        Text(
                            text = FinanceEngine.formatVnd(overview.netMonthlySavings),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) LiquidEmerald else LiquidRose
                        )
                        Text(
                            text = if (isPositive) "Thu > Chi an toàn" else "Chi vượt thu!",
                            fontSize = 11.sp,
                            color = if (isPositive) LiquidEmerald else LiquidRose
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
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.20f))
                    .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
