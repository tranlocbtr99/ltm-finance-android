package com.ltm.finance.domain

import com.ltm.finance.data.model.*
import java.math.BigDecimal
import java.math.RoundingMode

object SimulationEngine {

    fun simulatePurchase(
        itemName: String,
        price: BigDecimal,
        currentLiquidCash: BigDecimal,
        monthlySurplus: BigDecimal, // Thu nhập - Chi phí trung bình hàng tháng
        installmentMonths: Int = 6,
        installmentFeePercent: Double = 3.0 // Phí chuyển đổi hoặc lãi suất trả góp
    ): PurchaseSimulationResult {
        val options = mutableListOf<PurchaseOption>()

        // 1. Không mua
        options.add(
            PurchaseOption(
                optionName = "Không mua",
                immediatePayment = BigDecimal.ZERO,
                monthlyPayment = BigDecimal.ZERO,
                totalPayment = BigDecimal.ZERO,
                remainingBalanceAfterPayment = currentLiquidCash,
                isAffordable = true,
                riskSummary = "Bảo toàn số dư sẵn có ${FinanceEngine.formatVnd(currentLiquidCash)}. Không phát sinh nghĩa vụ nợ."
            )
        )

        // 2. Mua ngay (Trả thẳng)
        val balanceAfterBuyNow = currentLiquidCash.subtract(price)
        val isBuyNowAffordable = balanceAfterBuyNow >= BigDecimal.ZERO
        val buyNowRisk = if (balanceAfterBuyNow < BigDecimal.ZERO) {
            "NGUY CƠ CAO: Thiếu ${FinanceEngine.formatVnd(balanceAfterBuyNow.abs())} để chi trả. Số dư về âm ngay lập tức!"
        } else if (balanceAfterBuyNow < BigDecimal("1000000")) {
            "CẢNH BÁO: Số dư còn lại quá mỏng (${FinanceEngine.formatVnd(balanceAfterBuyNow)}), rất dễ mất khả năng chi trả khi có sự cố."
        } else {
            "Khả thi. Bạn vẫn giữ được ${FinanceEngine.formatVnd(balanceAfterBuyNow)} tiền mặt dự phòng."
        }
        options.add(
            PurchaseOption(
                optionName = "Mua ngay (Trả thẳng)",
                immediatePayment = price,
                monthlyPayment = BigDecimal.ZERO,
                totalPayment = price,
                remainingBalanceAfterPayment = balanceAfterBuyNow,
                isAffordable = isBuyNowAffordable,
                riskSummary = buyNowRisk
            )
        )

        // 3. Mua trả góp N tháng
        val validMonths = installmentMonths.coerceAtLeast(1)
        val feeRate = BigDecimal(1.0 + installmentFeePercent / 100.0)
        val totalInstallment = price.multiply(feeRate).setScale(0, RoundingMode.HALF_UP)
        val monthlyPayment = totalInstallment.divide(BigDecimal(validMonths), 0, RoundingMode.HALF_UP)
        val balanceAfterFirstMonth = currentLiquidCash.subtract(monthlyPayment)
        val isInstallmentAffordable = monthlySurplus > monthlyPayment && balanceAfterFirstMonth > BigDecimal.ZERO
        val installmentRisk = if (monthlyPayment > monthlySurplus) {
            "RỦI RO: Tiền trả góp (${FinanceEngine.formatVnd(monthlyPayment)}/tháng) vượt quá mức thặng dư chi tiêu (${FinanceEngine.formatVnd(monthlySurplus)}/tháng)!"
        } else {
            "Áp lực vừa phải: Mỗi tháng trích ${FinanceEngine.formatVnd(monthlyPayment)} trong $validMonths tháng. Tổng tiền trả: ${FinanceEngine.formatVnd(totalInstallment)}."
        }
        options.add(
            PurchaseOption(
                optionName = "Trả góp ($validMonths tháng)",
                immediatePayment = monthlyPayment,
                monthlyPayment = monthlyPayment,
                totalPayment = totalInstallment,
                remainingBalanceAfterPayment = balanceAfterFirstMonth,
                isAffordable = isInstallmentAffordable,
                riskSummary = installmentRisk
            )
        )

        // 4. Tiết kiệm trước rồi mua (Save first)
        val monthsNeeded = if (monthlySurplus > BigDecimal.ZERO) {
            price.divide(monthlySurplus, 0, RoundingMode.CEILING).toInt().coerceAtLeast(1)
        } else 12
        options.add(
            PurchaseOption(
                optionName = "Tích lũy trước rồi mua",
                immediatePayment = BigDecimal.ZERO,
                monthlyPayment = if (monthlySurplus > BigDecimal.ZERO) monthlySurplus else BigDecimal("500000"),
                totalPayment = price,
                remainingBalanceAfterPayment = currentLiquidCash,
                isAffordable = true,
                riskSummary = "Khuyên dùng: Để dành khoảng ${FinanceEngine.formatVnd(if (monthlySurplus > BigDecimal.ZERO) monthlySurplus else BigDecimal("500000"))}/tháng. Sau $monthsNeeded tháng mua trọn vẹn, không chịu phí vay."
            )
        )

        val bestRecommendation = when {
            isBuyNowAffordable && balanceAfterBuyNow > BigDecimal("2000000") ->
                "Tài chính của bạn đủ mạnh để mua ngay mà không lo thiếu hụt dòng tiền."
            isInstallmentAffordable ->
                "Nếu thiết bị phục vụ học tập cấp thiết, phương án Trả góp $installmentMonths tháng là phù hợp nhất."
            else ->
                "Khuyến nghị: Nên chọn 'Tích lũy trước rồi mua' để tránh nguy cơ cạn kiệt tiền sinh hoạt."
        }

        return PurchaseSimulationResult(
            itemName = itemName,
            price = price,
            options = options,
            recommendation = bestRecommendation
        )
    }
}

