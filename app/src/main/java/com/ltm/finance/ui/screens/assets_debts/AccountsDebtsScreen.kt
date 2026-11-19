package com.ltm.finance.ui.screens.assets_debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ltm.finance.data.model.AccountType
import com.ltm.finance.data.model.DebtType
import com.ltm.finance.data.model.FinancialAccount
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidSegmentedControl
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal

@Composable
fun AccountsDebtsScreen(
    viewModel: MainViewModel
) {
    val accounts by viewModel.accounts.collectAsState()
    val debts by viewModel.debts.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tài sản & Ví", "Khoản nợ & Vay")

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<FinancialAccount?>(null) }
    var deleteConfirmItem by remember { mutableStateOf<Pair<String, String>?>(null) } // id to type

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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

                // Add button pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selectedTab == 0) LiquidEmerald else LiquidRose)
                        .clickable {
                            if (selectedTab == 0) showAddAccountDialog = true else showAddDebtDialog = true
                        }
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
                            text = if (selectedTab == 0) "Thêm ví" else "Thêm nợ",
                            color = DarkObsidian,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
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
            // Summary Banner
            item {
                val totalAssets = FinanceEngine.calculateTotalAssets(accounts)
                val liquidCash = FinanceEngine.calculateLiquidCash(accounts)

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = LiquidEmerald.copy(alpha = 0.15f),
                    borderColor = LiquidEmerald.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tổng tài sản", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FinanceEngine.formatVnd(totalAssets),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tiền khả dụng", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FinanceEngine.formatVnd(liquidCash),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LiquidEmerald,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Accounts List or Empty State
            if (accounts.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = LiquidCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Chưa có tài khoản hoặc ví nào",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Thêm ví tiền mặt, ngân hàng hoặc ví điện tử của bạn để bắt đầu quản lý.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddAccountDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidEmerald),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Tạo tài khoản đầu tiên", color = DarkObsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(accounts, key = { it.id }) { acc ->
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
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = acc.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "${acc.provider} • ${acc.type.displayName}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    if (!acc.isLiquid && acc.maturityDate != null) {
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = "Đáo hạn: ${acc.maturityDate} (Lãi ${acc.interestRateYearly}%)",
                                            fontSize = 10.sp,
                                            color = LiquidAmber,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FinanceEngine.formatVnd(acc.balance),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (acc.isLiquid) LiquidEmerald.copy(alpha = 0.15f) else LiquidAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (acc.isLiquid) "Thanh khoản" else "Tiết kiệm",
                                            fontSize = 9.sp,
                                            color = if (acc.isLiquid) LiquidEmerald else LiquidAmber,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Delete icon
                                IconButton(
                                    onClick = { deleteConfirmItem = acc.id to "account" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Xóa",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Debts List
            val totalDebts = FinanceEngine.calculateTotalDebts(debts)
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = LiquidRose.copy(alpha = 0.15f),
                    borderColor = LiquidRose.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Tổng dư nợ hiện tại", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FinanceEngine.formatVnd(totalDebts),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LiquidRose,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hạn mức tín dụng không tính vào tài sản của bạn.",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            if (debts.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = LiquidEmerald,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Hiện không có khoản nợ nào",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tài chính an toàn! Nếu có khoản vay hoặc trả góp, bạn có thể thêm để theo dõi lịch trả.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDebtDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidRose),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Thêm khoản nợ / trả góp", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(debts, key = { it.id }) { debt ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = debt.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${debt.type.displayName} • Đến hạn ngày ${debt.paymentDueDay}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = FinanceEngine.formatVnd(debt.currentBalance),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidRose,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { deleteConfirmItem = debt.id to "debt" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Xóa",
                                            tint = TextTertiary,
                                            modifier = Modifier.size(16.dp)
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

    // Dialog: Thêm tài khoản / tài sản
    if (showAddAccountDialog) {
        var accName by remember { mutableStateOf("") }
        var accBalanceText by remember { mutableStateOf("") }
        var accProvider by remember { mutableStateOf("") }
        var selectedAccType by remember { mutableStateOf(AccountType.BANK) }
        var isLiquid by remember { mutableStateOf(true) }
        var interestText by remember { mutableStateOf("") }
        var maturityDateText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddAccountDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Thêm tài khoản / Ví tiền", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Tên tài khoản (Ví dụ: VCB, MoMo)", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accBalanceText,
                        onValueChange = { accBalanceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Số dư ban đầu (VNĐ)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Picker
                    Column {
                        Text("Loại tài sản:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AccountType.entries) { type ->
                                val isSel = selectedAccType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) LiquidCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isSel) LiquidCyan else GlassBorderStroke, RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedAccType = type
                                            isLiquid = type != AccountType.SAVINGS
                                        }
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Text(type.displayName, fontSize = 11.sp, color = if (isSel) LiquidCyan else TextSecondary)
                                }
                            }
                        }
                    }

                    if (selectedAccType == AccountType.SAVINGS) {
                        OutlinedTextField(
                            value = interestText,
                            onValueChange = { interestText = it },
                            label = { Text("Lãi suất %/năm (Ví dụ: 5.5)", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = maturityDateText,
                            onValueChange = { maturityDateText = it },
                            label = { Text("Ngày đáo hạn (YYYY-MM-DD)", color = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddAccountDialog = false }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val bal = accBalanceText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                if (accName.isNotBlank()) {
                                    viewModel.addAccount(
                                        name = accName,
                                        type = selectedAccType,
                                        balance = bal,
                                        provider = accProvider.ifBlank { accName },
                                        isLiquid = isLiquid,
                                        interestRateYearly = interestText.toDoubleOrNull() ?: 0.0,
                                        maturityDate = maturityDateText.ifBlank { null }
                                    )
                                    showAddAccountDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Lưu tài khoản", color = DarkObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Thêm khoản nợ / Vay
    if (showAddDebtDialog) {
        var debtName by remember { mutableStateOf("") }
        var debtAmountText by remember { mutableStateOf("") }
        var paymentDayText by remember { mutableStateOf("15") }
        var selectedDebtType by remember { mutableStateOf(DebtType.INSTALLMENT) }

        Dialog(onDismissRequest = { showAddDebtDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Thêm nghĩa vụ nợ / Trả góp", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = debtName,
                        onValueChange = { debtName = it },
                        label = { Text("Tên khoản nợ (Ví dụ: Trả góp laptop)", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = debtAmountText,
                        onValueChange = { debtAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Số dư nợ còn lại (VNĐ)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = paymentDayText,
                        onValueChange = { paymentDayText = it.filter { c -> c.isDigit() } },
                        label = { Text("Ngày thanh toán hàng tháng (1-28)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDebtDialog = false }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val amt = debtAmountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                if (debtName.isNotBlank() && amt > BigDecimal.ZERO) {
                                    viewModel.addDebt(
                                        name = debtName,
                                        type = selectedDebtType,
                                        initialAmount = amt,
                                        currentBalance = amt,
                                        interestRateYearly = 0.0,
                                        paymentDueDay = paymentDayText.toIntOrNull()?.coerceIn(1, 28) ?: 15
                                    )
                                    showAddDebtDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidRose),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Lưu khoản nợ", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    deleteConfirmItem?.let { (id, type) ->
        Dialog(onDismissRequest = { deleteConfirmItem = null }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Xác nhận xóa?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidRose)
                    Text(
                        text = if (type == "account") "Bạn có chắc muốn xóa tài khoản này? Thao tác này không thể hoàn tác."
                        else "Bạn có chắc muốn xóa khoản nợ này?",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { deleteConfirmItem = null }) {
                            Text("Hủy", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (type == "account") viewModel.deleteAccount(id) else viewModel.deleteDebt(id)
                                deleteConfirmItem = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidRose),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Xác nhận xóa", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
