package com.ltm.finance.domain

import com.ltm.finance.data.model.*
import java.math.BigDecimal
import java.util.UUID
import java.util.regex.Pattern

object AiFinanceAssistant {

    fun scanProactiveAlerts(
        accounts: List<FinancialAccount>,
        debts: List<Debt>,
        budgets: List<Budget>,
        forecast: CashflowForecast
    ): List<AiRecommendation> {
        val list = mutableListOf<AiRecommendation>()

        // 1. Cảnh báo vượt / sắp chạm ngân sách
        budgets.forEach { budget ->
            if (budget.isOverBudget) {
                list.add(
                    AiRecommendation(
                        id = "REC-BUDGET-OVER-${budget.id}",
                        category = budget.category,
                        title = "Vượt ngân sách ${budget.category}",
                        content = "Đã chi ${FinanceEngine.formatVnd(budget.spentAmount)} / ${FinanceEngine.formatVnd(budget.currentAmount)} (${budget.usagePercent}%).",
                        reason = "Tốc độ chi tiêu vượt quá giới hạn định mức cho tháng này.",
                        previousBudget = budget.currentAmount,
                        proposedBudget = budget.spentAmount.add(BigDecimal("200000")),
                        status = RecommendationStatus.CREATED,
                        doubleCheckPrompt = "Bạn có muốn tạm dừng chi tiêu danh mục này hay đề xuất nới ngân sách thêm 200.000đ?"
                    )
                )
            } else if (budget.isNearLimit) {
                list.add(
                    AiRecommendation(
                        id = "REC-BUDGET-NEAR-${budget.id}",
                        category = budget.category,
                        title = "Sắp chạm ngân sách ${budget.category}",
                        content = "Đã dùng ${budget.usagePercent}% ngân sách. Còn lại ${FinanceEngine.formatVnd(budget.remainingAmount)} khả dụng.",
                        reason = "Chi tiêu danh mục ${budget.category} đã chạm ngưỡng cảnh báo ${budget.warningThresholdPercent}%.",
                        previousBudget = budget.currentAmount,
                        status = RecommendationStatus.CREATED,
                        doubleCheckPrompt = "Hãy cân nhắc giảm bớt chi tiêu ${budget.category} trong những ngày tới."
                    )
                )
            }
        }

        // 2. Cảnh báo dòng tiền dự báo âm
        if (forecast.alerts.isNotEmpty()) {
            val topAlert = forecast.alerts.first()
            list.add(
                AiRecommendation(
                    id = "REC-CASHFLOW-${topAlert.date}",
                    category = "Dòng tiền",
                    title = "Nguy cơ thiếu tiền (${topAlert.date})",
                    content = "Dự báo bạn sẽ thiếu hụt khoảng ${FinanceEngine.formatVnd(topAlert.estimatedShortage)}. Cần chuẩn bị biện pháp ứng phó.",
                    reason = topAlert.reason,
                    status = RecommendationStatus.CREATED,
                    doubleCheckPrompt = "Bạn đã có kế hoạch bù đắp khoản thiếu hụt này chưa?"
                )
            )
        }

        // 3. Cảnh báo nợ đến hạn
        debts.forEach { debt ->
            debt.schedules.filter { it.status == RepaymentStatus.UPCOMING || it.status == RepaymentStatus.DUE_TODAY }.forEach { s ->
                list.add(
                    AiRecommendation(
                        id = "REC-DEBT-${s.id}",
                        category = "Trả nợ",
                        title = "Đến hạn trả nợ: ${debt.name}",
                        content = "Khoản trả góp/vay ${FinanceEngine.formatVnd(s.totalDue)} đến hạn vào ngày ${s.dueDate}.",
                        reason = "Đảm bảo trả đúng hạn để tránh phát sinh phí phạt trễ hạn.",
                        status = RecommendationStatus.CREATED,
                        doubleCheckPrompt = "Bạn đã chuẩn bị đủ tiền trong tài khoản để thanh toán khoản này chưa?"
                    )
                )
            }
        }

        return list
    }

