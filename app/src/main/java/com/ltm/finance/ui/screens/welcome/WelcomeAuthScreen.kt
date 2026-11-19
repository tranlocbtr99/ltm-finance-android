package com.ltm.finance.ui.screens.welcome

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassBackground
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.theme.*

@Composable
fun WelcomeAuthScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    onAdminLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var hasAcceptedTerms by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminErrorMsg by remember { mutableStateOf<String?>(null) }
    var showQuickEmailDialog by remember { mutableStateOf(false) }
    var quickEmailInput by remember { mutableStateOf("") }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account.email ?: "sinhvien@gmail.com"
            val name = account.displayName ?: "Sinh viên Google"
            val photo = account.photoUrl?.toString()
            viewModel.loginAsUser(email, name, photo)
            Toast.makeText(context, "Đăng nhập Google thành công: $email", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        } catch (e: Exception) {
            Toast.makeText(context, "Đăng nhập Google: ${e.localizedMessage ?: "Đã hủy"}", Toast.LENGTH_SHORT).show()
            // In case SHA-1 is pending or Play Services fails, prompt quick email login
            showQuickEmailDialog = true
        }
    }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(LiquidCyan.copy(alpha = 0.8f), LiquidIndigo.copy(alpha = 0.3f))
                            ),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = LiquidCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LTM FINANCE",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Hệ thống quản trị tài chính cá nhân tích hợp AI",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Middle: Highlights Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                FeatureHighlightPill(
                    icon = Icons.Default.Calculate,
                    color = LiquidCyan,
                    title = "Finance Engine tính toán chuẩn xác",
                    desc = "Quản lý thu chi, ngân sách, tài sản và nợ với độ chuẩn xác tuyệt đối."
                )

                FeatureHighlightPill(
                    icon = Icons.Default.AutoAwesome,
                    color = LiquidEmerald,
                    title = "AI Finance Assistant đồng hành",
                    desc = "Tự động phân tích câu lệnh tiếng Việt, dự báo rủi ro & dòng tiền."
                )

                FeatureHighlightPill(
                    icon = Icons.Default.CloudSync,
                    color = LiquidPurple,
                    title = "Đồng bộ đám mây Firebase Realtime",
                    desc = "Dữ liệu tự động đồng bộ xuyên suốt trên mọi thiết bị bạn sử dụng."
                )
            }

            // Bottom: Consent Checkbox & Login Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Consent Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0xFF0F172A).copy(alpha = 0.75f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { hasAcceptedTerms = !hasAcceptedTerms }
                        ) {
                            Checkbox(
                                checked = hasAcceptedTerms,
                                onCheckedChange = { hasAcceptedTerms = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LiquidEmerald,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = DarkObsidian
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Tôi đồng ý với Điều khoản & Chính sách thu thập dữ liệu",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Dữ liệu được mã hóa nặc danh, không lưu mật khẩu Google, cam kết bảo mật 100%.",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showPrivacyDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Xem chi tiết chính sách ➔", fontSize = 11.sp, color = LiquidCyan)
                            }
                        }
                    }
                }

                // Google Sign-In Button
                Button(
                    onClick = {
                        if (!hasAcceptedTerms) {
                            Toast.makeText(context, "Vui lòng tích chọn đồng ý điều khoản trước khi đăng nhập", Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                googleAuthLauncher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                showQuickEmailDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasAcceptedTerms) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng nhập bằng tài khoản Google",
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Quick Demo Login Button
                OutlinedButton(
                    onClick = {
                        if (!hasAcceptedTerms) {
                            Toast.makeText(context, "Vui lòng tích chọn đồng ý điều khoản trước khi bắt đầu", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.loginAsUser("sinhvien.demo@ltmfinance.edu.vn", "Nguyễn Văn Sinh Viên", null)
                            Toast.makeText(context, "Đăng nhập thành công với tài khoản mẫu", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderStroke)
                ) {
                    Text(
                        text = "Dùng thử nhanh (Tài khoản thử nghiệm)",
                        color = LiquidCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Admin Gateway Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showAdminLoginDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cổng Quản trị viên hệ thống",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }

    // Dialog: Privacy Policy Details
    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.98f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chính sách & Quyền riêng tư", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { showPrivacyDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                        }
                    }

                    Text(
                        text = "1. Mục đích thu thập:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidCyan
                    )
                    Text(
                        text = "Hệ thống ghi nhận giao dịch thu chi, thiết lập ngân sách và tương tác với AI nhằm cá nhân hóa lời khuyên tài chính và phục vụ nghiên cứu quan sát hành vi tài chính sinh viên.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Text(
                        text = "2. Bảo mật & Nặc danh:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidEmerald
                    )
                    Text(
                        text = "Toàn bộ dữ liệu nghiên cứu được mã hóa nặc danh theo mã định danh ngẫu nhiên (Participant ID). Hệ thống tuyệt đối không lưu mật khẩu Google hay chia sẻ thông tin cho bên thứ ba.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Text(
                        text = "3. Quyền của người dùng:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidPurple
                    )
                    Text(
                        text = "Bạn có quyền xuất dữ liệu, xóa sạch toàn bộ dữ liệu hoặc thu hồi quyền nghiên cứu bất cứ lúc nào trong cài đặt ứng dụng.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Button(
                        onClick = {
                            hasAcceptedTerms = true
                            showPrivacyDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Đồng ý & Đóng", color = DarkObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Dialog: Admin Login
    if (showAdminLoginDialog) {
        Dialog(onDismissRequest = { showAdminLoginDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.98f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Xác thực Quản trị viên", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { showAdminLoginDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                        }
                    }

                    Text(
                        text = "Nhập mã bảo mật Quản trị viên hệ thống để truy cập Cổng nghiên cứu & Thống kê:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            adminErrorMsg = null
                        },
                        placeholder = { Text("Mã PIN Admin (mặc định: 8888)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    adminErrorMsg?.let {
                        Text(it, color = LiquidRose, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (viewModel.loginAsAdmin(adminPinInput)) {
                                showAdminLoginDialog = false
                                Toast.makeText(context, "Chào mừng Quản trị viên Super Admin", Toast.LENGTH_SHORT).show()
                                onAdminLoginSuccess()
                            } else {
                                adminErrorMsg = "Mã xác thực Quản trị viên không chính xác"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Đăng nhập Quản trị", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Dialog: Quick Email Login Fallback
    if (showQuickEmailDialog) {
        Dialog(onDismissRequest = { showQuickEmailDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.98f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Đăng nhập email Google", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Nhập email Google của bạn để liên kết dữ liệu và đồng bộ đám mây:", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = quickEmailInput,
                        onValueChange = { quickEmailInput = it },
                        placeholder = { Text("vd: sinhvien@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val email = if (quickEmailInput.isNotBlank()) quickEmailInput.trim() else "sinhvien@gmail.com"
                            val name = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                            viewModel.loginAsUser(email, name, null)
                            showQuickEmailDialog = false
                            Toast.makeText(context, "Đăng nhập thành công: $email", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Xác nhận đăng nhập", color = DarkObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    desc: String
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f))
                    .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
