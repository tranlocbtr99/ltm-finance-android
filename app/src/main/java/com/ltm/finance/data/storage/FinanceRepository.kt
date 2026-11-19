package com.ltm.finance.data.storage

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
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

enum class CloudSyncStatus {
    OFFLINE,
    SYNCING,
    SYNCED,
    ERROR
}

data class CloudSyncState(
    val status: CloudSyncStatus = CloudSyncStatus.OFFLINE,
    val message: String = "Chưa kết nối đám mây",
    val lastSyncedTime: Long = 0L
)

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

    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }
    private var cloudListener: ListenerRegistration? = null
    private var isCloudUpdating = false

    private val _cloudSyncState = MutableStateFlow(CloudSyncState())
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    private val prefs by lazy {
        context?.getSharedPreferences("ltm_finance_prefs", Context.MODE_PRIVATE)
    }

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

        val isAuth = prefs?.getBoolean("is_authenticated", false) ?: false
        val savedRoleStr = prefs?.getString("user_role", UserRole.USER.name) ?: UserRole.USER.name
        val savedRole = try { UserRole.valueOf(savedRoleStr) } catch (e: Exception) { UserRole.USER }
        val hasAccepted = prefs?.getBoolean("has_accepted_terms", false) ?: false
        val savedEmail = prefs?.getString("google_email", null)

        if (isAuth) {
            val savedName = prefs?.getString("display_name", if (savedRole == UserRole.ADMIN) "Quản trị viên" else "Sinh viên") ?: "Sinh viên"
            val savedPhoto = prefs?.getString("photo_url", null)
            val isLinked = prefs?.getBoolean("is_google_linked", false) ?: false
            _userProfile.value = _userProfile.value.copy(
                role = savedRole,
                isAuthenticated = true,
                hasAcceptedTerms = hasAccepted,
                isGoogleLinked = isLinked,
                googleEmail = savedEmail,
                displayName = savedName,
                photoUrl = savedPhoto
            )
            if (isLinked && !savedEmail.isNullOrBlank()) {
                setupCloudSync(savedEmail)
            }
        }
    }

    private fun onDataChanged() {
        refreshProactiveAlerts()
        pushToCloud()
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
        onDataChanged()
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
        onDataChanged()
    }

    fun deleteAccount(id: String) {
        _accounts.value = _accounts.value.filter { it.id != id }
        logResearchEvent("ACCOUNT_DELETED", mapOf("id" to id))
        onDataChanged()
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
        onDataChanged()
    }

    fun deleteDebt(id: String) {
        _debts.value = _debts.value.filter { it.id != id }
        logResearchEvent("DEBT_DELETED", mapOf("id" to id))
        onDataChanged()
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
        onDataChanged()
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
        onDataChanged()
    }

    fun deleteBudget(budgetId: String) {
        _budgets.value = _budgets.value.filter { it.id != budgetId }
        logResearchEvent("BUDGET_DELETED", mapOf("budgetId" to budgetId))
        onDataChanged()
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
        onDataChanged()
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
        onDataChanged()
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
    fun loginAsUser(email: String, displayName: String, photoUrl: String?) {
        val finalName = if (displayName.isBlank() || displayName == "Sinh viên") "Sinh viên" else displayName
        _userProfile.value = _userProfile.value.copy(
            isGoogleLinked = true,
            googleEmail = email,
            displayName = finalName,
            photoUrl = photoUrl,
            role = UserRole.USER,
            isAuthenticated = true,
            hasAcceptedTerms = true
        )
        prefs?.edit()?.apply {
            putString("google_email", email)
            putString("display_name", finalName)
            putString("photo_url", photoUrl)
            putBoolean("is_google_linked", true)
            putString("user_role", UserRole.USER.name)
            putBoolean("is_authenticated", true)
            putBoolean("has_accepted_terms", true)
            apply()
        }
        logResearchEvent("AUTH_USER_LOGIN", mapOf("email" to email))
        setupCloudSync(email)
    }

    fun loginAsAdmin(adminCode: String): Boolean {
        if (adminCode.trim() == "8888" || adminCode.trim() == "admin123") {
            _userProfile.value = _userProfile.value.copy(
                role = UserRole.ADMIN,
                isAuthenticated = true,
                hasAcceptedTerms = true,
                displayName = "Quản trị viên Super Admin",
                email = "admin@ltmfinance.edu.vn"
            )
            prefs?.edit()?.apply {
                putString("user_role", UserRole.ADMIN.name)
                putBoolean("is_authenticated", true)
                putBoolean("has_accepted_terms", true)
                apply()
            }
            logResearchEvent("AUTH_ADMIN_LOGIN", mapOf("role" to "SUPER_ADMIN"))
            return true
        }
        return false
    }

    fun signOut() {
        val currentEmail = _userProfile.value.googleEmail ?: ""
        cloudListener?.remove()
        cloudListener = null
        prefs?.edit()?.apply {
            remove("google_email")
            remove("display_name")
            remove("photo_url")
            putBoolean("is_google_linked", false)
            putBoolean("is_authenticated", false)
            putString("user_role", UserRole.USER.name)
            apply()
        }
        _userProfile.value = _userProfile.value.copy(
            isGoogleLinked = false,
            googleEmail = null,
            photoUrl = null,
            isAuthenticated = false,
            role = UserRole.USER
        )
        _cloudSyncState.value = CloudSyncState(
            status = CloudSyncStatus.OFFLINE,
            message = "Chưa kết nối đám mây"
        )
        logResearchEvent("AUTH_SIGNOUT", mapOf("email" to currentEmail))
    }

    fun linkGoogleAccount(email: String, displayName: String, photoUrl: String?) {
        loginAsUser(email, displayName, photoUrl)
    }

    fun unlinkGoogleAccount() {
        signOut()
    }

    fun setupCloudSync(email: String) {
        val db = firestore ?: return
        val docId = email.replace(".", "_")
        cloudListener?.remove()

        _cloudSyncState.value = CloudSyncState(
            status = CloudSyncStatus.SYNCING,
            message = "Đang kết nối đám mây..."
        )

        cloudListener = db.collection("ltm_users").document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _cloudSyncState.value = CloudSyncState(
                        status = CloudSyncStatus.ERROR,
                        message = "Lỗi đám mây: ${error.localizedMessage ?: "Mất kết nối"}"
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        isCloudUpdating = true
                        val accsJson = snapshot.getString("accountsJson")
                        if (!accsJson.isNullOrBlank()) {
                            val type = object : TypeToken<List<FinancialAccount>>() {}.type
                            _accounts.value = gson.fromJson(accsJson, type)
                        }
                        val txsJson = snapshot.getString("transactionsJson")
                        if (!txsJson.isNullOrBlank()) {
                            val type = object : TypeToken<List<Transaction>>() {}.type
                            _transactions.value = gson.fromJson(txsJson, type)
                        }
                        val debtsJson = snapshot.getString("debtsJson")
                        if (!debtsJson.isNullOrBlank()) {
                            val type = object : TypeToken<List<Debt>>() {}.type
                            _debts.value = gson.fromJson(debtsJson, type)
                        }
                        val budgetsJson = snapshot.getString("budgetsJson")
                        if (!budgetsJson.isNullOrBlank()) {
                            val type = object : TypeToken<List<Budget>>() {}.type
                            _budgets.value = gson.fromJson(budgetsJson, type)
                        }
                        val pin = snapshot.getString("securityPin")
                        if (!pin.isNullOrBlank()) {
                            _securityPin.value = pin
                        }
                        _cloudSyncState.value = CloudSyncState(
                            status = CloudSyncStatus.SYNCED,
                            message = "Đã đồng bộ từ đám mây",
                            lastSyncedTime = System.currentTimeMillis()
                        )
                        refreshProactiveAlerts()
                    } catch (e: Exception) {
                        _cloudSyncState.value = CloudSyncState(
                            status = CloudSyncStatus.ERROR,
                            message = "Lỗi đọc dữ liệu đám mây: ${e.message}"
                        )
                    } finally {
                        isCloudUpdating = false
                    }
                } else {
                    // Document doesn't exist yet on cloud, push initial local data
                    pushToCloud()
                }
            }
    }

    fun pushToCloud() {
        val email = _userProfile.value.googleEmail
        if (email.isNullOrBlank() || isCloudUpdating) return
        val db = firestore ?: return

        _cloudSyncState.value = _cloudSyncState.value.copy(
            status = CloudSyncStatus.SYNCING,
            message = "Đang lưu lên đám mây..."
        )
        val docId = email.replace(".", "_")
        val data = mapOf(
            "email" to email,
            "displayName" to _userProfile.value.displayName,
            "accountsJson" to gson.toJson(_accounts.value),
            "transactionsJson" to gson.toJson(_transactions.value),
            "debtsJson" to gson.toJson(_debts.value),
            "budgetsJson" to gson.toJson(_budgets.value),
            "securityPin" to (_securityPin.value ?: ""),
            "lastUpdatedAt" to System.currentTimeMillis()
        )
        db.collection("ltm_users").document(docId).set(data, SetOptions.merge())
            .addOnSuccessListener {
                _cloudSyncState.value = CloudSyncState(
                    status = CloudSyncStatus.SYNCED,
                    message = "Đã đồng bộ đám mây",
                    lastSyncedTime = System.currentTimeMillis()
                )
            }
            .addOnFailureListener { e ->
                _cloudSyncState.value = CloudSyncState(
                    status = CloudSyncStatus.ERROR,
                    message = "Lỗi đồng bộ: ${e.localizedMessage ?: "Mất kết nối"}",
                    lastSyncedTime = System.currentTimeMillis()
                )
            }
    }

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
        onDataChanged()
    }

    fun setSecurityPin(pin: String?) {
        _securityPin.value = pin
        onDataChanged()
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
        onDataChanged()
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
            onDataChanged()
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
