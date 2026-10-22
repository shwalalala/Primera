package cit.edu.primera.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.feature.auth.data.AuthRepository
import cit.edu.primera.feature.auth.domain.PasswordRuleViolation
import cit.edu.primera.feature.auth.domain.PasswordValidator
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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

    private var pendingCredential: AuthCredential? = null
    private var cooldownJob: Job? = null

    init {
        loadSavedPreferences()
        checkSession()
    }

    private fun loadSavedPreferences() {
        val savedEmail = preferenceRepository.getSavedEmail()
        if (!savedEmail.isNullOrBlank()) {
            _state.update {
                it.copy(
                    email = savedEmail,
                    rememberMe = true
                )
            }
        }
    }

    fun onNavigateToRegisterClicked() {
        wipeSensitiveState()
        viewModelScope.launch {
            _effect.send(AuthEffect.NavigateToRegister)
        }
    }

    fun onNavigateToLoginClicked() {
        wipeSensitiveState()
        viewModelScope.launch {
            _effect.send(AuthEffect.NavigateToLogin)
        }
    }

    fun onTabSelected(tab: AuthTab) {
        wipeSensitiveState()
        _state.update {
            it.copy(
                activeTab = tab,
                errorMessage = null,
                resetEmailSent = false,
                usernameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                termsError = null
            )
        }
    }

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, usernameError = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        val strength = PasswordValidator.calculateStrength(value)
        val validation = PasswordValidator.validate(value)
        _state.update {
            val confirmErr = if (it.confirmPassword.isNotBlank() && it.confirmPassword != value) {
                "Passwords do not match"
            } else null
            it.copy(
                password = value,
                passwordStrength = strength,
                passwordRuleViolations = validation.violations,
                passwordError = null,
                confirmPasswordError = confirmErr
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update {
            val confirmErr = if (value.isNotBlank() && value != it.password) {
                "Passwords do not match"
            } else null
            it.copy(
                confirmPassword = value,
                confirmPasswordError = confirmErr
            )
        }
    }

    fun onRememberMeToggle(checked: Boolean) {
        _state.update { it.copy(rememberMe = checked) }
        if (!checked) {
            preferenceRepository.setSavedEmail(null)
        }
    }

    fun onAgreedToTermsToggle(checked: Boolean) {
        _state.update { it.copy(agreedToTerms = checked, termsError = null) }
    }

    fun onTermsClicked() {
        _state.update { it.copy(showTermsDialog = true) }
    }

    fun onDismissTermsDialog() {
        _state.update { it.copy(showTermsDialog = false) }
    }

    fun onDismissAccountLinkingDialog() {
        pendingCredential = null
        _state.update {
            it.copy(
                showAccountLinkingDialog = false,
                pendingSocialProvider = null
            )
        }
    }

    fun onLoginClicked() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.cooldownSeconds > 0) return
        val trimmedEmail = currentState.email.trim()
        if (!validateLoginForm()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(trimmedEmail, currentState.password)

            result.fold(
                onSuccess = {
                    handleSuccessfulAuthentication(trimmedEmail, currentState.rememberMe)
                },
                onFailure = { error ->
                    handleAuthenticationFailure(error, "Invalid email or password")
                }
            )
        }
    }

    fun onSignUpClicked() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.cooldownSeconds > 0) return
        val trimmedUsername = currentState.username.trim()
        val trimmedEmail = currentState.email.trim()
        if (!validateRegisterForm()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signUp(
                trimmedUsername,
                trimmedEmail,
                currentState.password
            )

            result.fold(
                onSuccess = {
                    handleSuccessfulAuthentication(trimmedEmail, false)
                },
                onFailure = { error ->
                    handleAuthenticationFailure(error, "Registration failed. Please check your details.")
                }
            )
        }
    }

    fun onForgotPasswordClicked() {
        _state.update { it.copy(errorMessage = null, resetEmailSent = false, emailError = null) }
        viewModelScope.launch {
            _effect.send(AuthEffect.NavigateToForgotPassword)
        }
    }

    fun onResetPasswordClicked() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.cooldownSeconds > 0) return
        val email = currentState.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required to reset password") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }
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
                    if (isTooManyRequestsError(error)) {
                        startRateLimitCooldown()
                    } else {
                        // Always set resetEmailSent = true to prevent user enumeration attacks
                        _state.update { it.copy(isLoading = false, resetEmailSent = true) }
                    }
                }
            )
        }
    }

    fun onSocialLoginClicked(provider: String) {
        _state.update { it.copy(errorMessage = "$provider login is coming soon") }
    }

    fun onFacebookLoginSuccess(accessToken: String) {
        val currentState = _state.value
        if (currentState.isLoading || currentState.cooldownSeconds > 0) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = authRepository.loginWithFacebook(accessToken)

            result.fold(
                onSuccess = {
                    handleSuccessfulAuthentication(_state.value.email, currentState.rememberMe)
                },
                onFailure = { error ->
                    if (error is FirebaseAuthUserCollisionException) {
                        pendingCredential = credential
                        _state.update {
                            it.copy(
                                isLoading = false,
                                showAccountLinkingDialog = true,
                                pendingSocialProvider = "Facebook"
                            )
                        }
                    } else {
                        handleAuthenticationFailure(error, "Unable to sign in with Facebook. Please try again later.")
                    }
                }
            )
        }
    }

    fun onFacebookLoginError(error: Exception) {
        android.util.Log.e("AuthViewModel", "Facebook SDK login error", error)
        val msg = error.message ?: ""
        val displayError = if (msg.contains("canceled", ignoreCase = true) || msg.contains("cancelled", ignoreCase = true)) {
            "Facebook sign-in was cancelled."
        } else {
            "Unable to sign in with Facebook. Please try again later."
        }
        _state.update { it.copy(isLoading = false, errorMessage = displayError) }
    }

    fun onGoogleLoginSuccess(idToken: String) {
        val currentState = _state.value
        if (currentState.isLoading || currentState.cooldownSeconds > 0) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = authRepository.loginWithGoogle(idToken)

            result.fold(
                onSuccess = {
                    handleSuccessfulAuthentication(_state.value.email, currentState.rememberMe)
                },
                onFailure = { error ->
                    if (error is FirebaseAuthUserCollisionException) {
                        pendingCredential = credential
                        _state.update {
                            it.copy(
                                isLoading = false,
                                showAccountLinkingDialog = true,
                                pendingSocialProvider = "Google"
                            )
                        }
                    } else {
                        handleAuthenticationFailure(error, "Google login failed. Please try again.")
                    }
                }
            )
        }
    }

    fun onGoogleLoginError(error: Exception) {
        android.util.Log.e("AuthViewModel", "Google login error", error)
        val msg = error.message ?: ""
        val displayError = if (msg.contains("canceled", ignoreCase = true) || msg.contains("cancelled", ignoreCase = true)) {
            "Google login was cancelled."
        } else {
            "Google login failed. Please try again."
        }
        _state.update { it.copy(isLoading = false, errorMessage = displayError) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            wipeSensitiveState()
            _state.update { it.copy(
                isAuthenticated = false,
                username = "",
                email = preferenceRepository.getSavedEmail() ?: ""
            ) }
            _effect.send(AuthEffect.NavigateToLogin)
        }
    }

    private fun handleSuccessfulAuthentication(email: String, rememberMe: Boolean) {
        preferenceRepository.setHasEverLoggedIn(true)
        if (rememberMe && email.isNotBlank()) {
            preferenceRepository.setSavedEmail(email)
        } else {
            preferenceRepository.setSavedEmail(null)
        }

        val pending = pendingCredential
        val userId = authRepository.getCurrentUserId() ?: ""

        viewModelScope.launch {
            if (pending != null) {
                authRepository.linkCredential(pending)
                pendingCredential = null
            }

            wipeSensitiveState()
            _state.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    showAccountLinkingDialog = false,
                    pendingSocialProvider = null,
                    linkedProviders = authRepository.getAuthProviders()
                )
            }

            if (preferenceRepository.shouldShowOnboarding(userId)) {
                _effect.send(AuthEffect.NavigateToOnboarding)
            } else {
                _effect.send(AuthEffect.NavigateToDashboard)
            }
        }
    }

    private fun handleAuthenticationFailure(error: Throwable, defaultMessage: String) {
        android.util.Log.e("AuthViewModel", "Authentication operation failed", error)
        if (isTooManyRequestsError(error)) {
            startRateLimitCooldown()
        } else {
            _state.update { it.copy(isLoading = false, errorMessage = defaultMessage) }
        }
    }

    private fun isTooManyRequestsError(error: Throwable): Boolean {
        if (error is FirebaseAuthException) {
            return error.errorCode == "ERROR_TOO_MANY_REQUESTS" || (error.message?.contains("TOO_MANY_REQUESTS", ignoreCase = true) == true)
        }
        val msg = error.message ?: ""
        return msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) || msg.contains("too many", ignoreCase = true)
    }

    private fun startRateLimitCooldown() {
        cooldownJob?.cancel()
        _state.update {
            it.copy(
                isLoading = false,
                cooldownSeconds = 30,
                errorMessage = "Please wait a moment before trying again."
            )
        }
        cooldownJob = viewModelScope.launch {
            for (i in 30 downTo 1) {
                _state.update { it.copy(cooldownSeconds = i) }
                delay(1000)
            }
            _state.update { it.copy(cooldownSeconds = 0, errorMessage = null) }
        }
    }

    private fun checkSession() {
        if (authRepository.isUserAuthenticated()) {
            preferenceRepository.setHasEverLoggedIn(true)
            val userId = authRepository.getCurrentUserId() ?: ""
            _state.update {
                it.copy(
                    isAuthenticated = true,
                    linkedProviders = authRepository.getAuthProviders()
                )
            }
            viewModelScope.launch {
                if (preferenceRepository.shouldShowOnboarding(userId)) {
                    _effect.send(AuthEffect.NavigateToOnboarding)
                } else {
                    _effect.send(AuthEffect.NavigateToDashboard)
                }
            }
        }
    }

    private fun wipeSensitiveState() {
        pendingCredential = null
        _state.update {
            it.copy(
                password = "",
                confirmPassword = "",
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null,
                showAccountLinkingDialog = false,
                pendingSocialProvider = null
            )
        }
    }

    private fun validateLoginForm(): Boolean {
        val email = _state.value.email.trim()
        val password = _state.value.password
        var isValid = true

        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }
            isValid = false
        }
        return isValid
    }

    private fun validateRegisterForm(): Boolean {
        val username = _state.value.username.trim()
        val email = _state.value.email.trim()
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword
        val agreed = _state.value.agreedToTerms
        var isValid = true

        if (username.isBlank()) {
            _state.update { it.copy(usernameError = "Username is required") }
            isValid = false
        }
        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        val validation = PasswordValidator.validate(password)
        if (!validation.isValid) {
            val msg = when {
                validation.violations.contains(PasswordRuleViolation.TOO_SHORT) -> "Password must be at least 8 characters"
                validation.violations.contains(PasswordRuleViolation.MISSING_LETTER) -> "Password must contain letters"
                else -> "Password must contain letters and numbers/symbols"
            }
            _state.update { it.copy(passwordError = msg) }
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            _state.update { it.copy(confirmPasswordError = "Please confirm your password") }
            isValid = false
        } else if (confirmPassword != password) {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        if (!agreed) {
            _state.update { it.copy(termsError = "You must agree to terms") }
            isValid = false
        }
        return isValid
    }

    override fun onCleared() {
        wipeSensitiveState()
        cooldownJob?.cancel()
    }
}
