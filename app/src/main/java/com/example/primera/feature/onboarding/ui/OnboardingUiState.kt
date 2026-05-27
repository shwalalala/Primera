package com.example.primera.feature.onboarding.ui

import java.util.Date

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.NAME,
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val birthday: Date? = null,
    val weightKg: Int = 0,
    val heightCm: Int = 0,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean? = null,
    
    // Pregnancy History (if not first)
    val pregnancyNumber: Int = 1,
    val historyDeliveryDate: Date? = null,
    val deliveryType: String = "", // Vaginal, C-section
    val childrenDelivered: String = "", // Single, Twins, Multiple
    val complications: List<String> = emptyList(),
    
    val preparationProgress: Float = 0f,
    val isCompleted: Boolean = false
)

enum class OnboardingStep {
    NAME,
    BIRTHDAY,
    WEIGHT,
    HEIGHT,
    LMP,
    EDD,
    FIRST_PREGNANCY,
    PREGNANCY_HISTORY,
    PREPARING
}
