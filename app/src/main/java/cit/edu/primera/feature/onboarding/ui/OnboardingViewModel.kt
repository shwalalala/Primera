package cit.edu.primera.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.feature.onboarding.data.OnboardingRepository
import cit.edu.primera.feature.onboarding.domain.OnboardingProfile
import cit.edu.primera.feature.onboarding.domain.PregnancyHistory
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

    init {
        loadSavedStep()
    }

    private fun loadSavedStep() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val savedStepName = preferenceRepository.getOnboardingLastStep(userId)
        if (savedStepName != null) {
            try {
                val step = OnboardingStep.valueOf(savedStepName)
                // Don't resume to PREPARING, that should be final
                if (step != OnboardingStep.PREPARING) {
                    _state.update { it.copy(currentStep = step) }
                }
            } catch (e: Exception) {
                // Ignore invalid step names
            }
        }
    }

    private fun saveCurrentStep(step: OnboardingStep) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        preferenceRepository.setOnboardingLastStep(userId, step.name)
    }

    fun onBirthdayChange(date: Date) = _state.update { it.copy(birthday = date) }
    fun onWeightChange(weight: Int) = _state.update { it.copy(weightKg = weight) }
    fun onHeightChange(height: Int) = _state.update { it.copy(heightCm = height) }
    fun onIsCycleRegularChange(isRegular: Boolean) = _state.update { it.copy(isCycleRegular = isRegular) }
    fun onShortestCycleChange(days: Int) = _state.update { it.copy(shortestCycleDays = days) }
    fun onLongestCycleChange(days: Int) = _state.update { it.copy(longestCycleDays = days) }
    fun onHasHadUltrasoundChange(hasHad: Boolean) = _state.update { it.copy(hasHadUltrasound = hasHad) }
    fun onPositiveTestDateChange(date: Date) {
        _state.update { state ->
            // Tentative EDD = Positive Test Date + 36 weeks (assuming 4 weeks gestation at test)
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.WEEK_OF_YEAR, 36)
            }
            state.copy(positiveTestDate = date, eddDate = state.eddDate ?: calendar.time)
        }
    }
    fun onLmpDateChange(date: Date) {
        _state.update { state ->
            // If EDD is not set, we can estimate it (LMP + 280 days)
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, 280)
            }
            state.copy(lmpDate = date, eddDate = state.eddDate ?: calendar.time)
        }
    }

    fun onEddDateChange(date: Date) {
        _state.update { state ->
            // If LMP is not set, we can estimate it (EDD - 280 days)
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, -280)
            }
            state.copy(eddDate = date, lmpDate = state.lmpDate ?: calendar.time)
        }
    }

    fun onScanDateChange(date: Date) = _state.update { it.copy(scanDate = date) }
    fun onScanWeeksChange(weeks: Int) = _state.update { it.copy(scanWeeks = weeks) }
    fun onScanDaysChange(days: Int) = _state.update { it.copy(scanDays = days) }
    fun onIsRevisedEddChange(isRevised: Boolean) = _state.update { it.copy(isRevisedEdd = isRevised) }

    fun onIsFirstPregnancyChange(isFirst: Boolean) = _state.update { it.copy(isFirstPregnancy = isFirst) }

    fun onSelectPregnancy(index: Int) = _state.update { it.copy(selectedPregnancyIndex = index) }

    fun addPregnancy() {
        _state.update { state ->
            val nextNumber = state.pregnancyHistories.size + 1
            val newList = state.pregnancyHistories + PregnancyHistory(pregnancyNumber = nextNumber)
            state.copy(pregnancyHistories = newList, selectedPregnancyIndex = newList.lastIndex)
        }
    }

    fun removePregnancy(index: Int) {
        _state.update { state ->
            if (state.pregnancyHistories.size <= 1) return@update state
            val newList = state.pregnancyHistories.toMutableList().apply { removeAt(index) }
            // Update pregnancy numbers
            val updatedList = newList.mapIndexed { i, history -> history.copy(pregnancyNumber = i + 1) }
            state.copy(
                pregnancyHistories = updatedList,
                selectedPregnancyIndex = if (state.selectedPregnancyIndex >= updatedList.size) updatedList.lastIndex else state.selectedPregnancyIndex
            )
        }
    }

    fun onHistoryDeliveryDateChange(date: Date) {
        _state.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            val current = newList[state.selectedPregnancyIndex]
            newList[state.selectedPregnancyIndex] = current.copy(deliveryDate = date)
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onDeliveryTypeChange(type: String) {
        _state.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            val current = newList[state.selectedPregnancyIndex]
            newList[state.selectedPregnancyIndex] = current.copy(deliveryType = type)
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onChildrenDeliveredChange(count: String) {
        _state.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            val current = newList[state.selectedPregnancyIndex]
            newList[state.selectedPregnancyIndex] = current.copy(childrenDelivered = count)
            state.copy(pregnancyHistories = newList)
        }
    }

    fun toggleComplication(complication: String) {
        _state.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            val current = newList[state.selectedPregnancyIndex]
            val newComplications = if (current.complications.contains(complication)) {
                current.complications - complication
            } else {
                current.complications + complication
            }
            newList[state.selectedPregnancyIndex] = current.copy(complications = newComplications)
            state.copy(pregnancyHistories = newList)
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
            OnboardingStep.HEIGHT -> OnboardingStep.CYCLE_REGULARITY
            OnboardingStep.CYCLE_REGULARITY -> {
                if (currentState.isCycleRegular == false) OnboardingStep.CYCLE_VARIANCE
                else OnboardingStep.LMP
            }
            OnboardingStep.CYCLE_VARIANCE -> OnboardingStep.HAD_ULTRASOUND
            OnboardingStep.HAD_ULTRASOUND -> {
                if (currentState.hasHadUltrasound == true) OnboardingStep.ULTRASOUND
                else OnboardingStep.EDD // Goes to tentative EDD
            }
            OnboardingStep.LMP -> OnboardingStep.EDD
            OnboardingStep.ULTRASOUND -> {
                calculateEddFromUltrasound()
                OnboardingStep.FIRST_PREGNANCY
            }
            OnboardingStep.EDD -> OnboardingStep.FIRST_PREGNANCY
            OnboardingStep.FIRST_PREGNANCY -> {
                if (currentState.isFirstPregnancy == true) OnboardingStep.PREPARING
                else OnboardingStep.PREGNANCY_HISTORY
            }
            OnboardingStep.PREGNANCY_HISTORY -> OnboardingStep.PREPARING
            OnboardingStep.PREPARING -> OnboardingStep.PREPARING
        }
        
        _state.update { it.copy(currentStep = next) }
        saveCurrentStep(next)
        
        if (next == OnboardingStep.PREPARING) {
            saveAndFinish()
        }
    }

    fun goToUltrasound() {
        _state.update { it.copy(currentStep = OnboardingStep.ULTRASOUND, isUsingUltrasound = true) }
        saveCurrentStep(OnboardingStep.ULTRASOUND)
    }

    private fun calculateEddFromUltrasound() {
        val s = _state.value
        if (s.isRevisedEdd) return // EDD already set via date picker in Ultrasound screen (shared with eddDate)

        val scanDate = s.scanDate ?: return
        
        // EDD = Scan Date + (280 days - (weeks * 7 + days))
        val gestationalDaysAtScan = (s.scanWeeks * 7) + s.scanDays
        val daysToRemaining = 280 - gestationalDaysAtScan
        
        val calendar = Calendar.getInstance().apply {
            time = scanDate
            add(Calendar.DAY_OF_YEAR, daysToRemaining)
        }
        _state.update { it.copy(eddDate = calendar.time) }
    }

    fun previousStep() {
        val currentState = _state.value
        val prev = when (currentState.currentStep) {
            OnboardingStep.BIRTHDAY -> OnboardingStep.BIRTHDAY
            OnboardingStep.WEIGHT -> OnboardingStep.BIRTHDAY
            OnboardingStep.HEIGHT -> OnboardingStep.WEIGHT
            OnboardingStep.CYCLE_REGULARITY -> OnboardingStep.HEIGHT
            OnboardingStep.CYCLE_VARIANCE -> OnboardingStep.CYCLE_REGULARITY
            OnboardingStep.HAD_ULTRASOUND -> OnboardingStep.CYCLE_VARIANCE
            OnboardingStep.LMP -> OnboardingStep.CYCLE_REGULARITY
            OnboardingStep.ULTRASOUND -> OnboardingStep.HAD_ULTRASOUND
            OnboardingStep.EDD -> {
                if (currentState.isCycleRegular == false) OnboardingStep.HAD_ULTRASOUND
                else OnboardingStep.LMP
            }
            OnboardingStep.FIRST_PREGNANCY -> {
                if (currentState.isUsingUltrasound || (currentState.isCycleRegular == false && currentState.hasHadUltrasound == true)) OnboardingStep.ULTRASOUND
                else OnboardingStep.EDD
            }
            OnboardingStep.PREGNANCY_HISTORY -> OnboardingStep.FIRST_PREGNANCY
            OnboardingStep.PREPARING -> OnboardingStep.FIRST_PREGNANCY
        }
        _state.update { it.copy(currentStep = prev) }
        saveCurrentStep(prev)
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
                birthday = s.birthday ?: defaultBirthday,
                weightKg = s.weightKg,
                heightCm = s.heightCm,
                isCycleRegular = s.isCycleRegular,
                shortestCycleDays = if (s.isCycleRegular == false) s.shortestCycleDays else null,
                longestCycleDays = if (s.isCycleRegular == false) s.longestCycleDays else null,
                hasHadUltrasound = s.hasHadUltrasound,
                positiveTestDate = s.positiveTestDate,
                lmpDate = s.lmpDate,
                eddDate = s.eddDate,
                isFirstPregnancy = s.isFirstPregnancy ?: false,
                pregnancyHistories = if (s.isFirstPregnancy == true) emptyList() else s.pregnancyHistories,
                scanDate = s.scanDate,
                scanWeeks = s.scanWeeks,
                scanDays = s.scanDays
            )
            
            repository.saveProfile(profile)
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            preferenceRepository.setOnboardingCompleted(userId)
            
            for (i in 0..100 step 10) {
                _state.update { it.copy(preparationProgress = i / 100f) }
                delay(100)
            }
            _state.update { it.copy(isCompleted = true) }
        }
    }
}
