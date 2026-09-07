package cit.edu.primera.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.feature.auth.data.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect: Flow<AuthEffect> = _effect.receiveAsFlow()

    init {
        checkSession()
    }

    fun onTabSelected(tab: AuthTab) {
        _state.update { it.copy(activeTab = tab, errorMessage = null, resetEmailSent = false) }
    }

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, usernameError = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null) }
    }

    fun onRememberMeToggle(checked: Boolean) {
        _state.update { it.copy(rememberMe = checked) }
    }

    fun onAgreedToTermsToggle(checked: Boolean) {
        _state.update { it.copy(agreedToTerms = checked, termsError = null) }
    }

    fun onLoginClicked() {
        val currentState = _state.value
        if (!validateLoginForm()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(currentState.email, currentState.password)
            
            result.fold(
                onSuccess = {
                    val userId = authRepository.getCurrentUserId() ?: ""
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        username = "",
                        email = "",
                        password = ""
                    ) }
                    if (preferenceRepository.shouldShowOnboarding(userId)) {
                        _effect.send(AuthEffect.NavigateToOnboarding)
                    } else {
                        _effect.send(AuthEffect.NavigateToDashboard)
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun onSignUpClicked() {
        val currentState = _state.value
        if (!validateRegisterForm()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signUp(
                currentState.username,
                currentState.email,
                currentState.password
            )

            result.fold(
                onSuccess = {
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        username = "",
                        email = "",
                        password = ""
                    ) }
                    _effect.send(AuthEffect.NavigateToOnboarding)
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun onForgotPasswordClicked() {
        _state.update { it.copy(errorMessage = null, resetEmailSent = false) }
        viewModelScope.launch {
            _effect.send(AuthEffect.NavigateToForgotPassword)
        }
    }

    fun onResetPasswordClicked() {
        val email = _state.value.email
        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required to reset password") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, resetEmailSent = false) }
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, resetEmailSent = true) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(
                isAuthenticated = false,
                username = "",
                email = "",
                password = ""
            ) }
            _effect.send(AuthEffect.NavigateToLogin)
        }
    }

    private fun checkSession() {
        if (authRepository.isUserAuthenticated()) {
            val userId = authRepository.getCurrentUserId() ?: ""
            _state.update { it.copy(isAuthenticated = true) }
            viewModelScope.launch {
                if (preferenceRepository.shouldShowOnboarding(userId)) {
                    _effect.send(AuthEffect.NavigateToOnboarding)
                } else {
                    _effect.send(AuthEffect.NavigateToDashboard)
                }
            }
        }
    }

    private fun validateLoginForm(): Boolean {
        val email = _state.value.email
        val password = _state.value.password
        var isValid = true

        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }
            isValid = false
        }
        if (password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }
            isValid = false
        }
        return isValid
    }

    private fun validateRegisterForm(): Boolean {
        val username = _state.value.username
        val email = _state.value.email
        val password = _state.value.password
        val agreed = _state.value.agreedToTerms
        var isValid = true

        if (username.isBlank()) {
            _state.update { it.copy(usernameError = "Username is required") }
            isValid = false
        }
        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }
            isValid = false
        }
        if (password.length < 6) {
            _state.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }
        if (!agreed) {
            _state.update { it.copy(termsError = "You must agree to terms") }
            isValid = false
        }
        return isValid
    }
}
