package com.example.primera.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.data.repository.PreferenceRepository
import com.example.primera.feature.onboarding.data.repository.OnboardingRepository
import com.example.primera.feature.onboarding.domain.model.OnboardingProfile
import com.example.primera.feature.onboarding.domain.model.PregnancyHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class OnboardingViewModel(
    private val repository: OnboardingRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = _state.asStateFlow()

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

    fun onSelectPregnancy(index: Int) {
        _state.update { it.copy(selectedPregnancyIndex = index) }
    }

    fun addPregnancy() {
        _state.update { state ->
            val nextNum = state.pregnancyHistories.size + 1
            val newHistories = state.pregnancyHistories + PregnancyHistory(pregnancyNumber = nextNum)
            state.copy(
                pregnancyHistories = newHistories,
                selectedPregnancyIndex = newHistories.size - 1
            )
        }
    }

    fun onHistoryDeliveryDateChange(date: Date) {
        updateCurrentPregnancy { it.copy(deliveryDate = date) }
    }

    fun onDeliveryTypeChange(type: String) {
        updateCurrentPregnancy { it.copy(deliveryType = type) }
    }

    fun onChildrenDeliveredChange(count: String) {
        updateCurrentPregnancy { it.copy(childrenDelivered = count) }
    }
    
    fun toggleComplication(complication: String) {
        updateCurrentPregnancy { curr ->
            val newList = if (curr.complications.contains(complication)) {
                curr.complications - complication
            } else {
                curr.complications + complication
            }
            curr.copy(complications = newList)
        }
    }

    fun removePregnancy(index: Int) {
        _state.update { state ->
            if (state.pregnancyHistories.size <= 1) return@update state // Keep at least one
            val newList = state.pregnancyHistories.toMutableList()
            newList.removeAt(index)
            
            // Adjust pregnancy numbers
            val adjustedList = newList.mapIndexed { i, history ->
                history.copy(pregnancyNumber = i + 1)
            }
            
            state.copy(
                pregnancyHistories = adjustedList,
                selectedPregnancyIndex = if (state.selectedPregnancyIndex >= adjustedList.size) adjustedList.size - 1 else state.selectedPregnancyIndex
            )
        }
    }

    private fun updateCurrentPregnancy(update: (PregnancyHistory) -> PregnancyHistory) {
        _state.update { state ->
            val updatedHistories = state.pregnancyHistories.toMutableList()
            val currentIndex = state.selectedPregnancyIndex
            if (currentIndex in updatedHistories.indices) {
                updatedHistories[currentIndex] = update(updatedHistories[currentIndex])
            }
            state.copy(pregnancyHistories = updatedHistories)
        }
    }

    fun nextStep() {
        val currentState = _state.value
        
        // If moving to PREPARING, show confirmation dialog instead
        val isMovingToPreparing = (currentState.currentStep == OnboardingStep.FIRST_PREGNANCY && currentState.isFirstPregnancy == true) ||
                                 (currentState.currentStep == OnboardingStep.PREGNANCY_HISTORY)
        
        if (isMovingToPreparing) {
            _state.update { it.copy(showConfirmationDialog = true) }
            return
        }

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
            OnboardingStep.BIRTHDAY -> OnboardingStep.BIRTHDAY
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

    fun dismissConfirmation() {
        _state.update { it.copy(showConfirmationDialog = false) }
    }

    fun confirmAndSave() {
        _state.update { it.copy(showConfirmationDialog = false, currentStep = OnboardingStep.PREPARING) }
        saveAndFinish()
    }

    private fun saveAndFinish() {
        viewModelScope.launch {
            val s = _state.value
            
            // BUG-003 Fix: Use a sensible default (25 years ago) if birthday is missing
            val defaultBirthday = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }.time
            
            val profile = OnboardingProfile(
                firstName = s.firstName,
                lastName = s.lastName,
                middleName = s.middleName,
                birthday = s.birthday ?: defaultBirthday,
                weightKg = s.weightKg,
                heightCm = s.heightCm,
                lmpDate = s.lmpDate,
                eddDate = s.eddDate,
                isFirstPregnancy = s.isFirstPregnancy ?: false,
                pregnancyHistories = if (s.isFirstPregnancy == true) emptyList() else s.pregnancyHistories
            )
            
            repository.saveProfile(profile)
            preferenceRepository.setOnboardingCompleted()
            
            for (i in 0..100 step 10) {
                _state.update { it.copy(preparationProgress = i / 100f) }
                delay(100)
            }
            _state.update { it.copy(isCompleted = true) }
        }
    }
}
