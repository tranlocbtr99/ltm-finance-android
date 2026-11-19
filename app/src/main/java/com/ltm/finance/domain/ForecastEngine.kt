package com.ltm.finance.domain

import com.ltm.finance.data.model.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ForecastEngine {

    fun generateForecast(
        startLiquidBalance: BigDecimal,
        userProfile: UserProfile,
        recurringTransactions: List<Transaction>,
        debts: List<Debt>,
        daysAhead: Int = 30
    ): CashflowForecast {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dailyList = mutableListOf<ForecastDay>()
        val alerts = mutableListOf<ShortageAlert>()

        var currentBalance = startLiquidBalance
        var minBalance = startLiquidBalance
        var totalInflow = BigDecimal.ZERO
        var totalOutflow = BigDecimal.ZERO

        for (i in 0 until daysAhead) {
            val date = today.plusDays(i.toLong())
            val dateStr = date.format(formatter)
            val dayOfMonth = date.dayOfMonth

            var dayInflow = BigDecimal.ZERO
            var dayOutflow = BigDecimal.ZERO
            val notes = mutableListOf<String>()

            // 1. Thu nhập định kỳ theo chu kỳ ngày trong tháng
            if (dayOfMonth == userProfile.incomeDayOfMonth) {
                dayInflow = dayInflow.add(userProfile.monthlyAllowance).add(userProfile.partTimeIncome)
                notes.add("Trợ cấp gia đình & Lương làm thêm: +${FinanceEngine.formatVnd(dayInflow)}")
            }

            // 2. Chi phí định kỳ từ recurring transactions
            recurringTransactions.forEach { tx ->
                val shouldApply = when (tx.recurrence) {
                    RecurrenceRule.DAILY -> true
                    RecurrenceRule.WEEKLY -> date.dayOfWeek == today.dayOfWeek
                    RecurrenceRule.MONTHLY -> dayOfMonth == 1
                    else -> false
                }
                if (shouldApply) {
                    if (tx.type == TransactionType.EXPENSE) {
                        dayOutflow = dayOutflow.add(tx.amount)
                    } else if (tx.type == TransactionType.INCOME) {
                        dayInflow = dayInflow.add(tx.amount)
                    }
                }
            }

            // 3. Lịch trả nợ định kỳ (FR-012)
            debts.forEach { debt ->
                debt.schedules.filter { it.dueDate == dateStr && it.status != RepaymentStatus.PAID }.forEach { schedule ->
                    dayOutflow = dayOutflow.add(schedule.totalDue)
                    notes.add("Đến hạn trả nợ '${debt.name}': -${FinanceEngine.formatVnd(schedule.totalDue)}")
                }
            }

            // Cập nhật số dư dòng tiền theo công thức FR-019
            currentBalance = currentBalance.add(dayInflow).subtract(dayOutflow)
            if (currentBalance < minBalance) {
                minBalance = currentBalance
            }
            totalInflow = totalInflow.add(dayInflow)
            totalOutflow = totalOutflow.add(dayOutflow)

            val isShortage = currentBalance < BigDecimal.ZERO
            if (isShortage) {
                notes.add("CẢNH BÁO: Số dư dự kiến âm!")
                if (alerts.none { it.date == dateStr }) {
                    alerts.add(
                        ShortageAlert(
                            date = dateStr,
                            estimatedShortage = currentBalance.abs(),
                            reason = "Khoản chi vượt quá dòng tiền sẵn có vào ngày $dateStr",
                            severity = "HIGH",
                            solutions = listOf(
                                "Tạm hoãn các khoản chi tiêu không thiết yếu",
                                "Đàm phán lùi hạn thanh toán khoản vay",
                                "Rút tiền từ quỹ tiết kiệm dự phòng"
                            )
                        )
                    )
                }
            }

            dailyList.add(
                ForecastDay(
                    date = dateStr,
                    projectedBalance = currentBalance,
                    inflow = dayInflow,
                    outflow = dayOutflow,
                    notes = notes,
                    isShortage = isShortage
                )
            )
        }

        return CashflowForecast(
            daysCount = daysAhead,
            startBalance = startLiquidBalance,
            endBalance = currentBalance,
            minBalance = minBalance,
            totalInflow = totalInflow,
            totalOutflow = totalOutflow,
            dailyForecasts = dailyList,
            alerts = alerts
        )
    }
}

