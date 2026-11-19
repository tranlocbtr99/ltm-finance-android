package com.ltm.finance.data.model

data class ResearchEvent(
    val eventId: String,
    val participantId: String,
    val eventType: String, // "BUDGET_CREATED", "ADVICE_VIEWED", "ADVICE_ACCEPTED", "TRANSACTION_LOGGED", "SIMULATION_RUN"
    val timestamp: Long = System.currentTimeMillis(),
    val metadataJson: String = "{}"
)

data class ResearchSummaryMetrics(
    val participantId: String,
    val totalTransactionsLogged: Int,
    val budgetComplianceRate: Double, // 0.0 to 100.0%
    val aiEngagementCount: Int,
    val adviceAcceptedRate: Double,
    val adviceImplementedRate: Double,
    val daysActive: Int
)

