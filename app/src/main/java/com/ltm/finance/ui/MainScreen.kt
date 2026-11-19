package com.ltm.finance.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ltm.finance.ui.components.LiquidGlassBackground
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.screens.ai.AiAssistantScreen
import com.ltm.finance.ui.screens.assets_debts.AccountsDebtsScreen
import com.ltm.finance.ui.screens.budgets.BudgetsScreen
import com.ltm.finance.ui.screens.forecast_simulation.ForecastSimulationScreen
import com.ltm.finance.ui.screens.overview.OverviewScreen
import com.ltm.finance.ui.screens.research.ResearchCenterScreen
import com.ltm.finance.ui.screens.transactions.AddTransactionDialog
import com.ltm.finance.ui.screens.transactions.TransactionsScreen
import com.ltm.finance.ui.theme.*

enum class NavigationTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    OVERVIEW("Tổng quan", Icons.Outlined.Home, Icons.Filled.Home),
    TRANSACTIONS("Giao dịch", Icons.Outlined.ReceiptLong, Icons.Filled.ReceiptLong),
    BUDGETS("Ngân sách", Icons.Outlined.PieChart, Icons.Filled.PieChart),
    ACCOUNTS("Tài sản", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    AI("AI", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome)
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(NavigationTab.OVERVIEW) }
    var showForecastSimulation by remember { mutableStateOf(false) }
    var showResearchCenter by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }

    val accounts by viewModel.accounts.collectAsState()

    LiquidGlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                LiquidGlassBottomBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        showForecastSimulation = false
                        showResearchCenter = false
                        currentTab = it
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Secondary screens or Primary Tab
                when {
                    showForecastSimulation -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Back header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showForecastSimulation = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                                }
                                Text("Quay lại", fontSize = 14.sp, color = TextPrimary)
                            }
                            ForecastSimulationScreen(viewModel = viewModel)
                        }
                    }

                    showResearchCenter -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showResearchCenter = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                                }
                                Text("Quay lại", fontSize = 14.sp, color = TextPrimary)
                            }
                            ResearchCenterScreen(viewModel = viewModel)
                        }
                    }

                    else -> {
                        // Main Tab Views
                        when (currentTab) {
                            NavigationTab.OVERVIEW -> OverviewScreen(
                                viewModel = viewModel,
                                onNavigateToTransactions = { currentTab = NavigationTab.TRANSACTIONS },
                                onNavigateToAi = { currentTab = NavigationTab.AI },
                                onNavigateToForecast = { showForecastSimulation = true },
                                onNavigateToSimulation = { showForecastSimulation = true },
                                onOpenAddTransaction = { showAddTransactionDialog = true }
                            )
                            NavigationTab.TRANSACTIONS -> TransactionsScreen(
                                viewModel = viewModel,
                                onOpenAddTransaction = { showAddTransactionDialog = true }
                            )
                            NavigationTab.BUDGETS -> BudgetsScreen(
                                viewModel = viewModel
                            )
                            NavigationTab.ACCOUNTS -> AccountsDebtsScreen(
                                viewModel = viewModel
                            )
                            NavigationTab.AI -> AiAssistantScreen(
                                viewModel = viewModel,
                                onNavigateToSimulation = { showForecastSimulation = true }
                            )
                        }
                    }
                }

                // Top right research center trigger pill on overview screen
                if (!showForecastSimulation && !showResearchCenter && currentTab == NavigationTab.OVERVIEW) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 18.dp, end = 74.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LiquidPurple.copy(alpha = 0.2f))
                            .border(1.dp, LiquidPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { showResearchCenter = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = LiquidPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nghiên cứu", fontSize = 11.sp, color = LiquidPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Add Transaction Dialog (FR-003, FR-004, FR-005)
        if (showAddTransactionDialog) {
            AddTransactionDialog(
                accounts = accounts,
                onDismiss = { showAddTransactionDialog = false },
                onSave = { type, amount, date, accountId, targetAccountId, category, note ->
                    viewModel.addTransaction(type, amount, date, accountId, targetAccountId, category, note)
                }
            )
        }
    }
}

@Composable
fun LiquidGlassBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating iOS Liquid Glass Pill Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.78f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassBorderTopHighlight,
                            GlassBorderStroke.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab
                    val activeColor = when (tab) {
                        NavigationTab.AI -> LiquidIndigo
                        NavigationTab.BUDGETS -> LiquidCyan
                        NavigationTab.TRANSACTIONS -> LiquidEmerald
                        NavigationTab.ACCOUNTS -> LiquidAmber
                        else -> LiquidBlue
                    }

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) activeColor else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) activeColor else TextSecondary
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

