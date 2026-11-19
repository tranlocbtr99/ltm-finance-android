package com.ltm.finance.data.model

import java.math.BigDecimal

enum class RecommendationStatus(val displayName: String) {
    CREATED("Mới tạo"),
    VIEWED("Đã xem"),
    ACCEPTED("Đã chấp nhận"),
    REJECTED("Đã từ chối"),
    MONITORING("Đang theo dõi"),
    PARTIALLY_IMPLEMENTED("Thực hiện một phần"),
    IMPLEMENTED("Đã thực hiện"),
    NOT_IMPLEMENTED("Chưa thực hiện"),
    INSUFFICIENT_DATA("Chưa đủ dữ liệu"),
    EXPIRED("Hết hạn")
}

enum class VerificationResult(val displayName: String) {
    EVIDENCE_FOUND("Có bằng chứng phù hợp"),
    PARTIALLY_FOUND("Thực hiện một phần"),
    NO_EVIDENCE_YET("Chưa có bằng chứng"),
    CONTRADICTORY("Dữ liệu mâu thuẫn"),
    INSUFFICIENT_DATA("Không đủ dữ liệu")
}

enum class AdviceFeedback(val displayName: String) {
    HELPFUL("Hữu ích"),
    IRRELEVANT("Không phù hợp hoàn cảnh"),
    TOO_STRICT("Quá nghiêm khắc"),
    HARD_TO_EXECUTE("Khó thực hiện"),
    LACK_INFO("Không đủ thông tin")
}

data class TransactionDraft(
    val type: TransactionType,
    val amount: BigDecimal,
    val category: String,
    val accountId: String,
    val targetAccountId: String? = null,
    val note: String
)

data class AiRecommendation(
    val id: String,
    val category: String,
    val title: String,
    val content: String,
    val reason: String,
    val previousBudget: BigDecimal? = null,
    val proposedBudget: BigDecimal? = null,
    val status: RecommendationStatus = RecommendationStatus.CREATED,
    val verification: VerificationResult = VerificationResult.NO_EVIDENCE_YET,
    val feedback: AdviceFeedback? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val doubleCheckPrompt: String? = null
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val linkedRecommendation: AiRecommendation? = null,
    val draftTransaction: TransactionDraft? = null,
    val actionSuggestion: String? = null
)
