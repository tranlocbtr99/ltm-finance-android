package com.ltm.finance.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ltm.finance.data.model.*
import com.ltm.finance.data.storage.CloudSyncState
import com.ltm.finance.data.storage.FinanceRepository
import com.ltm.finance.data.update.AppUpdateManager
import com.ltm.finance.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class OverviewUiState(
    val totalAssets: BigDecimal = BigDecimal.ZERO,
    val liquidCash: BigDecimal = BigDecimal.ZERO,
    val totalDebts: BigDecimal = BigDecimal.ZERO,
    val netWorth: BigDecimal = BigDecimal.ZERO,
    val monthIncome: BigDecimal = BigDecimal.ZERO,
    val monthExpense: BigDecimal = BigDecimal.ZERO,
    val netMonthlySavings: BigDecimal = BigDecimal.ZERO,
    val totalBudget: BigDecimal = BigDecimal.ZERO,
    val totalBudgetSpent: BigDecimal = BigDecimal.ZERO,
    val budgetUsagePercent: Int = 0,
    val upcomingDebtDueSoon: RepaymentSchedule? = null,
    val activeShortageAlert: ShortageAlert? = null,
    val topProactiveRecommendations: List<AiRecommendation> = emptyList()
)

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    val repository: FinanceRepository = FinanceRepository(application.applicationContext)

    val userProfile = repository.userProfile
    val accounts = repository.accounts
    val transactions = repository.transactions
    val debts = repository.debts
    val budgets = repository.budgets
    val recommendations = repository.recommendations
    val chatMessages = repository.chatMessages
    val researchEvents = repository.researchEvents
    val securityPin = repository.securityPin
    val cloudSyncState = repository.cloudSyncState

    fun triggerCloudSync() = repository.pushToCloud()

    // In-App Auto-Update Manager
    private val updateManager = AppUpdateManager(application.applicationContext)
    val updateStatus = updateManager.status

    fun checkForUpdates() {
        viewModelScope.launch {
            updateManager.checkForUpdates()
        }
    }

    fun downloadAndInstallUpdate(url: String) {
        updateManager.openDownloadInBrowser(url)
    }

    fun dismissUpdate() {
        updateManager.resetStatus()
    }

    fun getAppVersion(): String = updateManager.getCurrentVersionName()

    // Security Lock state
    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked = _isUnlocked.asStateFlow()

    private val _forecastDays = MutableStateFlow(30)
    val forecastDays = _forecastDays.asStateFlow()

    // Simulation State
    private val _purchaseSimulation = MutableStateFlow<PurchaseSimulationResult?>(null)
    val purchaseSimulation = _purchaseSimulation.asStateFlow()

    // Combined Overview State
    val overviewState: StateFlow<OverviewUiState> = combine(
        accounts,
        debts,
        transactions,
        budgets,
        recommendations
    ) { accs, dbts, txs, bdgs, recs ->
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val totalAssets = FinanceEngine.calculateTotalAssets(accs)
        val liquidCash = FinanceEngine.calculateLiquidCash(accs)
        val totalDebts = FinanceEngine.calculateTotalDebts(dbts)
        val netWorth = FinanceEngine.calculateNetWorth(accs, dbts)
        val monthIncome = FinanceEngine.calculateMonthIncome(txs, currentMonth)
        val monthExpense = FinanceEngine.calculateMonthExpense(txs, currentMonth)
        val netSavings = monthIncome.subtract(monthExpense)

        val totalBudget = bdgs.fold(BigDecimal.ZERO) { acc, b -> acc.add(b.currentAmount) }
        val totalSpent = bdgs.fold(BigDecimal.ZERO) { acc, b -> acc.add(b.spentAmount) }
        val budgetUsage = if (totalBudget > BigDecimal.ZERO) {
            totalSpent.multiply(BigDecimal(100)).divide(totalBudget, 0, java.math.RoundingMode.HALF_UP).toInt()
        } else 0

        val upcomingSchedule = dbts.flatMap { it.schedules }
            .filter { it.status == RepaymentStatus.UPCOMING || it.status == RepaymentStatus.DUE_TODAY }
            .minByOrNull { it.dueDate }

        OverviewUiState(
            totalAssets = totalAssets,
            liquidCash = liquidCash,
            totalDebts = totalDebts,
            netWorth = netWorth,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            netMonthlySavings = netSavings,
            totalBudget = totalBudget,
            totalBudgetSpent = totalSpent,
            budgetUsagePercent = budgetUsage,
            upcomingDebtDueSoon = upcomingSchedule,
            topProactiveRecommendations = recs.filter { it.status == RecommendationStatus.CREATED || it.status == RecommendationStatus.VIEWED }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverviewUiState()
    )

    // Current Cashflow Forecast
    val currentForecast: StateFlow<CashflowForecast> = combine(
        accounts,
        debts,
        transactions,
        userProfile,
        forecastDays
    ) { accs, dbts, txs, profile, days ->
        val liquid = FinanceEngine.calculateLiquidCash(accs)
        ForecastEngine.generateForecast(
            startLiquidBalance = liquid,
            userProfile = profile,
            recurringTransactions = txs.filter { it.recurrence != RecurrenceRule.NONE },
            debts = dbts,
            daysAhead = days
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CashflowForecast(30, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, emptyList(), emptyList())
    )

    // Research & Admin Metrics
    val researchMetrics: StateFlow<ResearchSummaryMetrics> = combine(
        researchEvents,
        recommendations,
        budgets,
        transactions,
        userProfile
    ) { events, recs, bdgs, txs, profile ->
        ResearchEngine.computeMetrics(
            participantId = profile.researchParticipantId,
            events = events,
            recommendations = recs,
            budgets = bdgs,
            transactionsCount = txs.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ResearchSummaryMetrics("N/A", 0, 100.0, 0, 0.0, 0.0, 1)
    )

    // Check PIN lock on launch & Auto check updates
    init {
        viewModelScope.launch {
            securityPin.collect { pin ->
                if (!pin.isNullOrBlank()) {
                    _isUnlocked.value = false
                } else {
                    _isUnlocked.value = true
                }
            }
        }
        viewModelScope.launch {
            updateManager.checkForUpdates()
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        return if (securityPin.value == enteredPin) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun setSecurityPin(newPin: String?) {
        repository.setSecurityPin(newPin)
    }

    fun setForecastDays(days: Int) {
        _forecastDays.value = days
    }

    // Account CRUD
    fun addAccount(
        name: String,
        type: AccountType,
        balance: BigDecimal,
        provider: String,
        isLiquid: Boolean,
        interestRateYearly: Double = 0.0,
        maturityDate: String? = null
    ) {
        repository.addAccount(name, type, balance, provider, isLiquid, interestRateYearly, maturityDate)
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
        repository.editAccount(id, name, balance, provider, isLiquid, interestRateYearly, maturityDate)
    }

    fun deleteAccount(id: String) {
        repository.deleteAccount(id)
    }

    // Budget CRUD
    fun addBudget(category: String, amount: BigDecimal, warningThreshold: Int = 80) {
        repository.addBudget(category, amount, warningThreshold)
    }

    fun adjustBudget(budgetId: String, newAmount: BigDecimal, reason: String) {
        repository.adjustBudget(budgetId, newAmount, reason, "User")
    }

    fun deleteBudget(budgetId: String) {
        repository.deleteBudget(budgetId)
    }

    // Debt CRUD
    fun addDebt(
        name: String,
        type: DebtType,
        initialAmount: BigDecimal,
        currentBalance: BigDecimal,
        interestRateYearly: Double,
        paymentDueDay: Int
    ) {
        repository.addDebt(name, type, initialAmount, currentBalance, interestRateYearly, paymentDueDay)
    }

    fun deleteDebt(id: String) {
        repository.deleteDebt(id)
    }

    // Transaction CRUD
    fun addTransaction(
        type: TransactionType,
        amount: BigDecimal,
        date: String,
        accountId: String,
        targetAccountId: String? = null,
        category: String,
        note: String
    ) {
        repository.addTransaction(type, amount, date, accountId, targetAccountId, category, note)
    }

    fun deleteTransaction(txId: String) {
        repository.deleteTransaction(txId)
    }

    // AI
    fun applyAiBudgetRecommendation(rec: AiRecommendation) {
        if (rec.proposedBudget != null) {
            val budget = budgets.value.find { it.category == rec.category }
            if (budget != null) {
                repository.adjustBudget(
                    budgetId = budget.id,
                    newAmount = rec.proposedBudget,
                    reason = "Áp dụng theo khuyến nghị AI: ${rec.title}",
                    modifiedBy = "AI Proposed (User Accepted)"
                )
            }
        }
        repository.updateRecommendationStatus(rec.id, RecommendationStatus.ACCEPTED)
    }

    fun rejectAiRecommendation(recId: String, feedback: AdviceFeedback? = null) {
        repository.updateRecommendationStatus(recId, RecommendationStatus.REJECTED, feedback)
    }

    fun markRecommendationImplemented(recId: String, isPartial: Boolean = false) {
        val status = if (isPartial) RecommendationStatus.PARTIALLY_IMPLEMENTED else RecommendationStatus.IMPLEMENTED
        val verification = if (isPartial) VerificationResult.PARTIALLY_FOUND else VerificationResult.EVIDENCE_FOUND
        repository.updateRecommendationStatus(recId, status, verification = verification)
    }

    fun sendChatMessage(text: String) {
        repository.sendChatMessage(text)
    }

    fun runPurchaseSimulation(itemName: String, price: BigDecimal, installmentMonths: Int = 6) {
        val liquid = FinanceEngine.calculateLiquidCash(accounts.value)
        val profile = userProfile.value
        val surplus = profile.monthlyAllowance.add(profile.partTimeIncome).subtract(profile.expectedLivingCost)
        val result = SimulationEngine.simulatePurchase(
            itemName = itemName,
            price = price,
            currentLiquidCash = liquid,
            monthlySurplus = surplus,
            installmentMonths = installmentMonths
        )
        _purchaseSimulation.value = result
    }

    fun updateConsent(terms: Boolean, ai: Boolean, research: Boolean) {
        repository.updateConsent(terms, ai, research)
    }

    // Profile, Auth & Reset
    fun loginAsUser(email: String, displayName: String, photoUrl: String?) {
        repository.loginAsUser(email, displayName, photoUrl)
    }

    fun loginAsAdmin(adminCode: String): Boolean {
        return repository.loginAsAdmin(adminCode)
    }

    fun signOut() {
        repository.signOut()
    }

    fun linkGoogleAccount(email: String, displayName: String, photoUrl: String?) {
        repository.linkGoogleAccount(email, displayName, photoUrl)
    }

    fun unlinkGoogleAccount() {
        repository.unlinkGoogleAccount()
    }

    fun updateUserProfile(
        displayName: String,
        university: String,
        academicYear: Int,
        monthlyAllowance: BigDecimal,
        partTimeIncome: BigDecimal,
        financialGoal: String
    ) {
        repository.updateUserProfile(displayName, university, academicYear, monthlyAllowance, partTimeIncome, financialGoal)
    }

    fun resetAllData() {
        repository.resetAllData()
    }

    fun exportBackupJson(): String = repository.exportBackupJson()

    fun importBackupJson(json: String): Boolean = repository.importBackupJson(json)
}
