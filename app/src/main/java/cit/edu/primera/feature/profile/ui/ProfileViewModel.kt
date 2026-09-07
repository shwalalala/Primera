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
                            firstName = data.firstName ?: "",
                            lastName = data.lastName ?: "",
                            middleName = data.middleName ?: "",
                            birthday = data.birthday,
                            weightKg = weightData?.weightKg ?: 0,
                            heightCm = data.heightCm ?: 0,
                            eddDate = data.dueDate,
                            isLoading = false
                        )
                    }
                }
            }.collect {}
        }
    }

    fun onFirstNameChange(name: String) = _uiState.update { it.copy(firstName = name) }
    fun onMiddleNameChange(name: String) = _uiState.update { it.copy(middleName = name) }
    fun onLastNameChange(name: String) = _uiState.update { it.copy(lastName = name) }
    fun onWeightChange(weight: Int) = _uiState.update { it.copy(weightKg = weight) }
    fun onHeightChange(height: Int) = _uiState.update { it.copy(heightCm = height) }
    fun onEddDateChange(date: Date) = _uiState.update { it.copy(eddDate = date) }

    fun toggleEdit() = _uiState.update { it.copy(isEditing = !it.isEditing) }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val profile = OnboardingProfile(
                birthday = _uiState.value.birthday ?: Date(),
                weightKg = _uiState.value.weightKg,
                heightCm = _uiState.value.heightCm,
                lmpDate = _uiState.value.lmpDate,
                eddDate = _uiState.value.eddDate,
                isFirstPregnancy = _uiState.value.isFirstPregnancy,
                pregnancyHistories = emptyList() // We don't want to overwrite history here
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
