package cit.edu.primera.feature.auth.ui

import cit.edu.primera.feature.auth.domain.PasswordRuleViolation

enum class PasswordStrength(val label: String) {
    NONE(""),
    WEAK("Weak"),
    FAIR("Fair"),
    STRONG("Strong")
}

data class AuthUiState(
    val activeTab: AuthTab = AuthTab.LOGIN,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordStrength: PasswordStrength = PasswordStrength.NONE,
    val passwordRuleViolations: List<PasswordRuleViolation> = emptyList(),
    val rememberMe: Boolean = false,
    val agreedToTerms: Boolean = false,
    val showTermsDialog: Boolean = false,
    
    val showAccountLinkingDialog: Boolean = false,
    val pendingSocialProvider: String? = null,
    val linkedProviders: Set<String> = emptySet(),
    val cooldownSeconds: Int = 0,

    val resetEmailSent: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null
)

sealed class AuthEffect {
    object NavigateToDashboard : AuthEffect()
    object NavigateToOnboarding : AuthEffect()
    object NavigateToLogin : AuthEffect()
    object NavigateToRegister : AuthEffect()
    object NavigateToForgotPassword : AuthEffect()
}
