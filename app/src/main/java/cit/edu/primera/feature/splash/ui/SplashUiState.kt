package cit.edu.primera.feature.splash.ui

sealed class SplashUiState {
    object Animating : SplashUiState()
    object Complete : SplashUiState()
}

sealed class SplashEffect {
    object Navigate : SplashEffect()
    object NavigateToAuth : SplashEffect()
    object NavigateToDashboard : SplashEffect()
}
