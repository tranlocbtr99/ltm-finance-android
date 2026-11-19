package com.ltm.finance.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

data class BackupDataWrapper(
    val userProfile: UserProfile,
    val accounts: List<FinancialAccount>,
    val transactions: List<Transaction>,
    val debts: List<Debt>,
    val budgets: List<Budget>,
    val pinCode: String? = null
)

class FinanceRepository(private val context: Context? = null) {

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "USR-${UUID.randomUUID().toString().take(6)}",
            email = "sinhvien@university.edu.vn",
            displayName = "Sinh viên",
            university = "Đại học",
            academicYear = 1,
            monthlyAllowance = BigDecimal.ZERO,
            partTimeIncome = BigDecimal.ZERO,
            expectedLivingCost = BigDecimal.ZERO,
            financialGoal = "Quản lý chi tiêu hiệu quả",
            incomeDayOfMonth = 1,
            consentTerms = true,
            consentAiProcessing = true,
            consentResearchStudy = true,
            researchParticipantId = "PARTICIPANT-${(1000..9999).random()}"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Clean initial state (NO mock/demo data)
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

    private val _securityPin = MutableStateFlow<String?>(null)
    val securityPin: StateFlow<String?> = _securityPin.asStateFlow()

    private val gson = Gson()

    init {
        // Welcome message
        _chatMessages.value = listOf(
            AiChatMessage(
                id = "MSG-WELCOME",
                isUser = false,
                text = "Xin chào! Tôi là **LTM AI Finance Assistant**.\n\n" +
                        "Hệ thống đã sẵn sàng cho bạn. Bạn có thể:\n" +
                        "1. Thêm tài khoản/ví của bạn tại tab **Tài sản**.\n" +
                        "2. Tạo định mức tại tab **Ngân sách**.\n" +
                        "3. Nhập câu tự nhiên như *\"Ăn trưa 35k ví momo\"* để tôi tự phân tích và tạo lệnh ghi sổ nhanh!"
            )
        )
        logResearchEvent("APP_INITIALIZED", mapOf("mode" to "CLEAN_START"))
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

    // ==========================================
    // 1. TÀI KHOẢN & TÀI SẢN (Thêm, Sửa, Xóa thủ công)
    // ==========================================
    fun addAccount(
        name: String,
        type: AccountType,
        balance: BigDecimal,
        provider: String = "",
        isLiquid: Boolean = true,
        interestRateYearly: Double = 0.0,
        maturityDate: String? = null,
        termMonths: Int = 0
    ) {
        val newAccount = FinancialAccount(
            id = "ACC-${UUID.randomUUID().toString().take(6)}",
            name = name,
            type = type,
            balance = balance,
            provider = provider.ifBlank { type.displayName },
            isLiquid = isLiquid,
            interestRateYearly = interestRateYearly,
            maturityDate = maturityDate,
            termMonths = termMonths,
            updatedAt = System.currentTimeMillis()
        )
        _accounts.value = _accounts.value + newAccount
        logResearchEvent("ACCOUNT_CREATED", mapOf("name" to name, "type" to type.name, "balance" to balance.toString()))
        refreshProactiveAlerts()
    }

    fun editAccount(
        id: String,
        name: String,
        balance: BigDecimal,
        provider: String,
        isLiquid: Boolean,
        interestRateYearly: Double = 0.0,
        maturityDate: String? = null
    ) {
        _accounts.value = _accounts.value.map {
            if (it.id == id) {
                it.copy(
                    name = name,
                    balance = balance,
                    provider = provider,
                    isLiquid = isLiquid,
                    interestRateYearly = interestRateYearly,
                    maturityDate = maturityDate,
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
        logResearchEvent("ACCOUNT_EDITED", mapOf("id" to id, "newBalance" to balance.toString()))
        refreshProactiveAlerts()
    }

    fun deleteAccount(id: String) {
        _accounts.value = _accounts.value.filter { it.id != id }
        logResearchEvent("ACCOUNT_DELETED", mapOf("id" to id))
        refreshProactiveAlerts()
    }

    // ==========================================
    // 2. KHOẢN NỢ & VAY (Thêm, Sửa, Xóa thủ công)
    // ==========================================
    fun addDebt(
        name: String,
        type: DebtType,
        initialAmount: BigDecimal,
        currentBalance: BigDecimal,
        interestRateYearly: Double,
        paymentDueDay: Int,
        creditLimit: BigDecimal? = null
    ) {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val nextDue = LocalDate.now().withDayOfMonth(paymentDueDay.coerceIn(1, 28)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val newDebt = Debt(
            id = "DEBT-${UUID.randomUUID().toString().take(6)}",
            name = name,
            type = type,
            initialAmount = initialAmount,
            currentBalance = currentBalance,
            interestRateYearly = interestRateYearly,
            creditLimit = creditLimit,
            paymentDueDay = paymentDueDay,
            monthlyPaymentEstimate = currentBalance,
            startDate = today,
            endDate = today,
            schedules = listOf(
                RepaymentSchedule(
                    id = "SCH-${UUID.randomUUID().toString().take(6)}",
                    debtId = "",
                    dueDate = nextDue,
                    principal = currentBalance,
                    interest = BigDecimal.ZERO,
                    status = RepaymentStatus.UPCOMING
                )
            )
        )
        _debts.value = _debts.value + newDebt
        logResearchEvent("DEBT_CREATED", mapOf("name" to name, "balance" to currentBalance.toString()))
        refreshProactiveAlerts()
    }

    fun deleteDebt(id: String) {
        _debts.value = _debts.value.filter { it.id != id }
        logResearchEvent("DEBT_DELETED", mapOf("id" to id))
        refreshProactiveAlerts()
    }

    // ==========================================
    // 3. NGÂN SÁCH (Thêm, Sửa, Xóa thủ công)
    // ==========================================
    fun addBudget(
        category: String,
        amount: BigDecimal,
        warningThresholdPercent: Int = 80
    ) {
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val newBudget = Budget(
            id = "BG-${UUID.randomUUID().toString().take(6)}",
            category = category,
            originalAmount = amount,
            currentAmount = amount,
            spentAmount = BigDecimal.ZERO,
            monthYear = currentMonth,
            warningThresholdPercent = warningThresholdPercent
        )
        val updatedBudgets = _budgets.value + newBudget
        _budgets.value = FinanceEngine.updateBudgetSpending(updatedBudgets, _transactions.value, currentMonth)
        logResearchEvent("BUDGET_CREATED", mapOf("category" to category, "amount" to amount.toString()))
        refreshProactiveAlerts()
    }

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

    fun deleteBudget(budgetId: String) {
        _budgets.value = _budgets.value.filter { it.id != budgetId }
        logResearchEvent("BUDGET_DELETED", mapOf("budgetId" to budgetId))
        refreshProactiveAlerts()
    }

    // ==========================================
    // 4. GIAO DỊCH (Thêm, Xóa, Chuyển tiền)
    // ==========================================
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
                if (targetAccountId != null && targetAccountId != accountId) {
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

        logResearchEvent("TRANSACTION_LOGGED", mapOf("type" to type.name, "amount" to amount.toString(), "category" to category))
        refreshProactiveAlerts()
    }

    fun deleteTransaction(txId: String) {
        val tx = _transactions.value.find { it.id == txId } ?: return
        _transactions.value = _transactions.value.filter { it.id != txId }

        // Revert balance
        val currentAccounts = _accounts.value.toMutableList()
        when (tx.type) {
            TransactionType.INCOME -> {
                val idx = currentAccounts.indexOfFirst { it.id == tx.accountId }
                if (idx != -1) currentAccounts[idx] = currentAccounts[idx].copy(balance = currentAccounts[idx].balance.subtract(tx.amount))
            }
            TransactionType.EXPENSE -> {
                val idx = currentAccounts.indexOfFirst { it.id == tx.accountId }
                if (idx != -1) currentAccounts[idx] = currentAccounts[idx].copy(balance = currentAccounts[idx].balance.add(tx.amount))
            }
            TransactionType.TRANSFER -> {
                if (tx.targetAccountId != null) {
                    _accounts.value = FinanceEngine.applyTransfer(currentAccounts, tx.targetAccountId, tx.accountId, tx.amount)
                }
            }
        }
        if (tx.type != TransactionType.TRANSFER) {
            _accounts.value = currentAccounts
        }

        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        _budgets.value = FinanceEngine.updateBudgetSpending(_budgets.value, _transactions.value, currentMonth)
        logResearchEvent("TRANSACTION_DELETED", mapOf("id" to txId))
        refreshProactiveAlerts()
    }

    // ==========================================
    // 5. AI CONVERSATION & RECOMMENDATION LOG
    // ==========================================
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

    // ==========================================
    // 6. HỒ SƠ, BẢO MẬT & SAO LƯU (Ltmsav features)
    // ==========================================
    fun updateUserProfile(
        displayName: String,
        university: String,
        academicYear: Int,
        monthlyAllowance: BigDecimal,
        partTimeIncome: BigDecimal,
        financialGoal: String
    ) {
        _userProfile.value = _userProfile.value.copy(
            displayName = displayName,
            university = university,
            academicYear = academicYear,
            monthlyAllowance = monthlyAllowance,
            partTimeIncome = partTimeIncome,
            financialGoal = financialGoal
        )
        logResearchEvent("PROFILE_UPDATED", mapOf("name" to displayName))
    }

    fun setSecurityPin(pin: String?) {
        _securityPin.value = pin
    }

    fun resetAllData() {
        _accounts.value = emptyList()
        _transactions.value = emptyList()
        _debts.value = emptyList()
        _budgets.value = emptyList()
        _recommendations.value = emptyList()
        _chatMessages.value = listOf(
            AiChatMessage(
                id = "MSG-RESET",
                isUser = false,
                text = "Đã dọn sạch toàn bộ dữ liệu. Bạn có thể bắt đầu tạo tài khoản và ghi chép mới!"
            )
        )
        logResearchEvent("ALL_DATA_RESET", emptyMap())
    }

    fun exportBackupJson(): String {
        val wrapper = BackupDataWrapper(
            userProfile = _userProfile.value,
            accounts = _accounts.value,
            transactions = _transactions.value,
            debts = _debts.value,
            budgets = _budgets.value,
            pinCode = _securityPin.value
        )
        return gson.toJson(wrapper)
    }

    fun importBackupJson(jsonStr: String): Boolean {
        return try {
            val wrapper = gson.fromJson(jsonStr, BackupDataWrapper::class.java)
            _userProfile.value = wrapper.userProfile
            _accounts.value = wrapper.accounts
            _transactions.value = wrapper.transactions
            _debts.value = wrapper.debts
            _budgets.value = wrapper.budgets
            _securityPin.value = wrapper.pinCode
            refreshProactiveAlerts()
            logResearchEvent("BACKUP_RESTORED", emptyMap())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateConsent(terms: Boolean, ai: Boolean, research: Boolean) {
        _userProfile.value = _userProfile.value.copy(
            consentTerms = terms,
            consentAiProcessing = ai,
            consentResearchStudy = research
        )
        logResearchEvent("CONSENT_UPDATED", mapOf("ai" to ai, "research" to research))
    }

    fun logResearchEvent(type: String, meta: Map<String, Any>) {
        if (_userProfile.value.consentResearchStudy) {
            val event = ResearchEngine.createEvent(_userProfile.value.researchParticipantId, type, meta)
            _researchEvents.value = _researchEvents.value + event
        }
    }
}
