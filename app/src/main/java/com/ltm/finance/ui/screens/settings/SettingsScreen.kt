package com.ltm.finance.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.theme.*
import java.math.BigDecimal

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenAdminPortal: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val securityPin by viewModel.securityPin.collectAsState()
    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var alertMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "CÀI ĐẶT & HỆ THỐNG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Cài đặt & Bảo mật",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // 1. Profile Card
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
                        Column {
                            Text(userProfile.displayName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${userProfile.university} • Năm ${userProfile.academicYear}", fontSize = 12.sp, color = TextSecondary)
                        }
                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa hồ sơ", tint = LiquidCyan)
                        }
                    }
                }
            }
        }

        // 2. Security PIN Lock
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPinDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = LiquidCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Khóa bảo mật mã PIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = if (!securityPin.isNullOrBlank()) "Đang bật mã PIN bảo vệ" else "Chưa đặt mã PIN",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }
        }

        // 3. Backup & Restore (Ltmsav)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("Dữ liệu & Sao lưu", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showBackupDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBaseLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sao lưu JSON", fontSize = 12.sp, color = TextPrimary)
                        }
                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBaseLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Khôi phục", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // 4. Admin Portal (Cổng quản trị tự động tổng hợp dữ liệu)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = LiquidPurple.copy(alpha = 0.15f),
                borderColor = LiquidPurple.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAdminPortal() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = LiquidPurple, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cổng Quản Trị & Nghiên Cứu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Tự động nhận dữ liệu ẩn danh và tổng hợp riêng", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LiquidPurple)
                }
            }
        }

        // 5. Reset All Data
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                borderColor = LiquidRose.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResetDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = LiquidRose, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Xóa toàn bộ dữ liệu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiquidRose)
                            Text("Dọn sạch toàn bộ tài khoản, giao dịch, ngân sách", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit Profile
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(userProfile.displayName) }
        var uni by remember { mutableStateOf(userProfile.university) }
        var yearText by remember { mutableStateOf(userProfile.academicYear.toString()) }
        var allowanceText by remember { mutableStateOf(userProfile.monthlyAllowance.toPlainString()) }
        var partTimeText by remember { mutableStateOf(userProfile.partTimeIncome.toPlainString()) }
        var goal by remember { mutableStateOf(userProfile.financialGoal) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Chỉnh sửa hồ sơ sinh viên", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Họ và tên") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = uni, onValueChange = { uni = it }, label = { Text("Trường học") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = allowanceText, onValueChange = { allowanceText = it.filter { c -> c.isDigit() } }, label = { Text("Trợ cấp gia đình (VNĐ/tháng)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = partTimeText, onValueChange = { partTimeText = it.filter { c -> c.isDigit() } }, label = { Text("Thu nhập làm thêm (VNĐ/tháng)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditProfileDialog = false }) { Text("Hủy", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                viewModel.updateUserProfile(
                                    displayName = name,
                                    university = uni,
                                    academicYear = yearText.toIntOrNull() ?: 1,
                                    monthlyAllowance = allowanceText.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                    partTimeIncome = partTimeText.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                    financialGoal = goal
                                )
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan)
                        ) { Text("Lưu", color = DarkObsidian, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // Dialog: Set PIN
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showPinDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cài đặt mã PIN (4 số)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("Nhập mã PIN 4 số") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (!securityPin.isNullOrBlank()) {
                            TextButton(onClick = {
                                viewModel.setSecurityPin(null)
                                showPinDialog = false
                            }) { Text("Gỡ bỏ PIN", color = LiquidRose) }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Row {
                            TextButton(onClick = { showPinDialog = false }) { Text("Hủy", color = TextSecondary) }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (pinInput.length == 4) {
                                        viewModel.setSecurityPin(pinInput)
                                        showPinDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan)
                            ) { Text("Xác nhận", color = DarkObsidian, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Backup JSON
    if (showBackupDialog) {
        val json = remember { viewModel.exportBackupJson() }
        Dialog(onDismissRequest = { showBackupDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sao lưu dữ liệu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Bấm nút bên dưới để sao chép chuỗi mã hóa sao lưu vào bộ nhớ tạm.", fontSize = 12.sp, color = TextSecondary)
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("LTM_FINANCE_BACKUP", json))
                            showBackupDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan)
                    ) { Text("Sao chép bản sao lưu", color = DarkObsidian, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    // Dialog: Import JSON
    if (showImportDialog) {
        var inputJson by remember { mutableStateOf("") }
        var importError by remember { mutableStateOf<String?>(null) }
        Dialog(onDismissRequest = { showImportDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Khôi phục dữ liệu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = inputJson,
                        onValueChange = { inputJson = it },
                        placeholder = { Text("Dán chuỗi JSON sao lưu vào đây...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    importError?.let { Text(it, color = LiquidRose, fontSize = 11.sp) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showImportDialog = false }) { Text("Hủy", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (viewModel.importBackupJson(inputJson.trim())) {
                                    showImportDialog = false
                                } else {
                                    importError = "Dữ liệu JSON không hợp lệ"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidEmerald)
                        ) { Text("Khôi phục", color = DarkObsidian, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // Dialog: Reset All Data
    if (showResetDialog) {
        Dialog(onDismissRequest = { showResetDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Xóa sạch toàn bộ dữ liệu?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LiquidRose)
                    Text("Toàn bộ tài khoản, số dư, giao dịch, ngân sách sẽ bị xóa sạch hoàn toàn để bắt đầu lại.", fontSize = 12.sp, color = TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showResetDialog = false }) { Text("Hủy", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                viewModel.resetAllData()
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidRose)
                        ) { Text("Xác nhận xóa hết", color = TextPrimary, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
