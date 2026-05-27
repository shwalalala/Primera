package com.example.primera.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.primera.PrimeraApplication
import com.example.primera.feature.auth.ui.AuthViewModel
import com.example.primera.feature.dashboard.ui.DashboardViewModel
import com.example.primera.feature.onboarding.ui.OnboardingViewModel
import com.example.primera.feature.splash.ui.SplashViewModel
import com.example.primera.feature.welcome.ui.WelcomeViewModel

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
                SplashViewModel::class.java -> SplashViewModel()
                WelcomeViewModel::class.java -> WelcomeViewModel()
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            } as T
        }
    }
}
