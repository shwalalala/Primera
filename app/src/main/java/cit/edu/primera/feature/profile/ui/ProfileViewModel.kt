package cit.edu.primera.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.onboarding.data.OnboardingRepository
import cit.edu.primera.feature.onboarding.domain.OnboardingProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ProfileViewModel(
    private val dashboardRepository: DashboardRepository,
    private val checkinsRepository: CheckinsRepository,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                dashboardRepository.observeDashboardData(),
                checkinsRepository.observeUserWeight()
            ) { data, weightData ->
                if (data != null) {
                    _uiState.update { 
                        it.copy(
                            email = data.email ?: "",
                            birthday = data.birthday,
                            weightKg = data.weightKg ?: weightData?.weightKg ?: 0,
                            heightCm = data.heightCm ?: 0,
                            lmpDate = data.lmpDate,
                            eddDate = data.dueDate,
                            isFirstPregnancy = data.isFirstPregnancy ?: true,
                            isCycleRegular = data.isCycleRegular,
                            shortestCycleDays = data.shortestCycleDays,
                            longestCycleDays = data.longestCycleDays,
                            hasHadUltrasound = data.hasHadUltrasound,
                            positiveTestDate = data.positiveTestDate,
                            scanDate = data.scanDate,
                            scanWeeks = data.scanWeeks,
                            scanDays = data.scanDays,
                            pregnancyHistories = data.pregnancyHistories,
                            emergencyContact = cit.edu.primera.feature.profile.domain.EmergencyContact(
                                name = data.iceName ?: "",
                                relationship = data.iceRelationship ?: "",
                                primaryPhone = data.icePrimaryPhone ?: "",
                                secondaryPhone = data.iceSecondaryPhone ?: ""
                            ),
                            isLoading = false
                        )
                    }
                }
            }.collect {}
        }
    }

    fun onWeightChange(weight: Int) = _uiState.update { it.copy(weightKg = weight) }
    fun onHeightChange(height: Int) = _uiState.update { it.copy(heightCm = height) }
    fun onEddDateChange(date: Date) = _uiState.update { it.copy(eddDate = date) }
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onBirthdayChange(date: Date) = _uiState.update { it.copy(birthday = date) }
    fun onLmpDateChange(date: Date) = _uiState.update { it.copy(lmpDate = date) }
    fun onIsCycleRegularChange(isRegular: Boolean) = _uiState.update { it.copy(isCycleRegular = isRegular) }
    fun onShortestCycleChange(days: Int) = _uiState.update { it.copy(shortestCycleDays = days) }
    fun onLongestCycleChange(days: Int) = _uiState.update { it.copy(longestCycleDays = days) }
    fun onHasHadUltrasoundChange(hasHad: Boolean) = _uiState.update { it.copy(hasHadUltrasound = hasHad) }
    fun onScanDateChange(date: Date) {
        _uiState.update { it.copy(scanDate = date) }
        calculateEddFromUltrasound()
    }
    fun onScanWeeksChange(weeks: Int) {
        _uiState.update { it.copy(scanWeeks = weeks) }
        calculateEddFromUltrasound()
    }
    fun onScanDaysChange(days: Int) {
        _uiState.update { it.copy(scanDays = days) }
        calculateEddFromUltrasound()
    }
    fun onPositiveTestDateChange(date: Date) = _uiState.update { it.copy(positiveTestDate = date) }

    private fun calculateEddFromUltrasound() {
        val s = _uiState.value
        val scanDate = s.scanDate ?: return
        
        // EDD = Scan Date + (280 days - (weeks * 7 + days))
        val gestationalDaysAtScan = ((s.scanWeeks ?: 0) * 7) + (s.scanDays ?: 0)
        val daysToRemaining = 280 - gestationalDaysAtScan
        
        val calendar = Calendar.getInstance().apply {
            time = scanDate
            add(Calendar.DAY_OF_YEAR, daysToRemaining)
        }
        _uiState.update { it.copy(eddDate = calendar.time) }
    }

    fun onHistoryDeliveryDateChange(index: Int, date: Date) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(deliveryDate = date)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryDeliveryTypeChange(index: Int, type: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(deliveryType = type)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryChildrenChange(index: Int, count: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(childrenDelivered = count)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    fun onHistoryComplicationsChange(index: Int, complication: String) {
        _uiState.update { state ->
            val newList = state.pregnancyHistories.toMutableList()
            if (index in newList.indices) {
                val current = newList[index]
                val newComps = if (current.complications.contains(complication)) {
                    current.complications - complication
                } else {
                    current.complications + complication
                }
                newList[index] = current.copy(complications = newComps)
            }
            state.copy(pregnancyHistories = newList)
        }
    }

    // ICE Contact
    fun onIceNameChange(name: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(name = name)) 
    }
    fun onIceRelationshipChange(rel: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(relationship = rel)) 
    }
    fun onIcePrimaryPhoneChange(phone: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(primaryPhone = phone)) 
    }
    fun onIceSecondaryPhoneChange(phone: String) = _uiState.update { 
        it.copy(emergencyContact = it.emergencyContact.copy(secondaryPhone = phone)) 
    }

    // UI State
    fun toggleSection(section: ProfileSection) {
        _uiState.update { state ->
            val newSections = if (state.expandedSections.contains(section)) {
                state.expandedSections - section
            } else {
                state.expandedSections + section
            }
            state.copy(expandedSections = newSections)
        }
    }

    fun toggleEdit() = _uiState.update { it.copy(isEditing = !it.isEditing) }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val profile = OnboardingProfile(
                email = _uiState.value.email,
                birthday = _uiState.value.birthday ?: Date(),
                weightKg = _uiState.value.weightKg,
                heightCm = _uiState.value.heightCm,
                isCycleRegular = _uiState.value.isCycleRegular,
                shortestCycleDays = _uiState.value.shortestCycleDays,
                longestCycleDays = _uiState.value.longestCycleDays,
                hasHadUltrasound = _uiState.value.hasHadUltrasound,
                positiveTestDate = _uiState.value.positiveTestDate,
                lmpDate = _uiState.value.lmpDate,
                eddDate = _uiState.value.eddDate,
                isFirstPregnancy = _uiState.value.isFirstPregnancy,
                scanDate = _uiState.value.scanDate,
                scanWeeks = _uiState.value.scanWeeks,
                scanDays = _uiState.value.scanDays,
                iceName = _uiState.value.emergencyContact.name,
                iceRelationship = _uiState.value.emergencyContact.relationship,
                icePrimaryPhone = _uiState.value.emergencyContact.primaryPhone,
                iceSecondaryPhone = _uiState.value.emergencyContact.secondaryPhone,
                pregnancyHistories = _uiState.value.pregnancyHistories
            )

            onboardingRepository.saveProfile(profile).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, isEditing = false, successMessage = "Profile updated successfully") }
                }
            ) { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message ?: "Failed to save profile") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