    /**
     * Nhận diện ngôn ngữ tự nhiên tiếng Việt tạo Draft giao dịch (từ Ltmsav aiParser)
     */
    fun parseVietnameseCommand(
        text: String,
        accounts: List<FinancialAccount>
    ): TransactionDraft? {
        val s = text.lowercase()

        // Extract amount: e.g. 35k, 35000, 1.5tr, 2 triệu
        var parsedAmount = BigDecimal.ZERO
        val kMatcher = Pattern.compile("(\\d+([.,]\\d+)?)\\s*(k|nghìn|ngan)").matcher(s)
        val trMatcher = Pattern.compile("(\\d+([.,]\\d+)?)\\s*(tr|triệu|trieu)").matcher(s)
        val plainMatcher = Pattern.compile("(\\d{4,})").matcher(s)

        if (kMatcher.find()) {
            val num = kMatcher.group(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            parsedAmount = BigDecimal(num * 1000).setScale(0, java.math.RoundingMode.HALF_UP)
        } else if (trMatcher.find()) {
            val num = trMatcher.group(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            parsedAmount = BigDecimal(num * 1000000).setScale(0, java.math.RoundingMode.HALF_UP)
        } else if (plainMatcher.find()) {
            val num = plainMatcher.group(1)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            parsedAmount = num
        }

        if (parsedAmount <= BigDecimal.ZERO) return null

        // Detect Type
        val isIncome = s.contains("lương") || s.contains("thu") || s.contains("thưởng") || s.contains("trợ cấp") || s.contains("nhận")
        val isTransfer = s.contains("chuyển") || s.contains("nạp") || s.contains("rút")

        val txType = when {
            isTransfer -> TransactionType.TRANSFER
            isIncome -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // Detect Category
        val category = when {
            s.contains("ăn") || s.contains("cơm") || s.contains("phở") || s.contains("cafe") || s.contains("uống") || s.contains("bún") || s.contains("bánh") -> "Ăn uống"
            s.contains("xăng") || s.contains("xe") || s.contains("bus") || s.contains("grab") || s.contains("be") -> "Đi lại"
            s.contains("sách") || s.contains("học") || s.contains("photo") || s.contains("giáo trình") -> "Học tập"
            s.contains("nhà") || s.contains("trọ") || s.contains("điện") || s.contains("nước") || s.contains("wifi") -> "Nhà ở"
            s.contains("mua") || s.contains("áo") || s.contains("quần") || s.contains("giày") || s.contains("shopee") -> "Mua sắm"
            s.contains("phim") || s.contains("chơi") || s.contains("game") -> "Giải trí"
            isIncome -> "Thu nhập khác"
            else -> "Khác"
        }

        // Detect Account (if specified, else default first account)
        var matchedAcc = accounts.firstOrNull { acc -> s.contains(acc.name.lowercase()) }
        if (matchedAcc == null) {
            matchedAcc = accounts.firstOrNull()
        }
        val accId = matchedAcc?.id ?: "ACC-DEFAULT"

        return TransactionDraft(
            type = txType,
            amount = parsedAmount,
            category = category,
            accountId = accId,
            targetAccountId = if (txType == TransactionType.TRANSFER) accounts.getOrNull(1)?.id else null,
            note = text
        )
    }

    fun answerQuestion(
        query: String,
        accounts: List<FinancialAccount>,
        debts: List<Debt>,
        budgets: List<Budget>,
        transactions: List<Transaction>,
        userProfile: UserProfile
    ): AiChatMessage {
        val lower = query.lowercase()
        val totalAssets = FinanceEngine.calculateTotalAssets(accounts)
        val liquidCash = FinanceEngine.calculateLiquidCash(accounts)
        val totalDebts = FinanceEngine.calculateTotalDebts(debts)
        val netWorth = FinanceEngine.calculateNetWorth(accounts, debts)

        // 1. Check if query is a natural command to log a transaction (Ltmsav Smart Parser)
        val draft = parseVietnameseCommand(query, accounts)
        if (draft != null) {
            val accountName = accounts.find { it.id == draft.accountId }?.name ?: "Ví"
            return AiChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = "⚡ **Nhận diện giao dịch nhanh:**\n\n" +
                        "• Loại: **${draft.type.displayName}**\n" +
                        "• Số tiền: **${FinanceEngine.formatVnd(draft.amount)}**\n" +
                        "• Danh mục: **${draft.category}**\n" +
                        "• Tài khoản: **$accountName**\n\n" +
                        "Bấm nút xác nhận bên dưới để lưu vào sổ giao dịch ngay!",
                draftTransaction = draft
            )
        }

        // 2. Q&A scenarios
        return when {
            lower.contains("laptop") || lower.contains("mua") -> {
                val sim = SimulationEngine.simulatePurchase(
                    itemName = "Thiết bị học tập",
                    price = BigDecimal("20000000"),
                    currentLiquidCash = liquidCash,
                    monthlySurplus = userProfile.monthlyAllowance.add(userProfile.partTimeIncome).subtract(userProfile.expectedLivingCost)
                )
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Phân tích mua sắm từ Finance Engine:**\n\n" +
                            "• Tiền mặt khả dụng: **${FinanceEngine.formatVnd(liquidCash)}**\n" +
                            "• Đánh giá: ${sim.recommendation}\n" +
                            "• Trả góp: Xem chi tiết tại tab **Mô phỏng**.",
                    actionSuggestion = "Mở mô phỏng mua sắm"
                )
            }

            lower.contains("chi nhiều") || lower.contains("tiêu vào đâu") -> {
                if (budgets.isEmpty()) {
                    AiChatMessage(
                        id = UUID.randomUUID().toString(),
                        isUser = false,
                        text = "Bạn chưa thiết lập ngân sách nào. Hãy vào tab **Ngân sách** bấm **+ Thêm** để theo dõi chi tiêu!"
                    )
                } else {
                    val topBudgets = budgets.sortedByDescending { it.spentAmount }
                    val top3 = topBudgets.take(3).joinToString("\n") {
                        "• **${it.category}**: đã chi ${FinanceEngine.formatVnd(it.spentAmount)} / ${FinanceEngine.formatVnd(it.currentAmount)} (${it.usagePercent}%)"
                    }
                    AiChatMessage(
                        id = UUID.randomUUID().toString(),
                        isUser = false,
                        text = "🤖 **Báo cáo chi tiêu tháng này:**\n\n$top3"
                    )
                }
            }

            lower.contains("nợ") || lower.contains("trả khoản vay") -> {
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Tình hình nghĩa vụ nợ:**\n\n" +
                            "• Tổng dư nợ: **${FinanceEngine.formatVnd(totalDebts)}**\n" +
                            "• Tiền mặt khả dụng: **${FinanceEngine.formatVnd(liquidCash)}**\n\n" +
                            if (liquidCash >= totalDebts) {
                                "✅ Bạn đủ tiền mặt để thanh toán toàn bộ nghĩa vụ nợ."
                            } else {
                                "⚠️ Tiền mặt khả dụng thấp hơn dư nợ. Hãy chú ý lịch trả nợ đúng hạn!"
                            }
                )
            }

            else -> {
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Tình hình tài chính tổng quan:**\n\n" +
                            "• Tổng tài sản: **${FinanceEngine.formatVnd(totalAssets)}**\n" +
                            "• Tiền khả dụng: **${FinanceEngine.formatVnd(liquidCash)}**\n" +
                            "• Tổng nợ: **${FinanceEngine.formatVnd(totalDebts)}**\n" +
                            "• Tài sản ròng: **${FinanceEngine.formatVnd(netWorth)}**\n\n" +
                            "💡 Bạn cũng có thể gõ câu nhanh như *\"Ăn trưa 35k\"*, *\"Đổ xăng 50k\"* để tôi tự tạo lệnh ghi sổ nhanh!"
                )
            }
        }
    }
}
