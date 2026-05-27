package com.example.primera.feature.onboarding.ui

import java.util.Date

data class OnboardingUiState(
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val birthday: Date? = null,
    val weightKg: Int = 50,
    val heightCm: Int = 160,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean? = null,
    val pregnancyNumber: Int = 1,
    val historyDeliveryDate: Date? = null,
    val deliveryType: String = "",
    val birthOutcome: String = "",
    val childrenDelivered: String = "",
    val complications: List<String> = emptyList(),
    
    val currentStep: OnboardingStep = OnboardingStep.NAME,
    val preparationProgress: Float = 0f,
    val isCompleted: Boolean = false,
    
    // Error handling
    val errorMessage: String? = null,
    val errorStep: OnboardingStep? = null
)

enum class OnboardingStep {
    NAME, BIRTHDAY, WEIGHT, HEIGHT, LMP, EDD, FIRST_PREGNANCY, PREGNANCY_HISTORY, PREPARING
}
