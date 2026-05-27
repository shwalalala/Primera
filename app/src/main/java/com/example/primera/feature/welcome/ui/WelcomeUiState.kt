package com.example.primera.feature.welcome.ui

data class WelcomeUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class WelcomePageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

sealed class WelcomeEffect {
    object NavigateToAuth : WelcomeEffect()
}
