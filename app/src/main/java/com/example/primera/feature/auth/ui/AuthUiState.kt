package com.example.primera.feature.auth.ui

import com.example.primera.ui.components.AuthTab

data class AuthUiState(
    val activeTab: AuthTab = AuthTab.LOGIN,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val agreedToTerms: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

sealed interface AuthEffect {
    data object NavigateToDashboard : AuthEffect
    data object NavigateToLogin : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
    data object NavigateToForgotPassword : AuthEffect
}
