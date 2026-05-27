package com.example.primera.core.di

import com.example.primera.feature.auth.data.datasource.AuthDataSource
import com.example.primera.feature.auth.data.repository.AuthRepository
import com.example.primera.feature.auth.data.repository.AuthRepositoryImpl
import com.example.primera.feature.dashboard.data.datasource.DashboardDataSource
import com.example.primera.feature.dashboard.data.repository.DashboardRepository
import com.example.primera.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.example.primera.feature.onboarding.data.datasource.OnboardingDataSource
import com.example.primera.feature.onboarding.data.repository.OnboardingRepository
import com.example.primera.feature.onboarding.data.repository.OnboardingRepositoryImpl

interface AppContainer {
    val authRepository: AuthRepository
    val dashboardRepository: DashboardRepository
    val onboardingRepository: OnboardingRepository
}

class AppContainerImpl : AppContainer {
    
    // Auth dependencies
    private val authDataSource: AuthDataSource by lazy {
        AuthDataSource()
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authDataSource)
    }

    // Dashboard dependencies
    private val dashboardDataSource: DashboardDataSource by lazy {
        DashboardDataSource()
    }

    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(dashboardDataSource)
    }

    // Onboarding dependencies
    private val onboardingDataSource: OnboardingDataSource by lazy {
        OnboardingDataSource()
    }

    override val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl(onboardingDataSource)
    }
}
