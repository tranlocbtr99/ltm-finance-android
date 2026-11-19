package com.ltm.finance.domain

import com.ltm.finance.data.model.*
import java.math.BigDecimal
import java.util.UUID

object AiFinanceAssistant {

    /**
     * Proactive AI (FR-5.1 B & Section 5.3 Level 3):
     * Quét dữ liệu và phát hiện các rủi ro tài chính chủ động mà người dùng không cần hỏi.
     */
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
                        content = "Bạn đã chi tiêu ${FinanceEngine.formatVnd(budget.spentAmount)} trên hạn mức ${FinanceEngine.formatVnd(budget.currentAmount)} (${budget.usagePercent}%).",
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
                        title = "Sắp chạm giới hạn ngân sách ${budget.category}",
                        content = "Đã sử dụng ${budget.usagePercent}% ngân sách. Bạn chỉ còn ${FinanceEngine.formatVnd(budget.remainingAmount)} khả dụng.",
                        reason = "Chi tiêu danh mục ${budget.category} đã chạm ngưỡng cảnh báo ${budget.warningThresholdPercent}%.",
                        previousBudget = budget.currentAmount,
                        status = RecommendationStatus.CREATED,
                        doubleCheckPrompt = "Hãy cân nhắc giảm bớt chi tiêu ${budget.category} trong những ngày tới."
                    )
                )
            }
        }

        // 2. Cảnh báo dòng tiền dự báo âm (FR-020)
        if (forecast.alerts.isNotEmpty()) {
            val topAlert = forecast.alerts.first()
            list.add(
                AiRecommendation(
                    id = "REC-CASHFLOW-${topAlert.date}",
                    category = "Dòng tiền",
                    title = "Nguy cơ thiếu tiền vào ngày ${topAlert.date}",
                    content = "Dự báo bạn sẽ thiếu hụt khoảng ${FinanceEngine.formatVnd(topAlert.estimatedShortage)}. Cần chuẩn bị biện pháp ứng phó.",
                    reason = topAlert.reason,
                    status = RecommendationStatus.CREATED,
                    doubleCheckPrompt = "Bạn đã có kế hoạch bù đắp khoản thiếu hụt này chưa?"
                )
            )
        }

        // 3. Cảnh báo nợ đến hạn (FR-012)
        debts.forEach { debt ->
            debt.schedules.filter { it.status == RepaymentStatus.UPCOMING || it.status == RepaymentStatus.DUE_TODAY }.forEach { s ->
                list.add(
                    AiRecommendation(
                        id = "REC-DEBT-${s.id}",
                        category = "Trả nợ",
                        title = "Đến hạn thanh toán: ${debt.name}",
                        content = "Khoản trả góp/vay ${FinanceEngine.formatVnd(s.totalDue)} đến hạn vào ngày ${s.dueDate}.",
                        reason = "Đảm bảo trả đúng hạn để tránh điểm tín dụng xấu và phí phạt trễ hạn.",
                        status = RecommendationStatus.CREATED,
                        doubleCheckPrompt = "Bạn đã chuẩn bị đủ tiền trong tài khoản để thanh toán khoản này chưa?"
                    )
                )
            }
        }

        return list
    }

    /**
     * On-demand AI (FR-5.1 A):
     * Trả lời câu hỏi tài chính dựa trên dữ liệu thực tế tính toán từ FinanceEngine.
     */
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

        return when {
            lower.contains("laptop") || lower.contains("mua sắm") || lower.contains("mua") -> {
                val sim = SimulationEngine.simulatePurchase(
                    itemName = "Laptop học tập",
                    price = BigDecimal("20000000"),
                    currentLiquidCash = liquidCash,
                    monthlySurplus = userProfile.monthlyAllowance.add(userProfile.partTimeIncome).subtract(userProfile.expectedLivingCost)
                )
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Phân tích mua sắm từ Finance Engine:**\n\n" +
                            "• Tiền mặt khả dụng hiện tại: **${FinanceEngine.formatVnd(liquidCash)}**\n" +
                            "• Giá dự kiến: **20.000.000 đ**\n\n" +
                            "👉 **Đánh giá:** ${sim.recommendation}\n\n" +
                            "• Mua ngay: ${if (liquidCash >= BigDecimal("20000000")) "Khả thi nhưng sẽ giảm thanh khoản" else "Không đủ tiền mặt"}\n" +
                            "• Trả góp 6 tháng: Khoảng ~3.400.000 đ/tháng.\n" +
                            "• Đề xuất tối ưu: Xem chi tiết tại tab **Mô phỏng quyết định**.",
                    actionSuggestion = "Mở mô phỏng mua sắm"
                )
            }

            lower.contains("chi nhiều") || lower.contains("tiêu vào đâu") || lower.contains("chi tiêu") -> {
                val topBudgets = budgets.sortedByDescending { it.spentAmount }
                val top3 = topBudgets.take(3).joinToString("\n") {
                    "• **${it.category}**: đã chi ${FinanceEngine.formatVnd(it.spentAmount)} / ${FinanceEngine.formatVnd(it.currentAmount)} (${it.usagePercent}%)"
                }
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Báo cáo chi tiêu tháng này:**\n\n" +
                            top3 + "\n\n" +
                            if (topBudgets.any { it.isOverBudget }) {
                                "⚠️ Bạn đang có danh mục vượt hạn mức. Hãy cân nhắc cắt giảm chi tiêu giải trí/mua sắm!"
                            } else {
                                "✅ Các danh mục vẫn đang nằm trong tầm kiểm soát."
                            }
                )
            }

            lower.contains("nợ") || lower.contains("trả khoản vay") || lower.contains("tháng sau") -> {
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Tình hình nghĩa vụ nợ:**\n\n" +
                            "• Tổng dư nợ hiện tại: **${FinanceEngine.formatVnd(totalDebts)}**\n" +
                            "• Số tiền thanh khoản sẵn sàng: **${FinanceEngine.formatVnd(liquidCash)}**\n" +
                            "• Tỷ lệ tiền mặt / nợ: **${if (totalDebts > BigDecimal.ZERO) (liquidCash.multiply(BigDecimal(100)).divide(totalDebts, 0, java.math.RoundingMode.HALF_UP)) else "100"}%**\n\n" +
                            if (liquidCash > totalDebts) {
                                "✅ Bạn hoàn toàn đủ tiền mặt để thanh toán toàn bộ nghĩa vụ nợ sắp tới."
                            } else {
                                "⚠️ Tiền mặt khả dụng thấp hơn tổng dư nợ. Cần ưu tiên trả nợ đúng hạn để tránh phát sinh lãi phạt."
                            }
                )
            }

            lower.contains("tiết kiệm") || lower.contains("kế hoạch") -> {
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Kế hoạch tích lũy 5.000.000 đ trong 3 tháng:**\n\n" +
                            "• Mục tiêu mỗi tháng: **1.700.000 đ**\n" +
                            "• Thu nhập dự kiến: **${FinanceEngine.formatVnd(userProfile.monthlyAllowance.add(userProfile.partTimeIncome))}**\n" +
                            "• Khuyến nghị: Trích lập ngay 1.700.000 đ vào tài khoản Tiết kiệm có kỳ hạn ngay sau ngày ${userProfile.incomeDayOfMonth} hàng tháng để tránh chi tiêu lẹm vào quỹ này."
                )
            }

            else -> {
                AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "🤖 **Tóm tắt tình trạng tài chính hiện tại:**\n\n" +
                            "• Tổng tài sản: **${FinanceEngine.formatVnd(totalAssets)}**\n" +
                            "• Tiền mặt khả dụng: **${FinanceEngine.formatVnd(liquidCash)}**\n" +
                            "• Tổng nợ: **${FinanceEngine.formatVnd(totalDebts)}**\n" +
                            "• Tài sản ròng: **${FinanceEngine.formatVnd(netWorth)}**\n\n" +
                            "Bạn có thể hỏi tôi về: Dự báo dòng tiền, kiểm tra khả năng mua sắm, hoặc rà soát ngân sách chi tiêu."
                )
            }
        }
    }
}

