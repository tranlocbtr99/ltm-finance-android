package com.ltm.finance.data.storage

import android.content.Context
import com.ltm.finance.data.model.*
import com.ltm.finance.domain.AiFinanceAssistant
import com.ltm.finance.domain.FinanceEngine
import com.ltm.finance.domain.ForecastEngine
import com.ltm.finance.domain.ResearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class FinanceRepository(private val context: Context? = null) {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _accounts = MutableStateFlow<List<FinancialAccount>>(emptyList())
    val accounts: StateFlow<List<FinancialAccount>> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _debts = MutableStateFlow<List<Debt>>(emptyList())
    val debts: StateFlow<List<Debt>> = _debts.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _recommendations = MutableStateFlow<List<AiRecommendation>>(emptyList())
    val recommendations: StateFlow<List<AiRecommendation>> = _recommendations.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _researchEvents = MutableStateFlow<List<ResearchEvent>>(emptyList())
    val researchEvents: StateFlow<List<ResearchEvent>> = _researchEvents.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val today = LocalDate.now()
        val currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // 1. Initial Accounts (FR-008, FR-009)
        val initialAccounts = listOf(
            FinancialAccount(
                id = "ACC-01",
                name = "Ví MoMo sinh viên",
                type = AccountType.E_WALLET,
                balance = BigDecimal("850000"),
                provider = "MoMo",
                isLiquid = true
            ),
            FinancialAccount(
                id = "ACC-02",
                name = "Vietcombank Digital",
                type = AccountType.BANK,
                balance = BigDecimal("3450000"),
                provider = "Vietcombank",
                isLiquid = true
            ),
            FinancialAccount(
                id = "ACC-03",
                name = "Tiền mặt trong ví",
                type = AccountType.CASH,
                balance = BigDecimal("420000"),
                provider = "Tiền mặt",
                isLiquid = true
            ),
            FinancialAccount(
                id = "ACC-04",
                name = "Sổ tiết kiệm dự phòng (6 tháng)",
                type = AccountType.SAVINGS,
                balance = BigDecimal("5000000"),
                provider = "BIDV",
                isLiquid = false, // Tiền gửi có kỳ hạn khóa lại (FR-010)
                interestRateYearly = 5.3,
                maturityDate = today.plusMonths(4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                termMonths = 6
            )
        )

        // 2. Initial Debts (FR-011, FR-012, FR-013)
        val initialDebts = listOf(
            Debt(
                id = "DEBT-01",
                name = "Trả góp điện thoại",
                type = DebtType.INSTALLMENT,
                initialAmount = BigDecimal("6000000"),
                currentBalance = BigDecimal("2000000"),
                interestRateYearly = 0.0,
                paymentDueDay = 25,
                monthlyPaymentEstimate = BigDecimal("1000000"),
                startDate = "2026-08-25",
                endDate = "2027-02-25",
                schedules = listOf(
                    RepaymentSchedule(
                        id = "SCH-01",
                        debtId = "DEBT-01",
                        dueDate = today.plusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        principal = BigDecimal("1000000"),
                        interest = BigDecimal.ZERO,
                        status = RepaymentStatus.UPCOMING
                    )
                )
            ),
            Debt(
                id = "DEBT-02",
                name = "Thẻ tín dụng sinh viên Shinhan",
                type = DebtType.CREDIT_CARD,
                initialAmount = BigDecimal("5000000"),
                currentBalance = BigDecimal("750000"),
                interestRateYearly = 18.0,
                creditLimit = BigDecimal("5000000"),
                statementDay = 20,
                paymentDueDay = 5,
                monthlyPaymentEstimate = BigDecimal("750000"),
                startDate = "2026-01-01",
                endDate = "2027-01-01"
            )
        )

        // 3. Initial Transactions (FR-003, FR-004)
        val initialTransactions = listOf(
            Transaction(
                id = "TX-01",
                type = TransactionType.INCOME,
                amount = BigDecimal("3500000"),
                date = today.withDayOfMonth(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                accountId = "ACC-02",
                category = "Trợ cấp gia đình",
                note = "Bố mẹ gửi tiền sinh hoạt tháng $currentMonth"
            ),
            Transaction(
                id = "TX-02",
                type = TransactionType.INCOME,
                amount = BigDecimal("1800000"),
                date = today.withDayOfMonth(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                accountId = "ACC-02",
                category = "Lương làm thêm",
                note = "Lương gia sư tuần 1-2"
            ),
            Transaction(
                id = "TX-03",
                type = TransactionType.EXPENSE,
                amount = BigDecimal("1800000"),
                date = today.withDayOfMonth(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                accountId = "ACC-02",
                category = "Nhà ở",
                note = "Tiền phòng trọ và điện nước tháng này"
            ),
            Transaction(
                id = "TX-04",
                type = TransactionType.EXPENSE,
                amount = BigDecimal("1450000"),
                date = today.minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                accountId = "ACC-01",
                category = "Ăn uống",
                note = "Cơm trưa, đi chợ cả tuần và cafe"
            ),
            Transaction(
                id = "TX-05",
                type = TransactionType.EXPENSE,
                amount = BigDecimal("320000"),
                date = today.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                accountId = "ACC-03",
                category = "Đi lại",
                note = "Đổ xăng xe máy và vé xe bus tháng"
            ),
            Transaction(
                id = "TX-06",
                type = TransactionType.EXPENSE,
                amount = BigDecimal("450000"),
                date = dateStr,
                accountId = "ACC-01",
                category = "Học tập",
                note = "Mua giáo trình chuyên ngành và photo tài liệu"
            )
        )

        // 4. Initial Budgets (FR-014, FR-015, FR-016)
        val initialBudgets = listOf(
            Budget(
                id = "BG-01",
                category = "Ăn uống",
                originalAmount = BigDecimal("2000000"),
                currentAmount = BigDecimal("2000000"),
                spentAmount = BigDecimal("1450000"),
                monthYear = currentMonth,
                warningThresholdPercent = 80
            ),
            Budget(
                id = "BG-02",
                category = "Nhà ở",
                originalAmount = BigDecimal("1800000"),
                currentAmount = BigDecimal("1800000"),
                spentAmount = BigDecimal("1800000"),
                monthYear = currentMonth,
                warningThresholdPercent = 80
            ),
            Budget(
                id = "BG-03",
                category = "Đi lại",
                originalAmount = BigDecimal("500000"),
                currentAmount = BigDecimal("500000"),
                spentAmount = BigDecimal("320000"),
                monthYear = currentMonth,
                warningThresholdPercent = 80
            ),
            Budget(
                id = "BG-04",
                category = "Học tập",
                originalAmount = BigDecimal("600000"),
                currentAmount = BigDecimal("600000"),
                spentAmount = BigDecimal("450000"),
                monthYear = currentMonth,
                warningThresholdPercent = 80
            ),
            Budget(
                id = "BG-05",
                category = "Giải trí",
                originalAmount = BigDecimal("400000"),
                currentAmount = BigDecimal("400000"),
                spentAmount = BigDecimal("150000"),
                monthYear = currentMonth,
                warningThresholdPercent = 80
            )
        )

        _accounts.value = initialAccounts
        _debts.value = initialDebts
        _transactions.value = initialTransactions
        _budgets.value = FinanceEngine.updateBudgetSpending(initialBudgets, initialTransactions, currentMonth)

        // Seed initial AI conversation
        _chatMessages.value = listOf(
            AiChatMessage(
                id = "MSG-01",
                isUser = false,
                text = "Xin chào! Tôi là **LTM AI Finance Assistant** của bạn.\n\n" +
                        "Tôi kết nối trực tiếp với **Finance Engine** để bảo đảm mọi tính toán số dư, ngân sách và dự báo đều chính xác 100%.\n\n" +
                        "Bạn có thể hỏi tôi về đánh giá mua sắm, dự báo dòng tiền 30 ngày, hoặc xem các cảnh báo ngân sách kịp thời."
            )
        )

        // Generate proactive alerts
        refreshProactiveAlerts()

        // Seed initial research events
        val initialEvents = listOf(
            ResearchEngine.createEvent(
                _userProfile.value.researchParticipantId,
                "SYSTEM_INIT",
                mapOf("academicYear" to 3, "study" to "Longitudinal Observational Study")
            ),
            ResearchEngine.createEvent(
                _userProfile.value.researchParticipantId,
                "BUDGET_CREATED",
                mapOf("categoriesCount" to 5)
            )
        )
        _researchEvents.value = initialEvents
    }

    fun refreshProactiveAlerts() {
        val liquidCash = FinanceEngine.calculateLiquidCash(_accounts.value)
        val forecast = ForecastEngine.generateForecast(
            startLiquidBalance = liquidCash,
            userProfile = _userProfile.value,
            recurringTransactions = _transactions.value.filter { it.recurrence != RecurrenceRule.NONE },
            debts = _debts.value,
            daysAhead = 30
        )
        val alerts = AiFinanceAssistant.scanProactiveAlerts(
            accounts = _accounts.value,
            debts = _debts.value,
            budgets = _budgets.value,
            forecast = forecast
        )
        _recommendations.value = alerts
    }

    // FR-003, FR-004, FR-005: Add Transaction
    fun addTransaction(
        type: TransactionType,
        amount: BigDecimal,
        date: String,
        accountId: String,
        targetAccountId: String? = null,
        category: String,
        note: String
    ) {
        val newTx = Transaction(
            id = "TX-${UUID.randomUUID().toString().take(8)}",
            type = type,
            amount = amount,
            date = date,
            accountId = accountId,
            targetAccountId = targetAccountId,
            category = category,
            note = note
        )

        val updatedTxList = listOf(newTx) + _transactions.value
        _transactions.value = updatedTxList

        // Update Account Balances
        val currentAccounts = _accounts.value.toMutableList()
        when (type) {
            TransactionType.INCOME -> {
                val idx = currentAccounts.indexOfFirst { it.id == accountId }
                if (idx != -1) {
                    val acc = currentAccounts[idx]
                    currentAccounts[idx] = acc.copy(balance = acc.balance.add(amount))
                }
            }
            TransactionType.EXPENSE -> {
                val idx = currentAccounts.indexOfFirst { it.id == accountId }
                if (idx != -1) {
                    val acc = currentAccounts[idx]
                    currentAccounts[idx] = acc.copy(balance = acc.balance.subtract(amount))
                }
            }
            TransactionType.TRANSFER -> {
                if (targetAccountId != null) {
                    _accounts.value = FinanceEngine.applyTransfer(currentAccounts, accountId, targetAccountId, amount)
                }
            }
        }
        if (type != TransactionType.TRANSFER) {
            _accounts.value = currentAccounts
        }

        // Recalculate Budgets
        val currentMonth = date.take(7)
        _budgets.value = FinanceEngine.updateBudgetSpending(_budgets.value, updatedTxList, currentMonth)

        // Log Research Event
        logResearchEvent("TRANSACTION_LOGGED", mapOf("type" to type.name, "amount" to amount.toString(), "category" to category))

        refreshProactiveAlerts()
    }

    // FR-016: Adjust Budget
    fun adjustBudget(budgetId: String, newAmount: BigDecimal, reason: String, modifiedBy: String = "User") {
        val updated = _budgets.value.map { b ->
            if (b.id == budgetId) {
                val adj = BudgetAdjustment(
                    id = "ADJ-${UUID.randomUUID().toString().take(6)}",
                    previousAmount = b.currentAmount,
                    newAmount = newAmount,
                    modifiedBy = modifiedBy,
                    reason = reason
                )
                b.copy(
                    userAdjustedAmount = if (modifiedBy == "User") newAmount else b.userAdjustedAmount,
                    aiProposedAmount = if (modifiedBy.contains("AI")) newAmount else b.aiProposedAmount,
                    currentAmount = newAmount,
                    adjustments = b.adjustments + adj
                )
            } else b
        }
        _budgets.value = updated
        logResearchEvent("BUDGET_ADJUSTED", mapOf("budgetId" to budgetId, "newAmount" to newAmount.toString(), "reason" to reason))
        refreshProactiveAlerts()
    }

    // FR-024: Update Recommendation Status
    fun updateRecommendationStatus(
        recId: String,
        status: RecommendationStatus,
        feedback: AdviceFeedback? = null,
        verification: VerificationResult = VerificationResult.NO_EVIDENCE_YET
    ) {
        val updated = _recommendations.value.map { rec ->
            if (rec.id == recId) {
                rec.copy(
                    status = status,
                    feedback = feedback ?: rec.feedback,
                    verification = verification,
                    respondedAt = System.currentTimeMillis()
                )
            } else rec
        }
        _recommendations.value = updated
        logResearchEvent("AI_RECOMMENDATION_STATUS_CHANGE", mapOf("recId" to recId, "status" to status.name))
    }

    // Send On-Demand Chat Message
    fun sendChatMessage(userText: String) {
        val userMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = userText
        )
        val assistantMsg = AiFinanceAssistant.answerQuestion(
            query = userText,
            accounts = _accounts.value,
            debts = _debts.value,
            budgets = _budgets.value,
            transactions = _transactions.value,
            userProfile = _userProfile.value
        )
        _chatMessages.value = _chatMessages.value + userMsg + assistantMsg
        logResearchEvent("AI_CHAT_QUERY", mapOf("length" to userText.length))
    }

    // Update Consent
    fun updateConsent(terms: Boolean, ai: Boolean, research: Boolean) {
        _userProfile.value = _userProfile.value.copy(
            consentTerms = terms,
            consentAiProcessing = ai,
            consentResearchStudy = research
        )
        logResearchEvent("CONSENT_UPDATED", mapOf("ai" to ai, "research" to research))
    }

    private fun logResearchEvent(type: String, meta: Map<String, Any>) {
        if (_userProfile.value.consentResearchStudy) {
            val event = ResearchEngine.createEvent(_userProfile.value.researchParticipantId, type, meta)
            _researchEvents.value = _researchEvents.value + event
        }
    }
}

