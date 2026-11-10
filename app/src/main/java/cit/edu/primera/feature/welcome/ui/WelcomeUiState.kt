package cit.edu.primera.feature.welcome.ui

import androidx.annotation.DrawableRes

data class WelcomeUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class WelcomePageData(
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String,
    val subtitle: String = "",
    @DrawableRes val iconRes: Int? = null,
    val tags: List<String> = emptyList()
)

sealed class WelcomeEffect {
    object NavigateToRegister : WelcomeEffect()
    object NavigateToLogin : WelcomeEffect()
    object NavigateToAuth : WelcomeEffect()
}
