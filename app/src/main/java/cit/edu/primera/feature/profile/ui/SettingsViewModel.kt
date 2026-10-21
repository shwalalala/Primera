package cit.edu.primera.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(hasPasswordAuthProvider = authRepository.hasPasswordAuthProvider())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onNewEmailChange(value: String) = _uiState.update { it.copy(newEmail = value) }
    fun onEmailCurrentPasswordChange(value: String) =
        _uiState.update { it.copy(emailCurrentPassword = value) }
    fun onPasswordCurrentPasswordChange(value: String) =
        _uiState.update { it.copy(passwordCurrentPassword = value) }
    fun onNewPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value) }

    fun updateEmail() {
        val currentPassword = _uiState.value.emailCurrentPassword
        val newEmail = _uiState.value.newEmail
        if (_uiState.value.hasPasswordAuthProvider && currentPassword.isBlank()) {
            _uiState.update { it.copy(error = "Current password is required") }
            return
        }
        if (newEmail.isBlank()) {
            _uiState.update { it.copy(error = "Email cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            authRepository.updateEmail(currentPassword, newEmail).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Email updated successfully") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun updatePassword() {
        val currentPassword = _uiState.value.passwordCurrentPassword
        val newPassword = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword
        if (_uiState.value.hasPasswordAuthProvider && currentPassword.isBlank()) {
            _uiState.update { it.copy(error = "Current password is required") }
            return
        }
        
        if (newPassword.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            authRepository.updatePassword(currentPassword, newPassword).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Password updated successfully") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
