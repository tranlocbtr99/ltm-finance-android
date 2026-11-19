package com.ltm.finance.domain

import com.ltm.finance.data.model.*
import java.util.UUID

object ResearchEngine {

    fun createEvent(
        participantId: String,
        eventType: String,
        metadata: Map<String, Any> = emptyMap()
    ): ResearchEvent {
        val jsonMeta = metadata.entries.joinToString(prefix = "{", postfix = "}") {
            "\"${it.key}\": \"${it.value}\""
        }
        return ResearchEvent(
            eventId = "EVT-${UUID.randomUUID().toString().take(8)}",
            participantId = participantId,
            eventType = eventType,
            timestamp = System.currentTimeMillis(),
            metadataJson = jsonMeta
        )
    }

    fun computeMetrics(
        participantId: String,
        events: List<ResearchEvent>,
        recommendations: List<AiRecommendation>,
        budgets: List<Budget>,
        transactionsCount: Int
    ): ResearchSummaryMetrics {
        val totalRecs = recommendations.size
        val acceptedRecs = recommendations.count { it.status == RecommendationStatus.ACCEPTED || it.status == RecommendationStatus.IMPLEMENTED }
        val implementedRecs = recommendations.count { it.status == RecommendationStatus.IMPLEMENTED }

        val complianceCount = budgets.count { !it.isOverBudget }
        val complianceRate = if (budgets.isNotEmpty()) {
            (complianceCount.toDouble() / budgets.size.toDouble()) * 100.0
        } else 100.0

        val acceptanceRate = if (totalRecs > 0) (acceptedRecs.toDouble() / totalRecs) * 100.0 else 0.0
        val implementationRate = if (acceptedRecs > 0) (implementedRecs.toDouble() / acceptedRecs) * 100.0 else 0.0

        return ResearchSummaryMetrics(
            participantId = participantId,
            totalTransactionsLogged = transactionsCount,
            budgetComplianceRate = complianceRate,
            aiEngagementCount = events.size,
            adviceAcceptedRate = acceptanceRate,
            adviceImplementedRate = implementationRate,
            daysActive = 21 // Mẫu nghiên cứu 3 tuần
        )
    }

    fun exportToCsv(events: List<ResearchEvent>): String {
        val sb = StringBuilder()
        sb.append("event_id,participant_id,event_type,timestamp,metadata\n")
        events.forEach { e ->
            sb.append("\"${e.eventId}\",\"${e.participantId}\",\"${e.eventType}\",${e.timestamp},\"${e.metadataJson.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }
}

