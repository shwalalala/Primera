package com.example.primera.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onNewEmailChange(value: String) = _uiState.update { it.copy(newEmail = value) }
    fun onNewPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value) }

    fun updateEmail() {
        val newEmail = _uiState.value.newEmail
        if (newEmail.isBlank()) {
            _uiState.update { it.copy(error = "Email cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.updateEmail(newEmail).fold(
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
        val newPassword = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword
        
        if (newPassword.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.updatePassword(newPassword).fold(
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
