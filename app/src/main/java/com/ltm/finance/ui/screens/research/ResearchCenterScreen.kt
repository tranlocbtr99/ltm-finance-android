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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    text = "Nghiên cứu quan sát theo chiều dọc (Longitudinal Study)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Participant ID Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = LiquidPurple.copy(alpha = 0.16f),
                borderColor = LiquidPurple.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MÃ NGHIÊN CỨU ẨN DANH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LiquidPurple, maxLines = 1)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userProfile.researchParticipantId,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text("Mã hóa tách rời danh tính cá nhân", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                    }
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = LiquidPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Research Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Chỉ số khoa học đang theo dõi:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Tuân thủ ngân sách",
                        value = "${metrics.budgetComplianceRate.toInt()}%",
                        color = LiquidEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Chấp nhận lời khuyên",
                        value = "${metrics.adviceAcceptedRate.toInt()}%",
                        color = LiquidCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Phân tách sự đồng ý (Consent)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Theo PRD Section 10 và Luật Bảo vệ dữ liệu cá nhân VN, bật/tắt từng mục độc lập mà không mất quyền dùng app.",
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
                            Text("Điều khoản dịch vụ app", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                            Text("Quản lý tài chính cá nhân cơ bản", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
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
                            Text("Xử lý dữ liệu AI cá nhân hóa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                            Text("Tư vấn và cảnh báo chủ động", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
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
                            Text("Tham gia nghiên cứu khoa học", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                            Text("Nhật ký ẩn danh phục vụ bài báo học thuật", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
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
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Xuất dữ liệu nghiên cứu (Export)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Xuất nhật ký sự kiện chuẩn CSV phục vụ SPSS, R hoặc Python.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            val csv = ResearchEngine.exportToCsv(events)
                            exportMessage = "Đã xuất ${events.size} sự kiện ra chuẩn CSV!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Xuất CSV cho SPSS / Python", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}
