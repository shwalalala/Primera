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

    private val userEmail: String
        get() = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""
        } catch (_: Exception) {
            ""
        }

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            hasPasswordAuthProvider = authRepository.hasPasswordAuthProvider(),
            authProviders = authRepository.getAuthProviders(),
            email = userEmail
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onNewEmailChange(value: String) = _uiState.update { it.copy(newEmail = value) }

    fun linkGoogleAccount(idToken: String) {
        linkAccount(
            providerName = "Google",
            action = { authRepository.loginWithGoogle(idToken).map { } }
        )
    }

    fun linkFacebookAccount(accessToken: String) {
        linkAccount(
            providerName = "Facebook",
            action = { authRepository.loginWithFacebook(accessToken).map { } }
        )
    }

    private fun linkAccount(providerName: String, action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            action().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authProviders = authRepository.getAuthProviders(),
                            hasPasswordAuthProvider = authRepository.hasPasswordAuthProvider(),
                            successMessage = "$providerName account linked successfully"
                        )
                    }
                },
                onFailure = { error ->
                    val message = if (error is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        "This $providerName account is already linked to another user."
                    } else {
                        error.message ?: "Unable to link $providerName account"
                    }
                    _uiState.update { it.copy(isLoading = false, error = message) }
                }
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return try {
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            if (pattern != null) {
                pattern.matcher(email).matches()
            } else {
                email.contains("@") && email.substringAfter("@").contains(".")
            }
        } catch (_: Throwable) {
            email.contains("@") && email.substringAfter("@").contains(".")
        }
    }

    fun requestEmailChange() {
        val newEmail = _uiState.value.newEmail.trim()
        if (newEmail.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a valid new email address") }
            return
        }
        if (!isValidEmail(newEmail)) {
            _uiState.update { it.copy(error = "Invalid email format") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            authRepository.requestEmailChange(newEmail).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            newEmail = "",
                            successMessage = "A verification link has been sent to $newEmail. Please check your inbox to confirm the change."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to request email change") }
                }
            )
        }
    }

    fun sendPasswordResetEmail() {
        val emailToSend = userEmail.ifBlank { _uiState.value.email }
        if (emailToSend.isBlank()) {
            _uiState.update { it.copy(error = "No registered email address found for this account.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            authRepository.sendPasswordResetEmail(emailToSend).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "A password reset link has been sent to $emailToSend."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to send password reset email") }
                }
            )
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            authRepository.deleteAccount().fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Account deletion failed. Re-authenticating by logging out and logging back in may be required."
                        )
                    }
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

    fun showError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message) }
    }
}
