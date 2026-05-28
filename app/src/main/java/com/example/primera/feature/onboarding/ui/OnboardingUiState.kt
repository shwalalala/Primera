package com.example.primera.feature.onboarding.ui

import com.example.primera.feature.onboarding.domain.model.PregnancyHistory
import java.util.Date

enum class OnboardingStep {
    BIRTHDAY, WEIGHT, HEIGHT, LMP, EDD, FIRST_PREGNANCY, PREGNANCY_HISTORY, PREPARING
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.BIRTHDAY,
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
    val selectedPregnancyIndex: Int = 0,
    val pregnancyHistories: List<PregnancyHistory> = listOf(PregnancyHistory(pregnancyNumber = 1)),
    val showConfirmationDialog: Boolean = false,
    
    val preparationProgress: Float = 0f,
    val isCompleted: Boolean = false
) {
    val currentPregnancy: PregnancyHistory
        get() = pregnancyHistories.getOrElse(selectedPregnancyIndex) { PregnancyHistory(pregnancyNumber = selectedPregnancyIndex + 1) }

    fun isPregnancyComplete(index: Int): Boolean {
        val history = pregnancyHistories.getOrNull(index) ?: return false
        return history.deliveryDate != null && 
               history.deliveryType.isNotBlank() && 
               history.childrenDelivered.isNotBlank()
    }

    fun canProceedToStep(step: OnboardingStep): Boolean {
        return when (step) {
            OnboardingStep.PREPARING -> {
                if (isFirstPregnancy == true) true
                else pregnancyHistories.all { isPregnancyComplete(pregnancyHistories.indexOf(it)) }
            }
            else -> true
        }
    }
}
