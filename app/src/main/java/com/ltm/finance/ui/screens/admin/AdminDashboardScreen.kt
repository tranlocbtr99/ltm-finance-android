package com.ltm.finance.ui.screens.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.domain.ResearchEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassBackground
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onAdminSignOut: () -> Unit
) {
    val context = LocalContext.current
    val events by viewModel.researchEvents.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val metrics by viewModel.researchMetrics.collectAsState()
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()) }

    LiquidGlassBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Logout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LiquidRose)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUPER ADMIN CONSOLE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = LiquidRose
                            )
                        }
                        Text(
                            text = "Cổng Quản trị & Nghiên cứu",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Logout Button
                    Button(
                        onClick = {
                            viewModel.signOut()
                            Toast.makeText(context, "Đã đăng xuất tài khoản Quản trị", Toast.LENGTH_SHORT).show()
                            onAdminSignOut()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidRose.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Đăng xuất",
                            tint = LiquidRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đăng xuất", color = LiquidRose, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Admin Role Banner
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = LiquidRose.copy(alpha = 0.12f),
                    borderColor = LiquidRose.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LiquidRose.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = LiquidRose, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Tài khoản Quản trị cấp cao", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Quyền truy cập độc quyền: Dữ liệu nghiên cứu, Audit Log, Thống kê SPSS & Hệ thống", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // 4 Research & System KPI Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Người tham gia",
                        value = "1",
                        sub = userProfile.researchParticipantId,
                        color = LiquidCyan
                    )
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Sự kiện Audit",
                        value = events.size.toString(),
                        sub = "Đã mã hóa",
                        color = LiquidPurple
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Chấp nhận AI",
                        value = "${String.format(Locale.US, "%.1f", metrics.adviceAcceptedRate)}%",
                        sub = "Tỷ lệ đồng thuận",
                        color = LiquidEmerald
                    )
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Tuân thủ ngân sách",
                        value = "${String.format(Locale.US, "%.1f", metrics.budgetComplianceRate)}%",
                        sub = "Hành vi thực tế",
                        color = LiquidAmber
                    )
                }
            }

            // CSV Export Section
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = LiquidCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xuất dữ liệu nghiên cứu (CSV)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(
                            text = "Tập dữ liệu được định dạng chuẩn cho SPSS, R, Python hoặc Stata để chạy phân tích hồi quy và kiểm định thống kê.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val csv = ResearchEngine.exportToCsv(events)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Research Events CSV", csv))
                                    Toast.makeText(context, "Đã chép CSV Sự kiện (${events.size} dòng) vào bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("Chép CSV Sự kiện", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val csv = "participant_id,transactions_logged,budget_compliance_rate,ai_engagement_count,advice_accepted_rate,advice_implemented_rate,days_active\n" +
                                            "\"${metrics.participantId}\",${metrics.totalTransactionsLogged},${metrics.budgetComplianceRate},${metrics.aiEngagementCount},${metrics.adviceAcceptedRate},${metrics.adviceImplementedRate},${metrics.daysActive}\n"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Participant Metrics CSV", csv))
                                    Toast.makeText(context, "Đã chép CSV Chỉ số nghiên cứu", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("Chép CSV Chỉ số", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Real-time Audit Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NHẬT KÝ KIỂM TOÁN HỆ THỐNG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("${events.size} bản ghi", fontSize = 11.sp, color = LiquidCyan)
                }
            }

            if (events.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có sự kiện nào được ghi nhận", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(events.reversed().take(20), key = { it.eventId }) { ev ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ev.eventType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Meta: ${ev.metadataJson}", fontSize = 10.sp, color = TextSecondary, maxLines = 1)
                            }
                            Text(
                                text = dateFormat.format(Date(ev.timestamp)),
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    sub: String,
    color: Color
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, fontSize = 10.sp, color = TextTertiary)
        }
    }
}
