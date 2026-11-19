package com.ltm.finance.ui.screens.research

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.ltm.finance.domain.ResearchEngine
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.theme.*

@Composable
fun ResearchCenterScreen(
    viewModel: MainViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val metrics by viewModel.researchMetrics.collectAsState()
    val events by viewModel.researchEvents.collectAsState()

    var termsConsent by remember { mutableStateOf(userProfile.consentTerms) }
    var aiConsent by remember { mutableStateOf(userProfile.consentAiProcessing) }
    var researchConsent by remember { mutableStateOf(userProfile.consentResearchStudy) }

    var exportMessage by remember { mutableStateOf<String?>(null) }

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
                    text = "NGHIÊN CỨU & QUYỀN RIÊNG TƯ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = LiquidCyan
                )
                Text(
                    text = "Research & Privacy",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Nghiên cứu quan sát theo chiều dọc (Longitudinal Observational Study)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Participant ID Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = LiquidPurple.copy(alpha = 0.16f),
                borderColor = LiquidPurple.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MÃ NGHIÊN CỨU ẨN DANH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidPurple)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userProfile.researchParticipantId,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text("Dữ liệu được mã hóa riêng biệt với thông tin danh tính cá nhân", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = LiquidPurple,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Research Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chỉ số khoa học đang theo dõi:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Tuân thủ ngân sách",
                        value = "${metrics.budgetComplianceRate.toInt()}%",
                        color = LiquidEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Chấp nhận AI",
                        value = "${metrics.adviceAcceptedRate.toInt()}%",
                        color = LiquidCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Thực hiện lời khuyên",
                        value = "${metrics.adviceImplementedRate.toInt()}%",
                        color = LiquidIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Tương tác AI",
                        value = "${metrics.aiEngagementCount} lượt",
                        color = LiquidAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Consent Management (Section 10.2)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Phân tách sự đồng ý (Consent Management)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Theo quy định PRD Section 10 và Luật Bảo vệ dữ liệu cá nhân số 91/2025/QH15, người dùng có quyền bật/tắt từng mục độc lập.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    // Consent 1: Terms
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Điều khoản dịch vụ app", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Quản lý tài khoản và ghi nhận thu chi cơ bản", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = termsConsent,
                            onCheckedChange = {
                                termsConsent = it
                                viewModel.updateConsent(termsConsent, aiConsent, researchConsent)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = LiquidCyan, checkedTrackColor = LiquidCyan.copy(alpha = 0.5f))
                        )
                    }

                    // Consent 2: AI Processing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Xử lý dữ liệu AI cá nhân hóa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Cho phép AI phân tích và đưa cảnh báo chủ động", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = aiConsent,
                            onCheckedChange = {
                                aiConsent = it
                                viewModel.updateConsent(termsConsent, aiConsent, researchConsent)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = LiquidIndigo, checkedTrackColor = LiquidIndigo.copy(alpha = 0.5f))
                        )
                    }

                    // Consent 3: Research Participation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tham gia nghiên cứu khoa học", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Ghi nhận nhật ký ẩn danh phục vụ bài báo học thuật (Rút đồng ý không ảnh hưởng tính năng app)", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = researchConsent,
                            onCheckedChange = {
                                researchConsent = it
                                viewModel.updateConsent(termsConsent, aiConsent, researchConsent)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = LiquidPurple, checkedTrackColor = LiquidPurple.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        // Data Export (Section 7.2 & 6.6)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Xuất dữ liệu nghiên cứu (Research Export)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Xuất bản ghi sự kiện theo cấu trúc chuẩn phục vụ SPSS, R hoặc Python.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            val csv = ResearchEngine.exportToCsv(events)
                            exportMessage = "Đã xuất thành công ${events.size} bản ghi sự kiện nghiên cứu (CSV)!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xuất CSV cho SPSS / Python", fontWeight = FontWeight.Bold)
                    }

                    exportMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = LiquidEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

