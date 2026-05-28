package com.example.primera.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.primera.PrimeraApplication
import com.example.primera.feature.auth.ui.AuthViewModel
import com.example.primera.feature.checkins.ui.CheckinsViewModel
import com.example.primera.feature.dashboard.ui.DashboardViewModel
import com.example.primera.feature.onboarding.ui.OnboardingViewModel
import com.example.primera.feature.splash.ui.SplashViewModel
import com.example.primera.feature.transcription.data.SpeechRecognitionManager
import com.example.primera.feature.transcription.ui.TranscriptionViewModel
import com.example.primera.feature.welcome.ui.WelcomeViewModel
import com.google.firebase.auth.FirebaseAuth

object ViewModelProvider {
    val Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as PrimeraApplication
            val container = application.container
            
            return when (modelClass) {
                AuthViewModel::class.java -> AuthViewModel(container.authRepository)
                DashboardViewModel::class.java -> DashboardViewModel(container.dashboardRepository)
                OnboardingViewModel::class.java -> OnboardingViewModel(container.onboardingRepository)
                SplashViewModel::class.java -> SplashViewModel(container.preferenceRepository)
                WelcomeViewModel::class.java -> WelcomeViewModel(container.preferenceRepository)
                CheckinsViewModel::class.java -> CheckinsViewModel(container.checkinsRepository)
                TranscriptionViewModel::class.java -> TranscriptionViewModel(
                    container.transcriptionRepository,
                    SpeechRecognitionManager(application),
                    FirebaseAuth.getInstance()
                )
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            } as T
        }
    }
}
