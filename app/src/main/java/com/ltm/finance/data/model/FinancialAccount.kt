package com.ltm.finance.data.model

import java.math.BigDecimal

enum class AccountType(val displayName: String) {
    CASH("Tiền mặt"),
    BANK("Ngân hàng"),
    E_WALLET("Ví điện tử"),
    SAVINGS("Tiết kiệm có kỳ hạn"),
    OTHER("Tài sản khác")
}

data class FinancialAccount(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: BigDecimal,
    val provider: String = "", // e.g., "Vietcombank", "MoMo", "Tiền mặt"
    val isLiquid: Boolean = true, // False if locked in term deposit
    val interestRateYearly: Double = 0.0,
    val maturityDate: String? = null, // YYYY-MM-DD (cho sổ tiết kiệm)
    val termMonths: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
