package cit.edu.primera.feature.onboarding.ui

import cit.edu.primera.feature.onboarding.domain.PregnancyHistory
import java.util.Date

enum class OnboardingStep {
    BIRTHDAY, WEIGHT, HEIGHT, CYCLE_REGULARITY, CYCLE_VARIANCE, HAD_ULTRASOUND, LMP, ULTRASOUND, EDD, FIRST_PREGNANCY, PREGNANCY_HISTORY, PREPARING
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.BIRTHDAY,
    val birthday: Date? = null,
    val weightKg: Int = 0,
    val heightCm: Int = 0,
    val isCycleRegular: Boolean? = null,
    val shortestCycleDays: Int = 28,
    val longestCycleDays: Int = 28,
    val hasHadUltrasound: Boolean? = null,
    val positiveTestDate: Date? = null,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean? = null,
    
    // Ultrasound Data
    val isUsingUltrasound: Boolean = false,
    val scanDate: Date? = null,
    val scanWeeks: Int = 0,
    val scanDays: Int = 0,
    val isRevisedEdd: Boolean = false,
    
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
