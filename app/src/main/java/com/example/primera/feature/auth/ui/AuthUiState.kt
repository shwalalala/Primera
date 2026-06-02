package com.example.primera.feature.auth.ui

data class AuthUiState(
    val activeTab: AuthTab = AuthTab.LOGIN,
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val agreedToTerms: Boolean = false,
    
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val termsError: String? = null
)

sealed class AuthEffect {
    object NavigateToDashboard : AuthEffect()
    object NavigateToOnboarding : AuthEffect()
    object NavigateToLogin : AuthEffect()
    object NavigateToForgotPassword : AuthEffect()
}
