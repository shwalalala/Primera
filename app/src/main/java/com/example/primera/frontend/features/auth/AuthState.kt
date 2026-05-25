package com.example.primera.frontend.features.auth

import com.example.primera.frontend.common.components.AuthTab

data class AuthState(
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
    data class ShowSnackbar(val message: String) : AuthEffect
    data object NavigateToForgotPassword : AuthEffect
}
