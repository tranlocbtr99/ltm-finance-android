package com.ltm.finance.data.model

import java.math.BigDecimal

data class ForecastDay(
    val date: String, // YYYY-MM-DD
    val projectedBalance: BigDecimal,
    val inflow: BigDecimal,
    val outflow: BigDecimal,
    val notes: List<String> = emptyList(),
    val isShortage: Boolean = false
)

data class ShortageAlert(
    val date: String,
    val estimatedShortage: BigDecimal,
    val reason: String,
    val severity: String, // "HIGH", "MEDIUM"
    val solutions: List<String>
)

data class CashflowForecast(
    val daysCount: Int, // 30, 90, 365
    val startBalance: BigDecimal,
    val endBalance: BigDecimal,
    val minBalance: BigDecimal,
    val totalInflow: BigDecimal,
    val totalOutflow: BigDecimal,
    val dailyForecasts: List<ForecastDay>,
    val alerts: List<ShortageAlert>
)

