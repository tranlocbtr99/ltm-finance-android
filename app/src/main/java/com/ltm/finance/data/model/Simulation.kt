package com.ltm.finance.data.model

import java.math.BigDecimal

enum class SimulationScenarioType(val title: String, val description: String) {
    PURCHASE("Mô phỏng mua sắm", "So sánh mua ngay, trả góp hoặc tích lũy trước"),
    INCOME_DROP("Giảm thu nhập / Mất việc làm thêm", "Đánh giá khả năng duy trì khi thu nhập giảm"),
    TUITION_FEE("Phát sinh học phí đột xuất", "Kế hoạch chi trả học phí kỳ mới"),
    RELOCATION("Chuyển trọ / Chi phí sinh hoạt tăng", "Tác động tiền cọc và tiền nhà mới")
}

data class PurchaseOption(
    val optionName: String,
    val immediatePayment: BigDecimal,
    val monthlyPayment: BigDecimal,
    val totalPayment: BigDecimal,
    val remainingBalanceAfterPayment: BigDecimal,
    val isAffordable: Boolean,
    val riskSummary: String
)

data class PurchaseSimulationResult(
    val itemName: String,
    val price: BigDecimal,
    val options: List<PurchaseOption>,
    val recommendation: String
)

