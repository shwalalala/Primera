package cit.edu.primera.feature.onboarding.domain

import java.util.Date

data class PregnancyHistory(
    val pregnancyNumber: Int = 1,
    val deliveryDate: Date? = null,
    val deliveryType: String = "",
    val birthOutcome: String = "",
    val childrenDelivered: String = "",
    val complications: List<String> = emptyList()
)

data class OnboardingProfile(
    val email: String? = null,
    val birthday: Date? = null,
    val weightKg: Int = 50,
    val heightCm: Int = 160,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean? = null,
    val isCycleRegular: Boolean? = null,
    val shortestCycleDays: Int? = null,
    val longestCycleDays: Int? = null,
    val hasHadUltrasound: Boolean? = null,
    val positiveTestDate: Date? = null,
    val pregnancyHistories: List<PregnancyHistory> = emptyList(),
    
    // Ultrasound Data
    val scanDate: Date? = null,
    val scanWeeks: Int? = null,
    val scanDays: Int? = null,
    
    // Emergency Contact
    val iceName: String? = null,
    val iceRelationship: String? = null,
    val icePrimaryPhone: String? = null,
    val iceSecondaryPhone: String? = null
)
