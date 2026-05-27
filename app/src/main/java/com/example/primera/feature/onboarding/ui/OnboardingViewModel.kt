package com.example.primera.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.feature.onboarding.data.repository.OnboardingRepository
import com.example.primera.feature.onboarding.domain.model.OnboardingProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class OnboardingViewModel(
    private val repository: OnboardingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onFirstNameChange(name: String) = _state.update { it.copy(firstName = name) }
    fun onLastNameChange(name: String) = _state.update { it.copy(lastName = name) }
    fun onMiddleNameChange(name: String) = _state.update { it.copy(middleName = name) }
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
            OnboardingStep.NAME -> OnboardingStep.BIRTHDAY
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
            OnboardingStep.PREPARING -> OnboardingStep.PREPARING
        }
        
        _state.update { it.copy(currentStep = next) }
        
        if (next == OnboardingStep.PREPARING) {
            saveAndFinish()
        }
    }

    fun previousStep() {
        val currentState = _state.value
        val prev = when (currentState.currentStep) {
            OnboardingStep.NAME -> OnboardingStep.NAME
            OnboardingStep.BIRTHDAY -> OnboardingStep.NAME
            OnboardingStep.WEIGHT -> OnboardingStep.BIRTHDAY
            OnboardingStep.HEIGHT -> OnboardingStep.WEIGHT
            OnboardingStep.LMP -> OnboardingStep.HEIGHT
            OnboardingStep.EDD -> OnboardingStep.LMP
            OnboardingStep.FIRST_PREGNANCY -> OnboardingStep.EDD
            OnboardingStep.PREGNANCY_HISTORY -> OnboardingStep.FIRST_PREGNANCY
            OnboardingStep.PREPARING -> OnboardingStep.FIRST_PREGNANCY
        }
        _state.update { it.copy(currentStep = prev) }
    }

    private fun saveAndFinish() {
        viewModelScope.launch {
            val s = _state.value
            val profile = OnboardingProfile(
                firstName = s.firstName,
                lastName = s.lastName,
                middleName = s.middleName,
                birthday = s.birthday,
                weightKg = s.weightKg,
                heightCm = s.heightCm,
                lmpDate = s.lmpDate,
                eddDate = s.eddDate,
                isFirstPregnancy = s.isFirstPregnancy,
                pregnancyNumber = s.pregnancyNumber,
                historyDeliveryDate = s.historyDeliveryDate,
                deliveryType = s.deliveryType,
                birthOutcome = s.birthOutcome,
                childrenDelivered = s.childrenDelivered,
                complications = s.complications
            )
            
            repository.saveProfile(profile)
            
            for (i in 0..100 step 10) {
                _state.update { it.copy(preparationProgress = i / 100f) }
                delay(100)
            }
            _state.update { it.copy(isCompleted = true) }
        }
    }
}
