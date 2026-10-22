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
        val strength = calculatePasswordStrength(value)
        _state.update {
            val confirmErr = if (it.confirmPassword.isNotBlank() && it.confirmPassword != value) {
                "Passwords do not match"
            } else null
            it.copy(
                password = value,
                passwordStrength = strength,
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

    fun onLoginClicked() {
        val currentState = _state.value
        val trimmedEmail = currentState.email.trim()
        if (!validateLoginForm()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(trimmedEmail, currentState.password)
            
            result.fold(
                onSuccess = {
                    val userId = authRepository.getCurrentUserId() ?: ""
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        username = "",
                        email = "",
                        password = "",
                        confirmPassword = ""
                    ) }
                    if (preferenceRepository.shouldShowOnboarding(userId)) {
                        _effect.send(AuthEffect.NavigateToOnboarding)
                    } else {
                        _effect.send(AuthEffect.NavigateToDashboard)
                    }
                },
                onFailure = { _state.update { it.copy(isLoading = false, errorMessage = "Invalid email or password") } }
            )
        }
    }

    fun onSignUpClicked() {
        val currentState = _state.value
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
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        username = "",
                        email = "",
                        password = "",
                        confirmPassword = ""
                    ) }
                    _effect.send(AuthEffect.NavigateToOnboarding)
                },
                onFailure = { _state.update { it.copy(isLoading = false, errorMessage = "Registration failed. Please check your details.") } }
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
        val email = _state.value.email.trim()
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
            // Always set resetEmailSent = true to prevent user enumeration attacks.
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, resetEmailSent = true) }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false, resetEmailSent = true) }
                }
            )
        }
    }

    fun onSocialLoginClicked(provider: String) {
        _state.update { it.copy(errorMessage = "$provider login is coming soon") }
    }

    fun onFacebookLoginSuccess(accessToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            if (!signInWithEnteredEmailIfNeeded()) return@launch
            val result = authRepository.loginWithFacebook(accessToken)
            
            result.fold(
                onSuccess = {
                    val userId = authRepository.getCurrentUserId() ?: ""
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true
                    ) }
                    if (preferenceRepository.shouldShowOnboarding(userId)) {
                        _effect.send(AuthEffect.NavigateToOnboarding)
                    } else {
                        _effect.send(AuthEffect.NavigateToDashboard)
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("AuthViewModel", "Facebook Firebase authentication failed", error)
                    val displayError = if (error is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        "An account with this email already exists. Please log in with your password or primary provider first to link Facebook."
                    } else {
                        "Unable to sign in with Facebook. Please try again later."
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = displayError) }
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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            if (!signInWithEnteredEmailIfNeeded()) return@launch
            val result = authRepository.loginWithGoogle(idToken)

            result.fold(
                onSuccess = {
                    val userId = authRepository.getCurrentUserId() ?: ""
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true
                    ) }
                    if (preferenceRepository.shouldShowOnboarding(userId)) {
                        _effect.send(AuthEffect.NavigateToOnboarding)
                    } else {
                        _effect.send(AuthEffect.NavigateToDashboard)
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("AuthViewModel", "Google Firebase authentication failed", error)
                    val displayError = if (error is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        "An account with this email already exists. Please log in with your password or primary provider first to link Google."
                    } else {
                        "Google login failed. Please try again."
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = displayError) }
                }
            )
        }
    }

    fun onGoogleLoginError(error: Exception) {
        android.util.Log.e("AuthViewModel", "Google login error", error)
        val msg = error.message ?: ""
        val displayError = if (msg.contains("SHA-1", ignoreCase = true) || msg.contains("Web Client ID", ignoreCase = true)) {
            "Google login requires SHA-1 fingerprint configuration in Firebase Console."
        } else if (msg.contains("canceled", ignoreCase = true) || msg.contains("cancelled", ignoreCase = true)) {
            "Google login was cancelled."
        } else {
            "Google login was cancelled or failed. Please try again."
        }
        _state.update { it.copy(isLoading = false, errorMessage = displayError) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            preferenceRepository.clearLocalData()
            _state.update { it.copy(
                isAuthenticated = false,
                username = "",
                email = "",
                password = "",
                confirmPassword = ""
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

    private suspend fun signInWithEnteredEmailIfNeeded(): Boolean {
        val currentState = _state.value
        if (authRepository.isUserAuthenticated()
            || currentState.activeTab != AuthTab.LOGIN
            || currentState.email.isBlank()
            || currentState.password.isBlank()
        ) {
            return true
        }

        val result = authRepository.login(
            currentState.email.trim(),
            currentState.password
        )
        if (result.isSuccess) return true

        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = "Enter the email password for this account before linking a social login."
            )
        }
        return false
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        if (password.length < 6) return PasswordStrength.WEAK

        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 3 && password.length >= 8 -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.FAIR
            else -> PasswordStrength.WEAK
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
        
        if (password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (password.length < 8) {
            _state.update { it.copy(passwordError = "Password must be at least 8 characters") }
            isValid = false
        } else if (!password.any { it.isDigit() || !it.isLetterOrDigit() } || !password.any { it.isLetter() }) {
            _state.update { it.copy(passwordError = "Password must contain letters and numbers/symbols") }
            isValid = false
        } else if (calculatePasswordStrength(password) == PasswordStrength.WEAK) {
            _state.update { it.copy(passwordError = "Password is too weak. Please include uppercase, lowercase, numbers or symbols.") }
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
}
