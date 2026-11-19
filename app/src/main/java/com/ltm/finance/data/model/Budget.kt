package com.ltm.finance.data.model

import java.math.BigDecimal

data class BudgetAdjustment(
    val id: String,
    val previousAmount: BigDecimal,
    val newAmount: BigDecimal,
    val modifiedBy: String, // "User" hoặc "AI Proposed"
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String
)

data class Budget(
    val id: String,
    val category: String,
    val originalAmount: BigDecimal,      // Ngân sách gốc ban đầu
    val aiProposedAmount: BigDecimal? = null, // Ngân sách AI gợi ý (FR-016)
    val userAdjustedAmount: BigDecimal? = null, // Người dùng tự sửa
    val currentAmount: BigDecimal,       // Ngân sách hiện hành đang áp dụng
    val spentAmount: BigDecimal = BigDecimal.ZERO,
    val monthYear: String, // "YYYY-MM"
    val warningThresholdPercent: Int = 80, // Cảnh báo mặc định 80%
    val adjustments: List<BudgetAdjustment> = emptyList()
) {
    val remainingAmount: BigDecimal
        get() = currentAmount.subtract(spentAmount)

    val usagePercent: Int
        get() = if (currentAmount.compareTo(BigDecimal.ZERO) > 0) {
            spentAmount.multiply(BigDecimal(100)).divide(currentAmount, 0, java.math.RoundingMode.HALF_UP).toInt()
        } else 0

    val isOverBudget: Boolean
        get() = spentAmount > currentAmount

    val isNearLimit: Boolean
        get() = usagePercent >= warningThresholdPercent && !isOverBudget
}
