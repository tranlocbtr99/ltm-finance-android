package com.ltm.finance.domain

import com.ltm.finance.data.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FinanceEngine {

    fun formatVnd(amount: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale.GERMANY).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return "${formatter.format(amount)} đ"
    }

    /**
     * FR-010 & FR-008:
     * Tổng tài sản = Tổng số dư các tài khoản (Tiền mặt, Ngân hàng, Ví, Tiết kiệm, v.v.)
     */
    fun calculateTotalAssets(accounts: List<FinancialAccount>): BigDecimal {
        return accounts.fold(BigDecimal.ZERO) { acc, account ->
            acc.add(account.balance)
        }
    }

    /**
     * Tiền thanh khoản / Có thể sử dụng ngay:
     * Chỉ gồm tài khoản có `isLiquid == true` (loại trừ sổ tiết kiệm có kỳ hạn chưa đáo hạn).
     */
    fun calculateLiquidCash(accounts: List<FinancialAccount>): BigDecimal {
        return accounts.filter { it.isLiquid }.fold(BigDecimal.ZERO) { acc, account ->
            acc.add(account.balance)
        }
    }

    /**
     * FR-011: Tổng nợ còn phải trả
     */
    fun calculateTotalDebts(debts: List<Debt>): BigDecimal {
        return debts.fold(BigDecimal.ZERO) { acc, debt ->
            acc.add(debt.currentBalance)
        }
    }

    /**
     * FR-010: Tài sản ròng = Tổng tài sản - Tổng nợ
     */
    fun calculateNetWorth(accounts: List<FinancialAccount>, debts: List<Debt>): BigDecimal {
        val totalAssets = calculateTotalAssets(accounts)
        val totalDebts = calculateTotalDebts(debts)
        return totalAssets.subtract(totalDebts)
    }

    /**
     * Tính tổng thu nhập trong tháng (YYYY-MM).
     * FR-005: Chuyển tiền (TRANSFER) KHÔNG tính vào thu nhập hay chi tiêu.
     */
    fun calculateMonthIncome(transactions: List<Transaction>, monthYearPrefix: String): BigDecimal {
        return transactions
            .filter { it.type == TransactionType.INCOME && it.date.startsWith(monthYearPrefix) && !it.isPending }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
    }

    /**
     * Tính tổng chi tiêu trong tháng (YYYY-MM).
     */
    fun calculateMonthExpense(transactions: List<Transaction>, monthYearPrefix: String): BigDecimal {
        return transactions
            .filter { it.type == TransactionType.EXPENSE && it.date.startsWith(monthYearPrefix) && !it.isPending }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
    }

    /**
     * Cập nhật số tiền đã chi (spentAmount) cho từng hạng mục ngân sách từ danh sách giao dịch
     */
    fun updateBudgetSpending(
        budgets: List<Budget>,
        transactions: List<Transaction>,
        monthYearPrefix: String
    ): List<Budget> {
        val expenseByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date.startsWith(monthYearPrefix) && !it.isPending }
            .groupBy { it.category }
            .mapValues { entry ->
                entry.value.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
            }

        return budgets.map { b ->
            val spent = expenseByCategory[b.category] ?: BigDecimal.ZERO
            b.copy(spentAmount = spent)
        }
    }

    /**
     * FR-005: Xử lý giao dịch chuyển tiền giữa 2 tài khoản
     */
    fun applyTransfer(
        accounts: List<FinancialAccount>,
        sourceId: String,
        targetId: String,
        amount: BigDecimal
    ): List<FinancialAccount> {
        return accounts.map { account ->
            when (account.id) {
                sourceId -> account.copy(balance = account.balance.subtract(amount))
                targetId -> account.copy(balance = account.balance.add(amount))
                else -> account
            }
        }
    }
}

