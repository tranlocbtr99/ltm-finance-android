package com.ltm.finance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.data.model.UserRole
import com.ltm.finance.ui.components.AppUpdateDialog
import com.ltm.finance.ui.components.LiquidGlassBackground
import com.ltm.finance.ui.screens.admin.AdminDashboardScreen
import com.ltm.finance.ui.screens.ai.AiAssistantScreen
import com.ltm.finance.ui.screens.assets_debts.AccountsDebtsScreen
import com.ltm.finance.ui.screens.budgets.BudgetsScreen
import com.ltm.finance.ui.screens.forecast_simulation.ForecastSimulationScreen
import com.ltm.finance.ui.screens.overview.OverviewScreen
import com.ltm.finance.ui.screens.reports.ReportsScreen
import com.ltm.finance.ui.screens.settings.SecurityLockScreen
import com.ltm.finance.ui.screens.settings.SettingsScreen
import com.ltm.finance.ui.screens.splash.SplashScreen
import com.ltm.finance.ui.screens.transactions.AddTransactionDialog
import com.ltm.finance.ui.screens.transactions.TransactionsScreen
import com.ltm.finance.ui.screens.welcome.WelcomeAuthScreen
import com.ltm.finance.ui.theme.*

enum class AppStage {
    SPLASH,
    AUTH,
    MAIN,
    ADMIN
}

enum class NavigationTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    OVERVIEW("Tổng quan", Icons.Outlined.Home, Icons.Filled.Home),
    TRANSACTIONS("Giao dịch", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS("Ngân sách", Icons.Outlined.PieChart, Icons.Filled.PieChart),
    ACCOUNTS("Tài sản", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    AI("Trợ lý AI", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome)
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    var appStage by remember { mutableStateOf(AppStage.SPLASH) }

    // In-App Auto-Update Dialog (Appears when new release is detected or downloading)
    AppUpdateDialog(
        status = updateStatus,
        onStartDownload = { url -> viewModel.downloadAndInstallUpdate(url) },
        onDismiss = { viewModel.dismissUpdate() }
    )

    when (appStage) {
        AppStage.SPLASH -> {
            SplashScreen(
                onFinishLoading = {
                    if (!userProfile.isAuthenticated) {
                        appStage = AppStage.AUTH
                    } else if (userProfile.role == UserRole.ADMIN) {
                        appStage = AppStage.ADMIN
                    } else {
                        appStage = AppStage.MAIN
                    }
                }
            )
        }

        AppStage.AUTH -> {
            WelcomeAuthScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    appStage = AppStage.MAIN
                },
                onAdminLoginSuccess = {
                    appStage = AppStage.ADMIN
                }
            )
        }

        AppStage.ADMIN -> {
            AdminDashboardScreen(
                viewModel = viewModel,
                onAdminSignOut = {
                    appStage = AppStage.AUTH
                }
            )
        }

        AppStage.MAIN -> {
            // If PIN lock is active, block UI until unlocked
            if (!isUnlocked) {
                SecurityLockScreen(onVerifyPin = { enteredPin ->
                    viewModel.verifyPin(enteredPin)
                })
                return
            }

            var currentTab by remember { mutableStateOf(NavigationTab.OVERVIEW) }
            var showForecastSimulation by remember { mutableStateOf(false) }
            var showReportsScreen by remember { mutableStateOf(false) }
            var showSettingsScreen by remember { mutableStateOf(false) }
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
                                showReportsScreen = false
                                showSettingsScreen = false
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
                        when {
                            showReportsScreen -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { showReportsScreen = false }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                                        }
                                        Text("Quay lại", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                    ReportsScreen(viewModel = viewModel)
                                }
                            }

                            showSettingsScreen -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { showSettingsScreen = false }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                                        }
                                        Text("Quay lại", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onSignOut = {
                                            showSettingsScreen = false
                                            appStage = AppStage.AUTH
                                        }
                                    )
                                }
                            }

                            showForecastSimulation -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { showForecastSimulation = false }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                                        }
                                        Text("Quay lại", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                    ForecastSimulationScreen(viewModel = viewModel)
                                }
                            }

                            else -> {
                                when (currentTab) {
                                    NavigationTab.OVERVIEW -> OverviewScreen(
                                        viewModel = viewModel,
                                        onNavigateToTransactions = { currentTab = NavigationTab.TRANSACTIONS },
                                        onNavigateToAi = { currentTab = NavigationTab.AI },
                                        onNavigateToForecast = { showForecastSimulation = true },
                                        onNavigateToSimulation = { showForecastSimulation = true },
                                        onNavigateToReports = { showReportsScreen = true },
                                        onNavigateToSettings = { showSettingsScreen = true },
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
                    }
                }

                // Add Transaction Dialog
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
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0x220F172A),
                    ambientColor = Color(0x100F172A)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            GlassBorderStroke
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) activeColor else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) activeColor else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(4.dp))
                        }
                    }
                }
            }
        }
    }
}
