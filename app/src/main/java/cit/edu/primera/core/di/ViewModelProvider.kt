package cit.edu.primera.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import cit.edu.primera.PrimeraApplication
import cit.edu.primera.feature.auth.ui.AuthViewModel
import cit.edu.primera.feature.checkins.ui.CheckinsViewModel
import cit.edu.primera.feature.dashboard.ui.DashboardViewModel
import cit.edu.primera.feature.insights.ui.InsightsViewModel
import cit.edu.primera.feature.profile.ui.ProfileViewModel
import cit.edu.primera.feature.profile.ui.SettingsViewModel
import cit.edu.primera.feature.onboarding.ui.OnboardingViewModel
import cit.edu.primera.feature.smartwatchconnection.ui.SmartwatchViewModel
import cit.edu.primera.feature.splash.ui.SplashViewModel
import cit.edu.primera.feature.transcription.data.SpeechRecognitionManager
import cit.edu.primera.feature.transcription.ui.TranscriptionViewModel
import cit.edu.primera.feature.welcome.ui.WelcomeViewModel

object ViewModelProvider {
    val Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as PrimeraApplication
            val container = application.container
            
            return when (modelClass) {
                AuthViewModel::class.java -> AuthViewModel(
                    container.authRepository,
                    container.preferenceRepository
                )
                DashboardViewModel::class.java -> DashboardViewModel(
                    container.dashboardRepository,
                    container.goalsRepository,
                    container.healthConnectManager,
                    container.preferenceRepository,
                    container.networkMonitor
                )
                OnboardingViewModel::class.java -> OnboardingViewModel(
                    container.onboardingRepository,
                    container.preferenceRepository
                )
                SplashViewModel::class.java -> SplashViewModel(container.preferenceRepository)
                WelcomeViewModel::class.java -> WelcomeViewModel(container.preferenceRepository)
                CheckinsViewModel::class.java -> CheckinsViewModel(
                    container.checkinsRepository,
                    container.symptomExtractor
                )
                TranscriptionViewModel::class.java -> TranscriptionViewModel(
                    container.transcriptionRepository,
                    container.symptomExtractor,
                    SpeechRecognitionManager(application)
                )
                InsightsViewModel::class.java -> InsightsViewModel(
                    container.dashboardRepository,
                    container.checkinsRepository,
                    container.goalsRepository,
                    container.networkMonitor
                )
                SmartwatchViewModel::class.java -> SmartwatchViewModel(
                    container.healthConnectManager,
                    container.preferenceRepository,
                    container.dashboardRepository,
                    container.networkMonitor
                )
                ProfileViewModel::class.java -> ProfileViewModel(
                    container.dashboardRepository,
                    container.checkinsRepository,
                    container.onboardingRepository
                )
                SettingsViewModel::class.java -> SettingsViewModel(
                    container.authRepository
                )
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            } as T
        }
    }
}
