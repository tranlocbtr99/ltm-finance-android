package com.ltm.finance.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltm.finance.data.model.AdviceFeedback
import com.ltm.finance.data.model.RecommendationStatus
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.components.LiquidGlassCard
import com.ltm.finance.ui.components.LiquidSegmentedControl
import com.ltm.finance.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel,
    onNavigateToSimulation: () -> Unit
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Trò chuyện AI", "Nhật ký Lời khuyên")

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val confirmedDraftIds = remember { mutableStateListOf<String>() }

    val suggestedPrompts = listOf(
        "Ăn trưa 35k ví tiền mặt",
        "Đổ xăng xe máy 60k",
        "Tôi có nên mua laptop 20tr không?",
        "Tháng này tôi đã chi quá nhiều vào đâu?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header
        Column {
            Text(
                text = "AI FINANCE ASSISTANT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = LiquidCyan
            )
            Text(
                text = "Trợ lý tài chính Hybrid",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab switcher
        LiquidSegmentedControl(
            items = tabs,
            selectedIndex = selectedTab,
            onItemSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Tab 1: On-demand Chat
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Quick prompt suggestions
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Gợi ý hoặc nhập lệnh nhanh:",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        suggestedPrompts.forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassBaseLight)
                                    .border(1.dp, GlassBorderStroke, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.sendChatMessage(prompt)
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(chatMessages.size)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = LiquidCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prompt,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Chat Messages
                items(chatMessages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        // User message bubble (Right aligned)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .background(LiquidCyan)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = DarkObsidian,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // AI Assistant Card (Left aligned, Liquid Glass)
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = LiquidIndigo.copy(alpha = 0.16f),
                            borderColor = LiquidIndigo.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = LiquidCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LTM AI Assistant",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidCyan,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 19.sp
                                )

                                // ⚡ 1-Tap Confirmation for Natural Language Transaction Draft (Ltmsav)
                                msg.draftTransaction?.let { draft ->
                                    val isConfirmed = confirmedDraftIds.contains(msg.id)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (!isConfirmed) {
                                                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                                viewModel.addTransaction(
                                                    type = draft.type,
                                                    amount = draft.amount,
                                                    date = today,
                                                    accountId = draft.accountId,
                                                    targetAccountId = draft.targetAccountId,
                                                    category = draft.category,
                                                    note = draft.note
                                                )
                                                confirmedDraftIds.add(msg.id)
                                            }
                                        },
                                        enabled = !isConfirmed,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isConfirmed) LiquidEmerald.copy(alpha = 0.3f) else LiquidEmerald,
                                            disabledContainerColor = LiquidEmerald.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (isConfirmed) "✅ Đã ghi vào sổ giao dịch" else "⚡ Xác nhận ghi vào sổ ngay",
                                            color = if (isConfirmed) LiquidEmerald else DarkObsidian,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (msg.actionSuggestion != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onNavigateToSimulation,
                                        colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(msg.actionSuggestion, color = DarkObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Bar
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 90.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Hỏi AI hoặc nhập lệnh nhanh...", color = TextTertiary, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendChatMessage(inputText.trim())
                                inputText = ""
                                coroutineScope.launch {
                                    listState.animateScrollToItem(chatMessages.size + 1)
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(LiquidCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = DarkObsidian,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Tab 2: Recommendation Log & Double-check (FR-024)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Text(
                        text = "Theo dõi trạng thái lời khuyên & xác minh qua đối chiếu số dư thực tế.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                items(recommendations, key = { it.id }) { rec ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rec.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (rec.status) {
                                                RecommendationStatus.IMPLEMENTED -> LiquidEmerald.copy(alpha = 0.2f)
                                                RecommendationStatus.ACCEPTED -> LiquidCyan.copy(alpha = 0.2f)
                                                RecommendationStatus.REJECTED -> LiquidRose.copy(alpha = 0.2f)
                                                else -> LiquidAmber.copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (rec.status) {
                                            RecommendationStatus.PARTIALLY_IMPLEMENTED -> "Một phần"
                                            RecommendationStatus.NOT_IMPLEMENTED -> "Chưa làm"
                                            else -> rec.status.displayName
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (rec.status) {
                                            RecommendationStatus.IMPLEMENTED -> LiquidEmerald
                                            RecommendationStatus.ACCEPTED -> LiquidCyan
                                            RecommendationStatus.REJECTED -> LiquidRose
                                            else -> LiquidAmber
                                        },
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = rec.content,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )

                            // Double-check verification section (FR-5.7)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Xác minh (Double-check):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LiquidCyan,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${rec.verification.displayName} • ${rec.doubleCheckPrompt ?: "Hệ thống đang theo dõi."}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            // Action buttons according to status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (rec.status == RecommendationStatus.CREATED || rec.status == RecommendationStatus.VIEWED) {
                                    TextButton(onClick = { viewModel.rejectAiRecommendation(rec.id, AdviceFeedback.HARD_TO_EXECUTE) }) {
                                        Text("Từ chối", color = LiquidRose, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = { viewModel.applyAiBudgetRecommendation(rec) },
                                        colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Chấp nhận", color = DarkObsidian, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                } else if (rec.status == RecommendationStatus.ACCEPTED || rec.status == RecommendationStatus.MONITORING) {
                                    TextButton(onClick = { viewModel.markRecommendationImplemented(rec.id, isPartial = true) }) {
                                        Text("Làm 1 phần", color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = { viewModel.markRecommendationImplemented(rec.id, isPartial = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = LiquidEmerald),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Hoàn thành", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                } else {
                                    Text(
                                        text = "Đã ghi nhận nghiên cứu",
                                        fontSize = 10.sp,
                                        color = TextTertiary,
                                        maxLines = 1
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
