package com.example.primera.frontend.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class OnboardingViewModel : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onBirthdayChange(date: Date) = _state.update { it.copy(birthday = date) }
    fun onWeightChange(weight: Int) = _state.update { it.copy(weightKg = weight) }
    fun onHeightChange(height: Int) = _state.update { it.copy(heightCm = height) }
    fun onLmpDateChange(date: Date) = _state.update { it.copy(lmpDate = date) }
    fun onEddDateChange(date: Date) = _state.update { it.copy(eddDate = date) }
    
    fun onIsFirstPregnancyChange(isFirst: Boolean) {
        _state.update { it.copy(isFirstPregnancy = isFirst) }
    }

    fun onPregnancyNumberChange(num: Int) = _state.update { it.copy(pregnancyNumber = num) }
    fun onHistoryDeliveryDateChange(date: Date) = _state.update { it.copy(historyDeliveryDate = date) }
    fun onDeliveryTypeChange(type: String) = _state.update { it.copy(deliveryType = type) }
    fun onBirthOutcomeChange(outcome: String) = _state.update { it.copy(birthOutcome = outcome) }
    fun onChildrenDeliveredChange(count: String) = _state.update { it.copy(childrenDelivered = count) }
    
    fun toggleComplication(complication: String) {
        _state.update { curr ->
            val newList = if (curr.complications.contains(complication)) {
                curr.complications - complication
            } else {
                curr.complications + complication
            }
            curr.copy(complications = newList)
        }
    }

    fun nextStep() {
        val currentState = _state.value
        val next = when (currentState.currentStep) {
            OnboardingStep.BIRTHDAY -> OnboardingStep.WEIGHT
            OnboardingStep.WEIGHT -> OnboardingStep.HEIGHT
            OnboardingStep.HEIGHT -> OnboardingStep.LMP
            OnboardingStep.LMP -> OnboardingStep.EDD
            OnboardingStep.EDD -> OnboardingStep.FIRST_PREGNANCY
            OnboardingStep.FIRST_PREGNANCY -> {
                if (currentState.isFirstPregnancy == true) OnboardingStep.PREPARING
                else OnboardingStep.PREGNANCY_HISTORY
            }
            OnboardingStep.PREGNANCY_HISTORY -> OnboardingStep.PREPARING
            OnboardingStep.PREPARING -> OnboardingStep.PREPARING // Finalizing
        }
        
        _state.update { it.copy(currentStep = next) }
        
        if (next == OnboardingStep.PREPARING) {
            startPreparation()
        }
    }

    fun previousStep() {
        val currentState = _state.value
        val prev = when (currentState.currentStep) {
            OnboardingStep.BIRTHDAY -> OnboardingStep.WEIGHT
            OnboardingStep.WEIGHT -> OnboardingStep.BIRTHDAY
            OnboardingStep.HEIGHT -> OnboardingStep.WEIGHT
            OnboardingStep.LMP -> OnboardingStep.HEIGHT
            OnboardingStep.EDD -> OnboardingStep.LMP
            OnboardingStep.FIRST_PREGNANCY -> OnboardingStep.EDD
            OnboardingStep.PREGNANCY_HISTORY -> OnboardingStep.FIRST_PREGNANCY
            OnboardingStep.PREPARING -> OnboardingStep.FIRST_PREGNANCY // Or History
        }
        _state.update { it.copy(currentStep = prev) }
    }

    private fun startPreparation() {
        viewModelScope.launch {
            for (i in 0..100 step 5) {
                _state.update { it.copy(preparationProgress = i / 100f) }
                delay(100)
            }
            _state.update { it.copy(isCompleted = true) }
        }
    }
}
