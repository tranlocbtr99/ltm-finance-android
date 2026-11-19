package com.ltm.finance.data.model

import java.math.BigDecimal

enum class DebtType(val displayName: String) {
    CREDIT_CARD("Thẻ tín dụng"),
    PERSONAL_LOAN("Vay cá nhân"),
    INSTALLMENT("Trả góp mua hàng"),
    STUDENT_LOAN("Vay học phí sinh viên"),
    OTHER("Khoản vay khác")
}

enum class RepaymentStatus(val displayName: String) {
    UPCOMING("Sắp đến hạn"),
    DUE_TODAY("Đến hạn hôm nay"),
    PAID("Đã thanh toán"),
    PARTIALLY_PAID("Thanh toán một phần"),
    OVERDUE("Quá hạn")
}

data class RepaymentSchedule(
    val id: String,
    val debtId: String,
    val dueDate: String, // YYYY-MM-DD
    val principal: BigDecimal,
    val interest: BigDecimal,
    val fee: BigDecimal = BigDecimal.ZERO,
    val totalDue: BigDecimal = principal.add(interest).add(fee),
    val paidAmount: BigDecimal = BigDecimal.ZERO,
    val status: RepaymentStatus = RepaymentStatus.UPCOMING
)

data class Debt(
    val id: String,
    val name: String,
    val type: DebtType,
    val initialAmount: BigDecimal,
    val currentBalance: BigDecimal,
    val interestRateYearly: Double,
    val creditLimit: BigDecimal? = null, // Cho thẻ tín dụng (FR-013)
    val statementDay: Int? = null,
    val paymentDueDay: Int,
    val monthlyPaymentEstimate: BigDecimal,
    val startDate: String,
    val endDate: String,
    val schedules: List<RepaymentSchedule> = emptyList()
)
