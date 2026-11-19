package com.ltm.finance.data.model

import java.math.BigDecimal

enum class TransactionType(val displayName: String) {
    INCOME("Thu nhập"),
    EXPENSE("Chi tiêu"),
    TRANSFER("Chuyển tiền")
}

enum class RecurrenceRule(val displayName: String) {
    NONE("Một lần"),
    DAILY("Hàng ngày"),
    WEEKLY("Hàng tuần"),
    MONTHLY("Hàng tháng")
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val date: String, // YYYY-MM-DD
    val accountId: String,
    val targetAccountId: String? = null, // Cho giao dịch chuyển tiền (FR-005)
    val category: String, // Default categories or custom
    val note: String = "",
    val recurrence: RecurrenceRule = RecurrenceRule.NONE,
    val isPending: Boolean = false, // Giao dịch dự kiến (FR-006)
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            "Ăn uống",
            "Nhà ở",
            "Đi lại",
            "Học tập",
            "Giải trí",
            "Mua sắm",
            "Y tế",
            "Liên lạc",
            "Gia đình",
            "Trả nợ",
            "Khác"
        )

        val DEFAULT_INCOME_CATEGORIES = listOf(
            "Trợ cấp gia đình",
            "Lương làm thêm",
            "Học bổng",
            "Thưởng",
            "Thu nhập khác"
        )
    }
}
