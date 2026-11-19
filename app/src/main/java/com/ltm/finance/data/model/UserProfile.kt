package com.ltm.finance.data.model

import java.math.BigDecimal

data class UserProfile(
    val id: String = "USR-2026-001",
    val email: String = "sinhvien@university.edu.vn",
    val displayName: String = "Nguyễn Văn Sinh Viên",
    val university: String = "Đại học Quốc Gia",
    val academicYear: Int = 3,
    val monthlyAllowance: BigDecimal = BigDecimal("3500000"), // Trợ cấp gia đình
    val partTimeIncome: BigDecimal = BigDecimal("2000000"),   // Làm thêm
    val expectedLivingCost: BigDecimal = BigDecimal("4500000"),
    val financialGoal: String = "Tiết kiệm 5 triệu mua laptop & không nợ quá hạn",
    val incomeDayOfMonth: Int = 5,
    val isGoogleLinked: Boolean = false,
    val googleEmail: String? = null,
    val photoUrl: String? = null,
    val consentTerms: Boolean = true,
    val consentAiProcessing: Boolean = true,
    val consentResearchStudy: Boolean = true,
    val researchParticipantId: String = "PARTICIPANT-9824"
)
